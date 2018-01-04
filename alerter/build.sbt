name := "iota-alerter"

organization  := "io.lemonlabs"

version       := "1.0.0"

scalaVersion  := "2.12.4"

val akkaVersion = "2.5.7"
val akkaHttpVersion = "10.0.11"

libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion ::
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion ::
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion ::
    "com.typesafe.akka" %% "akka-stream" % akkaVersion ::
    "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "0.15+35-9736ee26+20180103-0048" ::
    "com.lightbend.akka" %% "akka-stream-alpakka-ses" % "0.15+35-9736ee26+20180103-0048" ::
    "io.spray" %%  "spray-json" % "1.3.4" :: Nil

// Test Dependencies
libraryDependencies ++=
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test ::
  "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test ::
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test ::
  "org.scalatest" %% "scalatest" % "3.0.4" % Test :: Nil

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
