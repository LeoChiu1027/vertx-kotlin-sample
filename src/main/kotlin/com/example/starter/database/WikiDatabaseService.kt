package com.example.starter.database

import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient


@ProxyGen
interface WikiDatabaseService {

  @Fluent
  fun fetchAllPages(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseService

  @Fluent
  fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseService

  @Fluent
  fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService

  @Fluent
  fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService

  @Fluent
  fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService

  @Fluent
  fun fetchAllPagesData(resultHandler: Handler<AsyncResult<List<JsonObject>>>): WikiDatabaseService

}

@GenIgnore
object WikiDatabaseServiceFactory {
  @JvmStatic
  fun create(dbClient: JDBCClient, sqlQueries: Map<SqlQuery, String>,
             readyHandler: Handler<AsyncResult<WikiDatabaseService>>): WikiDatabaseService =
    WikiDatabaseServiceImpl(dbClient, sqlQueries, readyHandler)

  @JvmStatic
  fun createProxy(vertx: Vertx, address: String): WikiDatabaseService =
    WikiDatabaseServiceVertxEBProxy(vertx, address)
}

