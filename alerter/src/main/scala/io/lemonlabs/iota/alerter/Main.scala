package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import io.lemonlabs.iota.alerter.email.EmailSender
import io.lemonlabs.iota.alerter.feed.Transaction
import io.lemonlabs.iota.alerter.feed.impl.TheTangleWebSocket
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
  val smokeTestDummyUpdate = Transaction(
    "smoketest", "smoketest", 1, "smoketest", "smoketest", "smoketest", "smoketest",
    System.currentTimeMillis / 1000,
    Some("IOTA Alerter Successfully Deployed!")
  )

  //val tangleUpdates = new BloxTangleWebSocket
  val tangleUpdates = new TheTangleWebSocket
  val dynamoDbSubscriber = new DynamoDbSubscriber
  val emailSender = new EmailSender

  val done = Source.single(smokeTestDummyUpdate)
    .concat(tangleUpdates.tangleUpdatesFeed)
    .flatMapConcat(dynamoDbSubscriber.findSubscriptionsForTangleUpdate)
    .via(emailSender.emailFlow)
    .runWith(Sink.foreach {
      case Success(res) => println("Email successfully sent")
      case Failure(ex) => ex.printStackTrace()
    })

  import system.dispatcher
  done.foreach(_ => println("Done!"))
}
