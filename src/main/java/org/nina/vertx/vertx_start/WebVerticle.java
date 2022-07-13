package org.nina.vertx.vertx_start;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * 描述：TODO
 * 作者：zgc
 * 时间：2022/7/13 08:31
 */
public class WebVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) throws Exception {
        configureRouter()
            .compose(this::startHttpServer)
            .onComplete(start::handle);

    }

    Future<Void> startHttpServer(Router router) {
        JsonObject http = config().getJsonObject("http");
        int httpPort = http.getInteger("port");
        HttpServer httpServer = vertx.createHttpServer().requestHandler(router);
        return Future.future(promise -> httpServer.listen(httpPort)).mapEmpty();
    }


    Future<Router> configureRouter() {
        Router router = Router.router(vertx);

        //SessionStore store = ClusteredSessionStore.create(vertx);
        SessionStore store = LocalSessionStore.create(vertx);
        router.route().handler(SessionHandler.create(store));
        router.route().handler(CorsHandler.create("localhost"));
        //router.route().handler(CSRFHandler.create("secret"));
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);
        router.route().handler(StaticHandler.create("web"));

        return Future.succeededFuture(router);
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
