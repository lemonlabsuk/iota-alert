package io.lemonlabs.iota.alerter.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.lemonlabs.iota.alerter.subscribe.{AlertSubscription, DynamoDbSubscriber}

class RestApi(dynamoDbSubscriber: DynamoDbSubscriber) extends SprayJsonSupport {

  val route: Route = extractActorSystem { implicit system =>
    import system.dispatcher

    post {
      path("alert-subscription") {
        entity(as[AlertSubscription]) { subscription =>
          complete {
            dynamoDbSubscriber.insertSubscription(subscription).map { result =>
              HttpResponse(status = StatusCodes.Created)
            }
          }
        }
      }
    }
  }
}
