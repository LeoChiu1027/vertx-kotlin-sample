package com.example.starter.database

import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.serviceproxy.ServiceBinder
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap


class WikiDatabaseVerticle: AbstractVerticle() {

  val CONFIG_WIKIDB_JDBC_URL = "wikidb.jdbc.url"
  val CONFIG_WIKIDB_JDBC_DRIVER_CLASS = "wikidb.jdbc.driver_class"
  val CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE = "wikidb.jdbc.max_pool_size"
  val CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE = "wikidb.sqlqueries.resource.file"
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue"


  override fun start() {
//    super.start(startPromise)

    val sqlQueries = loadSqlQueries()
    val dbClient: JDBCClient = JDBCClient.createShared(vertx, JsonObject()
//      .put("url", config().getString(CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:file:db/wiki"))
      .put("url", config().getString(CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:mem:test?shutdown=true"))
      .put("driver_class", config().getString(CONFIG_WIKIDB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 30)))

    WikiDatabaseServiceFactory.create(dbClient, sqlQueries, Handler {
      if (it.succeeded()) {
        val binder = ServiceBinder(vertx)
        binder.setAddress(CONFIG_WIKIDB_QUEUE).register(WikiDatabaseService::class.java, it.result())
//        if(startPromise.tryComplete()){
//          startPromise.complete()
//        }
      } else {
//        startPromise.fail(it.cause())
      }
    })
  }


  @Throws(IOException::class)
  private fun loadSqlQueries(): Map<SqlQuery, String> {
    val queriesFile = config().getString(CONFIG_WIKIDB_SQL_QUERIES_RESOURCE_FILE)
    val queriesInputStream: InputStream
    queriesInputStream = queriesFile?.let { FileInputStream(it) }
      ?: javaClass.getResourceAsStream("/db-queries.properties")
    val queriesProps = Properties()
    queriesProps.load(queriesInputStream)
    queriesInputStream.close()
    val sqlQueries: HashMap<SqlQuery, String> = HashMap()
    sqlQueries[SqlQuery.CREATE_PAGES_TABLE] = queriesProps.getProperty("create-pages-table")
    sqlQueries[SqlQuery.ALL_PAGES] = queriesProps.getProperty("all-pages")
    sqlQueries[SqlQuery.GET_PAGE] = queriesProps.getProperty("get-page")
    sqlQueries[SqlQuery.CREATE_PAGE] = queriesProps.getProperty("create-page")
    sqlQueries[SqlQuery.SAVE_PAGE] = queriesProps.getProperty("save-page")
    sqlQueries[SqlQuery.DELETE_PAGE] = queriesProps.getProperty("delete-page")
    sqlQueries[SqlQuery.ALL_PAGES_DATA] = queriesProps.getProperty("all-pages-data")
    return sqlQueries
  }
}
