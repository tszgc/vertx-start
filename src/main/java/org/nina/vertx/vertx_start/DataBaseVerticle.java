package org.nina.vertx.vertx_start;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

/**
 * 描述：TODO
 * 作者：zgc
 * 时间：2022/7/13 08:24
 */
public class DataBaseVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) throws Exception {
        doDataBaseMigrations()
            .onComplete(start::handle);
    }

    Future<Void> doDataBaseMigrations() {
        JsonObject dbConfig = config().getJsonObject("db");
        String url = dbConfig.getString("url", "jdbc:postgresql://127.0.0.1:5432/todo");
        String adminUser = dbConfig.getString("admin_user", "postgres");
        String adminPass = dbConfig.getString("admin_pass", "introduction");
        Flyway flyway = Flyway.configure().dataSource(url, adminUser, adminPass).load();
        try {
            flyway.migrate();
            return Future.succeededFuture();
        } catch (FlywayException fe) {
            return Future.failedFuture(fe);
        }
    }
}
