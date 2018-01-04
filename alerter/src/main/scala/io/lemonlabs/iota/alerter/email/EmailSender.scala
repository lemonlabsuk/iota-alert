package io.lemonlabs.iota.alerter.email

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.ses.scaladsl.SesPublisher
import akka.stream.scaladsl.Flow
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsync, AmazonSimpleEmailServiceAsyncClientBuilder}
import io.lemonlabs.iota.alerter.feed.TangleUpdate

import scala.util.{Failure, Success, Try}

case class EmailAlert(email: String, tangleUpdate: TangleUpdate)

class EmailSender()(implicit system: ActorSystem, materializer: Materializer) {

  implicit val sesClient: AmazonSimpleEmailServiceAsync =
    AmazonSimpleEmailServiceAsyncClientBuilder.defaultClient()

  type EmailFlow = Flow[EmailAlert, Try[SendEmailResult], NotUsed]

  val emailFlow: EmailFlow =     emailFlow(subject = tu => s"${tu.value} IOTA received")
  val smokeTestFlow: EmailFlow = emailFlow(subject = _  => s"IOTA Alerter Successfully Deployed!")

  def emailFlow(subject: TangleUpdate => String): EmailFlow =
    Flow[EmailAlert].map { case EmailAlert(email, tangleUpdate) =>
      new SendEmailRequest(
        "iota@lemonlabs.io",
        new Destination().withToAddresses(email),
        new Message(
          new Content(subject(tangleUpdate)),
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
