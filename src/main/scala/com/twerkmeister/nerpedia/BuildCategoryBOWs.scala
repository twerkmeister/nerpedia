package com.twerkmeister.nerpedia

import java.io.{PrintWriter, StringWriter, File}

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import org.rogach.scallop._
import com.scalableminds.wikistreamer.parser.{TextWithLinksRevisionBuilderFactory, DefaultRevisionBuilderFactory, WikiPage, WikiXmlPullParser}
import akka.stream._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.matching.Regex

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val categories = opt[List[String]](required = true)
  val wikifile = trailArg[String](required = true)
}

case class CategoryIndex(name: String) {
  private val outputDirectoryName = "categoryIndices"
  private val outputDirectory = new File(outputDirectoryName)

  if(!outputDirectory.exists)
    outputDirectory.mkdir()

  private val outputFile = new File(s"$outputDirectoryName/$name.idx")
  private val out = new PrintWriter(outputFile)

  def write(title: String) = out.write(title + "\n")

  def close = {
    out.flush()
    out.close()
  }
}

object BuildCategoryIndices {
  def main(args: Array[String]) = {
    val conf = new Conf(args)
    implicit val system = ActorSystem()
    implicit val mat = ActorFlowMaterializer()


    for {wikifileName <- conf.wikifile
         categories <- conf.categories} {
      val categorySets: Map[String, scala.collection.mutable.Set[String]] = categories.map { cat => cat -> scala.collection.mutable.Set.empty[String]}.toMap
      val parser = new WikiXmlPullParser(new DefaultRevisionBuilderFactory)
      val wikiPages = Source(parser.parse(wikifileName))
      val filteredPages = wikiPages
        .filter { p => categories.find(cat => p.revision.categories.contains(cat)).isDefined}
      val createDict = filteredPages.runForeach(p =>
        p.revision.categories.find(cat => categories.contains(cat)).map { cat =>
          categorySets(cat).add(p.title)
        }
      )
      createDict.onComplete { case _ =>
        categorySets.foreach {
          case (cat, titles) =>
            println(cat)
            println("=" * cat.size)
            println(titles.mkString(", "))
        }

        val textWithLinksParser = new WikiXmlPullParser(new TextWithLinksRevisionBuilderFactory)
        val wikiPages = Source(parser.parse(wikifileName))
        val linkRegex = """\[\[(.*?)\]\]""".r
        val namedLinkRegex = """\[\[([^\[\]]*?)\|(.*?)\]\]""".r
        val sentences = wikiPages.mapConcat[String] { p =>
          p.revision.sections.values.toVector
        }.mapConcat[String] { s =>
          s.split('.').toVector
        }.map{s =>
          linkRegex.findAllMatchIn(s).foldLeft(s) { (s: String, m: Regex.Match) =>
            categorySets.find { case (cat, set) => set.contains(m.group(1))} match {
              case Some((cat, set)) => s.replace(m.group(0), s"<<$cat:${m.group(1)}>>")
              case _ => s
            }
          }
        }.filter{s => s.containsSlice("<<")}

        val output = sentences.runForeach(s =>
          println(s)
        )

        output.onComplete { case _ =>
          system.shutdown()
        }
      }
    }


  }
}
