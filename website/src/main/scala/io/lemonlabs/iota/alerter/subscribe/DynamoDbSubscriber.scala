package io.lemonlabs.iota.alerter.subscribe

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.dynamodb.impl.DynamoSettings
import akka.stream.alpakka.dynamodb.scaladsl.{DynamoClient, DynamoImplicits}
import akka.stream.scaladsl.{Sink, Source}
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._
import scala.concurrent.Future

class DynamoDbSubscriber()(implicit system: ActorSystem, materializer: Materializer) {

  import DynamoImplicits._
  implicit val ec = system.dispatcher

  val settings = DynamoSettings(system)
  val client = DynamoClient(settings)

  val tableName = "iota-alert-subscriptions"

  def insertSubscription(subscription: AlertSubscription): Future[PutItemResult] =
    Source.single(new PutItemRequest()
      .withTableName(tableName)
      .withItem(Map(
        "IotaAddress" -> new AttributeValue(subscription.iotaAddress),
        "Email" -> new AttributeValue(subscription.email)
      ).asJava)
      .toOp
    )
    .via(client.flow)
    .runWith(Sink.head)
}
