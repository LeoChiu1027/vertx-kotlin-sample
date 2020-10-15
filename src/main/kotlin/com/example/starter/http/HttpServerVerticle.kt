package com.example.starter.http

import com.example.starter.database.WikiDatabaseService
import com.example.starter.database.WikiDatabaseServiceFactory
import com.example.starter.database.WikiDatabaseServiceImpl
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.stream.Collectors


class HttpServerVerticle : AbstractVerticle() {

  companion object {
    private val LOGGER: Logger = io.vertx.core.logging.LoggerFactory.getLogger(WikiDatabaseServiceImpl::class.java)
  }

  val CONFIG_HTTP_SERVER_PORT = "http.server.port"
  val CONFIG_WIKIDB_QUEUE = "wikidb.queue"

  var dbService: WikiDatabaseService? = null

  override fun start() {
//    super.start(startPromise)
    val wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue")
    dbService = WikiDatabaseServiceFactory.createProxy(vertx, wikiDbQueue)

    val server = vertx.createHttpServer()
    // tag::apiRouter[]
    val apiRouter = Router.router(vertx)
    apiRouter["/pages"].handler { context: RoutingContext? -> apiRoot(context!!) }

    // end::apiRouter[]
    val portNumber: Int = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080) as Int
    server
      .requestHandler(apiRouter)
      .listen(portNumber) { ar: AsyncResult<HttpServer?> ->
        if (ar.succeeded()) {
          LOGGER.info("HTTP server running on port $portNumber")
//          if(startPromise.tryComplete()){
//            startPromise.complete()
//          }
        } else {
          LOGGER.error("Could not start a HTTP server", ar.cause())
//          startPromise.fail(ar.cause())
        }
      }
  }

  // end::apiGetPage[]
  private fun apiRoot(context: RoutingContext) {
    println("fetchAllPagesData~~~~~~~~~~~~~~~")
    dbService!!.fetchAllPagesData(Handler { reply ->
      val response = JsonObject()
      if (reply.succeeded()) {
        val pages: List<JsonObject> = reply.result()
          .stream()
          .map { obj ->
            JsonObject()
              .put("id", obj.getInteger("ID")) // <1>
              .put("name", obj.getString("NAME"))
              .put("content", obj.getString("CONTENT"))
          }
          .collect(Collectors.toList())
        response
          .put("success", true)
          .put("pages", pages) // <2>
        context.response().statusCode = 200
        context.response().putHeader("Content-Type", "application/json")
        context.response().end(response.encode()) // <3>
      } else {
        response
          .put("success", false)
          .put("error", reply.cause().message)
        context.response().statusCode = 500
        context.response().putHeader("Content-Type", "application/json")
        context.response().end(response.encode())
      }
    })
  }
}
