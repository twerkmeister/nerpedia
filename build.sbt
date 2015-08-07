name := """nerpedia"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "1.0"

libraryDependencies += "org.rogach" %% "scallop" % "0.9.5"

libraryDependencies += "com.websudos" %% "phantom-dsl" % "1.10.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.1.3"

lazy val wikistreamer= RootProject(file("../wikistreamer"))

lazy val root = project.in(file(".")).dependsOn(wikistreamer)

