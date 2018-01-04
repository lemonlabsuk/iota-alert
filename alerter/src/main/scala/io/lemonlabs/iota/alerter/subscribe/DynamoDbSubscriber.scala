package io.lemonlabs.iota.alerter.subscribe

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.dynamodb.impl.DynamoSettings
import akka.stream.alpakka.dynamodb.scaladsl.{DynamoClient, DynamoImplicits}
import akka.stream.scaladsl.Source
import com.amazonaws.services.dynamodbv2.model._
import io.lemonlabs.iota.alerter.email.EmailAlert
import io.lemonlabs.iota.alerter.feed.TangleUpdate

import scala.collection.JavaConverters._

class DynamoDbSubscriber()(implicit system: ActorSystem, materializer: Materializer) {

  import DynamoImplicits._
  implicit val ec = system.dispatcher

  val settings = DynamoSettings(system)
  val client = DynamoClient(settings)

  val tableName = "iota-alert-subscriptions"

  def findSubscriptionsForTangleUpdate(tangleUpdate: TangleUpdate) =
    Source.single(new QueryRequest()
      .withTableName(tableName)
      .withConsistentRead(false)
      .withKeyConditionExpression("IotaAddress = :iotaAddress")
      .withExpressionAttributeValues(Map(":iotaAddress" -> new AttributeValue(tangleUpdate.address)).asJava)
      .toOp
    )
    .via(client.flow)
    .mapConcat(_.getItems.asScala.toVector)
    .map(result => EmailAlert(result.get("Email").getS, tangleUpdate))
    .recoverWithRetries(1, {
      case t: Throwable =>
        t.printStackTrace()
        Source.empty
    })
}
