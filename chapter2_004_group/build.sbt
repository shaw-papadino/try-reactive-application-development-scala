name := "Guidebook-Tourist_TwoJvm"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,
  "ch.qos.logback"    %  "logback-classic"             % "1.2.3",
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
)
