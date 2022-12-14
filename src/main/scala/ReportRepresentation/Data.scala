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
import scala.util.{Success, Try}

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

  def columns: List[String]

  def toCSVFormat: String = columns
    .map { this.getClass.getDeclaredField }
    .map(x => {
      x.setAccessible(true)
      x.get(this)
    })
    .map {
      case None              => ""
      case Some(str: String) => f""""${str.replaceAll("\"", "'")}""""
      case str @ _: String   => f""""${str.replaceAll("\"", "'")}""""
      case value @ _         => f"$value"
    }
    .mkString(",")

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
) extends Data {
  def columns: List[String] = Report.columns
}

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

  // Must match the same order has the case class fields definition
  // This is a solution to the problem induced by _.getClass.getDeclaredFields which is unordered
  def columns: List[String] = List(
    "date",
    "city",
    "state",
    "country",
    "shape",
    "duration",
    "summary",
    "posted",
    "hasImages"
  )
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
) extends Data {
  def columns: List[String] = ReportEnhanced.columns
}

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
      state = line("state").toLowerCase()
      city = line("city").toLowerCase()
      key = f"$state-$city"
    } yield (key -> line)).toMap
  }

  private def getData(report: Report) = content(
    // TODO
    f"${report.state.toLowerCase()}-${report.city.toLowerCase()}"
  )

  def fromReport(report: Report): ReportEnhanced = {
    val info = Try(getData(report))
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
      latitude = info.toOption map (_("latitude")),
      longitude = info.toOption map (_("longitude"))
    )
  }

  // Must match the same order has the case class fields definition
  def columns: List[String] = Report.columns ::: List("latitude", "longitude")

}
