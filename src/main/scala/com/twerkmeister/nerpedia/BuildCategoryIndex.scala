package com.twerkmeister.nerpedia

import com.scalableminds.wikistreamer.parser.WikiXmlPullParser
import com.scalableminds.wikistreamer.transformers.extractors.CategoryExtractor
import com.twerkmeister.nerpedia.models.{Connector, Pages, Page}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import org.rogach.scallop._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val wikiFileName = trailArg[String](required = true)
}

object BuildCategoryIndex extends Connector {
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
      .mapAsyncUnordered(5){page => Future(CategoryExtractor(page))}
      .mapAsyncUnordered(10) { page =>
        page.revision.categories match {
          case Some(categories) =>
            Pages.store(Page(page.title, categories)).map{_ => page}
          case _ =>
            logger.error(s"failed to extract categories from ${page.title}")
            Future.failed(throw new IllegalStateException(s"Failed to extract categories from ${page.title}"))
        }
      }.runFold(0){ case (counter, _) =>
        if (counter % 1000 == 0)
          logger.error(s"Processed $counter pages. Took ${(System.currentTimeMillis() - start) / 1000}s")
        counter + 1
    }.andThen{
      case _ =>
        session.getCluster.close()
        sys.shutdown()
    }
  }
}
