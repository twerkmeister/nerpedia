package com.twerkmeister.nerpedia.models

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global._
import com.websudos.phantom.dsl._

case class Category(name: String, page: String)

class Categories extends CassandraTable[Categories, Category] {

  object name extends StringColumn(this) with PartitionKey[String]
  object page extends StringColumn(this) with PrimaryKey[String] with ClusteringOrder[String] with Ascending

  def fromRow(row: Row): Category = {
    Category(
      name(row),
      page(row)
    )
  }
}

object Categories extends Categories with Connector {
  Await.ready(Categories.create.ifNotExists().future(), 3 seconds)

  def store(category: Category): Future[ResultSet] = {
    insert
      .value(_.name, category.name)
      .value(_.page, category.page)
      .future()
  }

  def getByName(name: String): Future[List[Category]] = {
    select.where(_.name eqs name).fetch()
  }

  def getPages(name: String): Future[List[String]] = {
    getByName(name).map{categories => categories.map{_.page}}
  }

}