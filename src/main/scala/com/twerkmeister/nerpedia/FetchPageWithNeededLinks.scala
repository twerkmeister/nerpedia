package com.twerkmeister.nerpedia

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.scalableminds.wikistreamer.parser.WikiXmlPullParser
import com.scalableminds.wikistreamer.transformers.TextCleaner
import com.scalableminds.wikistreamer.transformers.extractors.CategoryExtractor
import com.scalableminds.wikistreamer.transformers.wikitextparser.{CategoryResolver, WikiTextParser}
import com.twerkmeister.nerpedia.BuildCategoryIndex._
import com.twerkmeister.nerpedia.models.{Page, Pages}
import org.slf4j.LoggerFactory

import scala.concurrent.Future


object FetchPageWithNeededLinks {
  def main(args: Array[String]): Unit = {
      val conf = new Conf(args)
      val logger = LoggerFactory.getLogger(this.getClass.getName)

      implicit val sys = ActorSystem("CategoryIndexBuilder")
      import sys.dispatcher
      implicit val materializer = ActorMaterializer()

      val parser = new WikiXmlPullParser
      val parsed = parser.parseToIterator(conf.wikiFileName())

      val start = System.currentTimeMillis()

      val source = Source(() => parsed)

      source
        .mapAsyncUnordered(10) {page => Future.successful(TextCleaner(page))}
        .mapAsyncUnordered(10) {page =>
          WikiTextParser(Set("Mann", "Frau"), categoryResolver, page)
      }.runFold(0){ case (counter, page) =>
        if (counter % 100 == 0) {
          logger.info(s"Processed $counter pages. Took ${(System.currentTimeMillis() - start) / 1000}s")
          logger.info(page.revision.text)
        }
        counter + 1
      }.andThen{
        case _ =>
          session.getCluster.close()
          sys.shutdown()
      }
    }
}

object categoryResolver extends CategoryResolver {
  import scala.concurrent.ExecutionContext.Implicits.global

  val categoryStore = Pages
  def categoriesForPage(title: String) = {
    categoryStore.getByName(title).map {
      case Some(page) => page.categories
      case _ => Set.empty[String]
    }
  }
}