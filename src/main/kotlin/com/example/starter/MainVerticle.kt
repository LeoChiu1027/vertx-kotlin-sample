package com.example.starter

//import io.vertx.core.Promise
import com.example.starter.database.WikiDatabaseVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise


class MainVerticle : AbstractVerticle() {

  override fun start() {
    val dbDeployPromise = Promise.promise<String>()
    vertx.deployVerticle(WikiDatabaseVerticle(), dbDeployPromise)
    val dbDeployFuture = dbDeployPromise.future()

    val deployHttpFuture = dbDeployFuture.compose { _ ->
      val deployHttpPromise = Promise.promise<String>()
      vertx.deployVerticle(
        "com.example.starter.http.HttpServerVerticle",
        DeploymentOptions().setInstances(2),
        deployHttpPromise)
      deployHttpPromise.future()
    }

    deployHttpFuture.onComplete { ar: AsyncResult<String> ->
      if (ar.succeeded()) {
//        if(startPromise.tryComplete()){
//          startPromise.complete()
//        }
      } else {
//        startPromise.fail(ar.cause())
      }
    }
  }
}
