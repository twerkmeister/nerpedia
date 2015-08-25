package com.twerkmeister.nerpedia.models

import com.websudos.phantom.connectors.{KeySpace, SimpleConnector}
import com.websudos.phantom.dsl._

trait Connector extends SimpleConnector {
  implicit val keySpace: KeySpace = new KeySpace("nerpedia")
}