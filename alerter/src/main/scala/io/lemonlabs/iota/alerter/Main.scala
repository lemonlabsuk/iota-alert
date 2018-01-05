package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import io.lemonlabs.iota.alerter.email.{EmailAlert, EmailSender}
import io.lemonlabs.iota.alerter.feed.{BloxTangleWebSocket, TangleUpdate}
import io.lemonlabs.iota.alerter.subscribe.DynamoDbSubscriber

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

object Main extends App {

  val decider: Supervision.Decider = t => {
    t.printStackTrace()
    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val smokeTestEmail = "forsey@gmail.com"
  val smokeTestDummyUpdate = TangleUpdate(
    "smoketest", "smoketest", 1, "smoketest",
    System.currentTimeMillis.toString,
    "1", "1",
    "smoketest", "smoketest", "smoketest",
    (System.currentTimeMillis / 1000).toString,
    Some("IOTA Alerter Successfully Deployed!")
  )

  val tangleUpdates = new BloxTangleWebSocket
  val dynamoDbSubscriber = new DynamoDbSubscriber
  val emailSender = new EmailSender

  Source.single(smokeTestDummyUpdate)
    .concat(tangleUpdates.tangleUpdatesFeed)
    .flatMapConcat(dynamoDbSubscriber.findSubscriptionsForTangleUpdate)
    .via(emailSender.emailFlow)
    .runWith(Sink.foreach {
      case Success(res) => println("Email successfully sent")
      case Failure(ex) => ex.printStackTrace()
    })
}
