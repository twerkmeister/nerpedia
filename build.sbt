name := """nerpedia"""

version := "1.0"

scalaVersion := "2.10.5"

resolvers += "jboss-m2proxy" at "https://repository.jboss.org/nexus/content/repositories/m2-proxy/"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-experimental" % "1.0"

libraryDependencies += "org.rogach" %% "scallop" % "0.9.5"

libraryDependencies += "com.websudos" %% "phantom-dsl" % "1.10.1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.1.3"

libraryDependencies += "org.deeplearning4j" % "deeplearning4j-nlp" % "0.4-rc0"

libraryDependencies += "org.nd4j" % "nd4j-jblas" % "0.4-rc0"

lazy val wikistreamer= RootProject(file("../wikistreamer"))

lazy val root = project.in(file(".")).dependsOn(wikistreamer)

