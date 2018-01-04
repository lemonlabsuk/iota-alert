package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream._
import io.lemonlabs.iota.alerter.rest.RestApi
import io.lemonlabs.iota.alerter.subscribe.DynamoDbSubscriber

object Main extends App {

  val decider: Supervision.Decider = t => {
    t.printStackTrace()
    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  import system.dispatcher

  val dynamoDbSubscriber = new DynamoDbSubscriber

  val restApi = new RestApi(dynamoDbSubscriber)
  val bindingFuture = Http().bindAndHandle(restApi.route, "0.0.0.0", 8080)

  bindingFuture.foreach { binding =>
    println(s"Bound to ${binding.localAddress}")
  }
}
