name := "chapter4_001_messaging"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed"         % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"       % akkaVersion,
  "ch.qos.logback"    %  "logback-classic"             % "1.2.3",
)
