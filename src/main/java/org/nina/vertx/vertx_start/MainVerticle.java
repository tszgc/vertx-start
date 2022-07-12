package org.nina.vertx.vertx_start;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) throws Exception {
//        DeploymentOptions opts = new DeploymentOptions()
//            .setWorker(true)
//            .setInstances(8);
//        vertx.deployVerticle("org.nina.vertx.vertx_start.HelloVerticle", opts);
        //vertx.deployVerticle(new HelloVerticle());

        vertx.deployVerticle("Hello.groovy");
        vertx.deployVerticle("Hello.js");

        Router router = Router.router(vertx);

//        router.route().handler(ctx -> {
//            String authToken = ctx.request().getHeader("AUTH_TOKEN");
//            if (authToken != null && "abc".contentEquals(authToken)) {
//                ctx.next();
//            } else {
//                ctx.response().setStatusCode(401).setStatusMessage("UNAUTHORIZED").end();
//            }
//        });

        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);
        router.route().handler(StaticHandler.create("web"));
//        int httpPort;
//        try {
//            httpPort = Integer.parseInt(System.getProperty("http.port", "8080"));
//        } catch (NumberFormatException nfe) {
//            httpPort = 8080;
//        }
        doConfig(start, router);
    }

    private void doConfig(Promise<Void> start, Router router) {
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

        Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResults(start, router, asyncResult);
        cfgRetriever.getConfig(handler);
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

    void helloVertx(RoutingContext ctx) {
        //ctx.request().response().end("Hello Vert.x World!");
        vertx.eventBus().request("hello.vertx.addr", "", reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }

    void helloName(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        //ctx.request().response().end(String.format("Hello %s!", name));
        vertx.eventBus().request("hello.named.addr", name, reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }
}
