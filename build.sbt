name := """nerpedia"""

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M3"

libraryDependencies += "org.rogach" %% "scallop" % "0.9.5"

libraryDependencies += "com.websudos" %% "phantom-dsl" % "1.10.1"

lazy val wikistreamer= RootProject(file("../wikistreamer"))

lazy val root = project.in(file(".")).dependsOn(wikistreamer)

