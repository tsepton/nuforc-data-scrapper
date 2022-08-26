package ReportRepresentation

import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.text

import scala.io.Source
import scala.util.Try

sealed trait Data {
  def date: String // Not using datetime because reports do not use standard formats (e.g. : "Several we 22:00")
  def city: String
  def state: String
  def country: String
  def shape: Option[String]
  def duration: Option[String]
  def summary: Option[String]
  def posted: String
  def hasImages: Boolean

  def toCSVFormat: String = List(
    date,
    city,
    state,
    country,
    shape,
    duration,
    summary,
    posted,
    hasImages
  ).map {
    case None      => ""
    case Some(str) => f""""$str""""
    case str @ _   => f""""$str""""
  }.mkString(",")
}

// Meant to be a single report of the NUFORC website
// For instance, a line from the following link https://nuforc.org/webreports/ndxp220622.html
case class Report(
    date: String, // Not using datetime because reports do not use standard formats (e.g. : "Several we 22:00")
    city: String,
    state: String,
    country: String,
    shape: Option[String],
    duration: Option[String],
    summary: Option[String],
    posted: String,
    hasImages: Boolean
) extends Data

object Report {
  // Following the structure of a line in https://nuforc.org/webreports/ndxp220622.html
  // Consulted on 17.08.2022
  def fromJSoup(e: JsoupElement): Report = {
    val t = e.children.toList.zipWithIndex
      .map {
        case (str, 0) => (str >> text("a"))
        case (str, _) => str.innerHtml
      }
      .zipWithIndex
      .map {
        case x @ (_, 8) => if (x._1 == "Yes") true else false
        case x @ ((_, 4) | (_, 5) | (_, 6)) =>
          if (x._1.nonEmpty) Some(x._1) else None
        case x @ _ => x._1
      }
    t match {
      case List(
            date: String,
            city: String,
            state: String,
            country: String,
            shape: Option[String],
            duration: Option[String],
            summary: Option[String],
            posted: String,
            hasImages: Boolean
          ) =>
        Report(
          date,
          city,
          state,
          country,
          shape,
          duration,
          summary,
          posted,
          hasImages
        )
    }
  }
}

case class ReportEnhanced(
    date: String, // Not using datetime because reports do not use standard formats (e.g. : "Several we 22:00")
    city: String,
    state: String,
    country: String,
    shape: Option[String],
    duration: Option[String],
    summary: Option[String],
    posted: String,
    hasImages: Boolean,
    latitude: Option[String],
    longitude: Option[String]
) extends Data

object ReportEnhanced {

  // FIXME : https://stackoverflow.com/questions/4002343/how-to-write-a-class-destructor-in-scala
  // For doing this :  def atDestroy(): Unit = source.close()

  lazy val file: Iterator[Array[String]] = {
    val source = Source fromFile "./data/external/cities.csv"
    val data = source.getLines.map(_ split ",")
    data
  }

  lazy private val header: Array[String] = file.next

  lazy private val content = {
    header
    val iterator = file.map(header.zip(_).toMap)
    (for {
      line <- iterator
      key = f"""${line("country")}-${line("state")}-${"city"}"""
    } yield (key -> line)).toMap
  }

  private def getData(report: Report) = content(
    f"""${report.country}-${report.state}-${report.city}"""
  )

  def fromReport(report: Report): ReportEnhanced = {
    val latitude = Try(getData(report)("latitude"))
    val longitude = Try(getData(report)("longitude"))
    ReportEnhanced(
      date = report.date,
      city = report.city,
      state = report.state,
      country = report.country,
      shape = report.shape,
      duration = report.duration,
      summary = report.summary,
      posted = report.posted,
      hasImages = report.hasImages,
      latitude = latitude.toOption,
      longitude = longitude.toOption
    )
  }

}
