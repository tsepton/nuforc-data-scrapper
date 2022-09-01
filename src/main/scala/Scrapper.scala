import ReportRepresentation.{Report, Table}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*

object Scrapper {

  def getReports(using browser: JsoupBrowser): Table[Report] = {
    val nonConcatenatedRecords = allPagesSortedPerDate
      .map(downloadReportsFromPage)
      .zipWithIndex
      .map {
        case (Right(data), _) => List(data)
        case (Left(error), i) =>
          println(f"Error (index $i): $error")
          Nil
      }
      .filter(_.nonEmpty)
      .flatten
    val concatenatedRecords: Table[Report] =
      nonConcatenatedRecords.foldLeft(Table.getEmpty) { (acc, report) =>
        acc.copy(fields = acc.fields ++ report.fields)
      }
    concatenatedRecords
  }

  private def allPagesSortedPerDate(using browser: JsoupBrowser): List[String] = {
    val dataByDateDoc = browser.get("https://nuforc.org/webreports/ndxevent.html")
    (dataByDateDoc >> elementList("tbody tr td a") >> attr("href")).map(x =>
      f"https://nuforc.org/webreports/${x}"
    )
  }

  private def downloadReportsFromPage(
      url: String
  )(using browser: JsoupBrowser): Either[ScrappingError, Table[Report]] = {
    val dataFromUrlDoc = browser.get(url)
    val columns: List[JsoupElement] =
      (dataFromUrlDoc >> elementList("thead tr th font")).map(_.asInstanceOf[JsoupElement])
    val data: List[JsoupElement] =
      (dataFromUrlDoc >> elementList("tbody tr")).map(_.asInstanceOf[JsoupElement])

    (columns.map(_.innerHtml), data.map(Report.fromJSoup)) match {
      case (header, data) if header.length == classOf[Report].getDeclaredFields.length =>
        Right(Table(data))
      case _ => Left(ScrappingError.WebsiteStructureHasChanged)
    }
  }
}

sealed trait ScrappingError

case object ScrappingError {
  // Nuforc website has probably changed and ReportRepresentation.Report case class doesn't reflect the new data structure
  case object WebsiteStructureHasChanged extends ScrappingError
}
