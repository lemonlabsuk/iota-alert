package io.lemonlabs.iota.alerter

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.Future

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  import system.dispatcher

  val flow = new BloxTangleWebSocket().transactionUpdates()

  // print each incoming strict text message
  val printSink: Sink[TransactionUpdate, Future[Done]] =
    Sink.foreach(println)

  val upgradeResponse = flow.to(printSink).run()

  val connected = upgradeResponse.map { upgrade =>
    // just like a regular http request we can access response status which is available via upgrade.response.status
    // status code 101 (Switching Protocols) indicates that server support WebSockets
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Done
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  // in a real application you would not side effect here
  // and handle errors more carefully
  connected.onComplete(_ => println("Connected"))
}
