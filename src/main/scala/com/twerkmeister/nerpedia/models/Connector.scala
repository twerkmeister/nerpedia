package com.twerkmeister.nerpedia.models

import com.websudos.phantom.connectors.ContactPoints
import com.websudos.phantom.dsl._

trait Keyspace {
  implicit val space: KeySpace = new KeySpace("phantom_demo")
}

object Defaults extends Keyspace {

  val hosts = Seq("127.0.0.1")


  val Connector = ContactPoints(hosts).keySpace(space.name)
}

trait Connector extends Defaults.Connector.Connector with Keyspace