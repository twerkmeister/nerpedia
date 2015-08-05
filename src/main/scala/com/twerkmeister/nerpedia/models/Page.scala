package com.twerkmeister.nerpedia.models

import com.twerkmeister.nerpedia.models.Categories._
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.PrimitiveColumn
import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class Page(name: String, categories: Set[String])

class Pages extends CassandraTable[Pages, Page] {

  object name extends StringColumn(this) with PartitionKey[String]
  object categories extends SetColumn[Pages, Page, String](this)

  def fromRow(row: Row): Page = {
    Page(
      name(row),
      categories(row)
    )
  }
}

object Pages extends Pages with Connector {

  def store(page: Page): Future[ResultSet] = {
    insert
      .value(_.name, page.name)
      .value(_.categories, page.categories)
      .consistencyLevel_=(ConsistencyLevel.ALL)
      .future().flatMap { resultSet =>
        Future.traverse(page.categories) { category =>
          Categories.insert
            .value(_.name, category)
            .value(_.page, page.name)
            .future()
        }.flatMap{ _ =>
          Future.successful(resultSet)
        }
    }
  }

  def getByName(name: String): Future[Option[Page]] = {
    select.where(_.name eqs name).one()
  }

}
