package com.example.starter.database

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import java.util.stream.Collectors


class WikiDatabaseServiceImpl(private val dbClient: JDBCClient, private val sqlQueries: Map<SqlQuery, String>, readyHandler: Handler<AsyncResult<WikiDatabaseService>>) : WikiDatabaseService {

  init {
    dbClient.getConnection { ar ->
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause())
        readyHandler.handle(Future.failedFuture(ar.cause()))
      } else {
        val connection: SQLConnection = ar.result()
        connection.execute(sqlQueries[SqlQuery.CREATE_PAGES_TABLE]) { create ->
          connection.close()
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause())
            println("Database preparation error"+ create.cause())
            readyHandler.handle(Future.failedFuture(create.cause()))
          } else {
            createPage("title1","content1", Handler {
              if(it.succeeded()){
                readyHandler.handle(Future.succeededFuture(this))
              }else{
                readyHandler.handle(Future.failedFuture(create.cause()))
              }
            })
//
            LOGGER.info("table created")
            println("table created")
          }
        }
      }
    }
  }

  companion object {
    private val LOGGER: Logger = LoggerFactory.getLogger(WikiDatabaseServiceImpl::class.java)
  }

  override fun fetchAllPages(resultHandler: Handler<AsyncResult<JsonArray>>): WikiDatabaseService {
    dbClient.query(sqlQueries[SqlQuery.ALL_PAGES]) { res: AsyncResult<ResultSet> ->
      if (res.succeeded()) {
        val pages = JsonArray(res.result()
          .results
          .stream()
          .map { json: JsonArray -> json.getString(0) }
          .sorted()
          .collect(Collectors.toList()) as MutableList<String>)
        resultHandler.handle(Future.succeededFuture(pages))
      } else {
        LOGGER.error("Database query error", res.cause())
        resultHandler.handle(Future.failedFuture(res.cause()))
      }
    }
    return this
  }

  override fun fetchPage(name: String, resultHandler: Handler<AsyncResult<JsonObject>>): WikiDatabaseService {
    TODO("Not yet implemented")
  }

  override fun createPage(title: String, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService {
    val data = JsonArray().add(title).add(markdown)
    dbClient.updateWithParams(sqlQueries[SqlQuery.CREATE_PAGE], data) { res: AsyncResult<UpdateResult?> ->
      if (res.succeeded()) {
        resultHandler.handle(Future.succeededFuture())
      } else {
        LOGGER.error("Database query error: ", res.cause())
        resultHandler.handle(Future.failedFuture(res.cause()))
      }
    }
    return this
  }

  override fun savePage(id: Int, markdown: String, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService {
    TODO("Not yet implemented")
  }

  override fun deletePage(id: Int, resultHandler: Handler<AsyncResult<Void>>): WikiDatabaseService {
    TODO("Not yet implemented")
  }

  override fun fetchAllPagesData(resultHandler: Handler<AsyncResult<List<JsonObject>>>): WikiDatabaseService {
    dbClient.query(sqlQueries[SqlQuery.ALL_PAGES_DATA]) { queryResult: AsyncResult<ResultSet> ->
      if (queryResult.succeeded()) {
        resultHandler.handle(Future.succeededFuture(queryResult.result().rows))
      } else {
        LOGGER.error("Database query error: ", queryResult.cause())
        resultHandler.handle(Future.failedFuture(queryResult.cause()))
      }
    }
    return this
  }

}
