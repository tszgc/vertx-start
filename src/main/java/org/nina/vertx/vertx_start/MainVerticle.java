package org.nina.vertx.vertx_start;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    final JsonObject loadedConfig = new JsonObject();

    @Override
    public void start(Promise<Void> start) throws Exception {
//        DeploymentOptions opts = new DeploymentOptions()
//            .setWorker(true)
//            .setInstances(8);
//        vertx.deployVerticle("org.nina.vertx.vertx_start.HelloVerticle", opts);
        //vertx.deployVerticle(new HelloVerticle());

        // Sequential Composition - Do A, Then B, Then C ... Handle errors
        // Concurrent Composition - Do A, B, C and D and once all/any complete -Do something else ....
        doConfig()
            .compose(this::storeConfig)
            .compose(this::deployOtherVerticles)
            .onComplete(start::handle);

        vertx.deployVerticle("Hello.groovy");
        vertx.deployVerticle("Hello.js");
        /**
        Handler<AsyncResult<Void>> dbMigrationResultHandler = result -> this.handleMigrationResult(start, result);
        vertx.executeBlocking(this::doDataBaseMigrations, dbMigrationResultHandler);
        */

        //doConfig(start, router);
    }

    Future<Void> deployOtherVerticles(Void unused) {
        DeploymentOptions opts = new DeploymentOptions().setConfig(loadedConfig);

        //Future<String> dbVerticle = Future.future(promise -> vertx.deployVerticle(new DataBaseVerticle(), opts, promise));
        Future<String> webVerticle = Future.future(promise -> vertx.deployVerticle(new WebVerticle(), opts, promise));
        Future<String> helloGroovy = Future.future(promise -> vertx.deployVerticle("Hello.groovy", opts, promise));
        Future<String> helloJs = Future.future(promise -> vertx.deployVerticle("Hello.js", opts, promise));

        //return CompositeFuture.all(helloGroovy, helloJs, dbVerticle, webVerticle).mapEmpty();
        return CompositeFuture.all(helloGroovy, helloJs, webVerticle).mapEmpty();
    }

    Future<Void> storeConfig(JsonObject config) {
        loadedConfig.mergeIn(config);
        return Promise.<Void>promise().future();
    }

    void handleMigrationResult(Promise<Void> start, AsyncResult<Void> result) {
        if (result.failed()) {
            start.fail(result.cause());
        }
    }


    Future<JsonObject> doConfig() {
        ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
            .setType("file")
            .setFormat("yaml")
            .setConfig(new JsonObject().put("path", "app.properties"));

        ConfigStoreOptions cliConfig = new ConfigStoreOptions()
            .setType("json")
            .setConfig(config());

        ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
            .addStore(defaultConfig)
            .addStore(cliConfig);

        ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);
        return Future.future(promise -> cfgRetriever.getConfig(promise));
    }

    void handleConfigResults(Promise<Void> start, Router router, AsyncResult<JsonObject> asyncResult) {
        if (asyncResult.succeeded()) {
            JsonObject config = asyncResult.result();
            JsonObject http = config.getJsonObject("http");
            int httpPort = http.getInteger("port");
            vertx.createHttpServer().requestHandler(router).listen(httpPort);
            start.complete();
        } else {
            //
            start.fail("Unable to load configuration");
        }
    }
}
