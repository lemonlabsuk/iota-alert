package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.Sink
import io.lemonlabs.iota.alerter.email.{EmailAlert, EmailSender}
import io.lemonlabs.iota.alerter.feed.BloxTangleWebSocket
import io.lemonlabs.iota.alerter.subscribe.DynamoDbSubscriber

import scala.util.{Failure, Success}

object Main extends App {

  val decider: Supervision.Decider = t => {
    t.printStackTrace()
    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val smokeTestEmail = "forsey@gmail.com"

  val tangleUpdates = new BloxTangleWebSocket
  val dynamoDbSubscriber = new DynamoDbSubscriber
  val emailSender = new EmailSender

  tangleUpdates.tangleUpdatesFeed
    .flatMapConcat(dynamoDbSubscriber.findSubscriptionsForTangleUpdate)
    .via(emailSender.emailFlow)
    .runWith(Sink.foreach {
      case Success(res) => println("Email successfully sent")
      case Failure(ex) => ex.printStackTrace()
    })

  // SmokeTest on start up
  tangleUpdates.tangleUpdatesFeed
    .take(1)
    .map(tangleUpdate => EmailAlert(smokeTestEmail, tangleUpdate))
    .via(emailSender.smokeTestFlow)
    .runWith(Sink.foreach {
      case Success(res) => println("Smoke Test successfully sent")
      case Failure(ex) => ex.printStackTrace()
    })
}
