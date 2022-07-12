package org.nina.vertx.vertx_start;

import io.vertx.core.AbstractVerticle;

import java.util.UUID;

/**
 * 描述：hello verticle
 * 作者：zgc
 * 时间：2022/7/12 22:37
 */
public class HelloVerticle extends AbstractVerticle {

    String verticleId = UUID.randomUUID().toString();

    @Override
    public void start() {
        vertx.eventBus().consumer("hello.vertx.addr", msg -> {
            msg.reply("Hello Vert.x World");
        });

        vertx.eventBus().consumer("hello.named.addr", msg -> {
            String name = (String)msg.body();
            msg.reply(String.format("Hello %s, from %s!", name, verticleId));
        });
    }

}
