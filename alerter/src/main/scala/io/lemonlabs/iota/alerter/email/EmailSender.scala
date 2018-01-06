package io.lemonlabs.iota.alerter.email

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.ses.scaladsl.SesPublisher
import akka.stream.scaladsl.Flow
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsync, AmazonSimpleEmailServiceAsyncClientBuilder}
import io.lemonlabs.iota.alerter.feed.Transaction

import scala.util.{Failure, Success, Try}

case class EmailAlert(email: String, tangleUpdate: Transaction)

class EmailSender()(implicit system: ActorSystem, materializer: Materializer) {

  implicit val sesClient: AmazonSimpleEmailServiceAsync =
    AmazonSimpleEmailServiceAsyncClientBuilder.defaultClient()

  type EmailFlow = Flow[EmailAlert, Try[SendEmailResult], NotUsed]

  val emailFlow: EmailFlow =
    Flow[EmailAlert].map { case EmailAlert(email, tangleUpdate) =>
      println("Sending email for " + tangleUpdate.address)
      new SendEmailRequest(
        "iota@lemonlabs.io",
        new Destination().withToAddresses(email),
        new Message(
          new Content(tangleUpdate.emailSubject.getOrElse(s"${tangleUpdate.value} IOTA received")),
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
