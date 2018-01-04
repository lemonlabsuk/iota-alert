package io.lemonlabs.iota.alerter.email

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.ses.scaladsl.SesPublisher
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsync, AmazonSimpleEmailServiceAsyncClientBuilder}
import io.lemonlabs.iota.alerter.feed.TangleUpdate
import io.lemonlabs.iota.alerter.subscribe.AlertSubscription

import scala.util.{Failure, Success}

case class EmailAlert(subscription: AlertSubscription, tangleUpdate: TangleUpdate) {
  require (
    subscription.iotaAddress == tangleUpdate.address,
    s"Cannot send alert, subscription is for ${subscription.iotaAddress}, not ${tangleUpdate.address}"
  )
}

class EmailSender()(implicit system: ActorSystem, materializer: Materializer) {

  implicit val sesClient: AmazonSimpleEmailServiceAsync =
    AmazonSimpleEmailServiceAsyncClientBuilder.defaultClient()

  //: Sink[EmailAlert, NotUsed]
  val emailFlow =
    Flow[EmailAlert].map { case EmailAlert(sub, tangleUpdate) =>
      println("Emailing " + sub.email)
      new SendEmailRequest(
        "iota@lemonlabs.io",
        new Destination().withToAddresses(sub.email),
        new Message(
          new Content(s"${tangleUpdate.value} IOTA received"),
          new Body()
            .withHtml(new Content(html.tangle_update_email(tangleUpdate).toString))
            .withText(new Content(txt.tangle_update_email(tangleUpdate).toString))
        )
      )
    }
    .via(SesPublisher.flow)
    .map(Success.apply)
    .recover({
      case t: Throwable => Failure(t)
    })
}
