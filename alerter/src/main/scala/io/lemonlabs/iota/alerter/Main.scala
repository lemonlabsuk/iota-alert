package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream._
import akka.stream.scaladsl.Sink
import io.lemonlabs.iota.alerter.email.EmailSender
import io.lemonlabs.iota.alerter.feed.BloxTangleWebSocket
import io.lemonlabs.iota.alerter.subscribe.DynamoDbSubscriber
import io.lemonlabs.rest.RestApi

import scala.util.{Failure, Success}

object Main extends App {

  val decider: Supervision.Decider = t => {
    t.printStackTrace()
    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  import system.dispatcher

  val tangleUpdates = new BloxTangleWebSocket
  val dynamoDbSubscriber = new DynamoDbSubscriber
  val emailSender = new EmailSender

  val restApi = new RestApi(dynamoDbSubscriber)
  val bindingFuture = Http().bindAndHandle(restApi.route, "0.0.0.0", 8080)

  bindingFuture.foreach { binding =>
    println(s"Bound to ${binding.localAddress}")
  }

  tangleUpdates.tangleUpdatesFeed
    .flatMapConcat(dynamoDbSubscriber.findSubscriptionsForTangleUpdate)
    .via(emailSender.emailFlow)
    .to(Sink.foreach {
      case Success(res) => println("Email successfully sent")
      case Failure(ex) => ex.printStackTrace()
    })
    .run()
}
