import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*
import net.sourceforge.htmlunit.cyberneko.HTMLElements.ElementList

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

@main def main(): Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()
  beautifyPrintln("Scrapping...")
  val nonConcatenatedRecords = allLinksToPages.map(downloadDataFromPage).zipWithIndex.map {
    case (Right(data), _) => List(data)
    case (Left(error), i) =>
      println(f"Error (index $i): $error")
      Nil
  }.filter(_.nonEmpty).flatten
  val concatenatedRecords = nonConcatenatedRecords.foldLeft(Reports.getEmpty) {
    (acc, report) => acc.copy(report.columns, acc.fields ++ report.fields)
  }
  beautifyPrintln("Saving data...")
  Files.write(Paths.get("./data.csv"), concatenatedRecords.toCSVFormat.getBytes(StandardCharsets.UTF_8))
}

private def allLinksToPages(using browser: JsoupBrowser): List[String] = {
  val dataByDateDoc = browser.get("https://nuforc.org/webreports/ndxevent.html")
  (dataByDateDoc >> elementList("tbody tr td a") >> attr("href")).map(x => f"https://nuforc.org/webreports/${x}")
}

private def downloadDataFromPage(url: String)(using browser: JsoupBrowser): Either[ReportsError, Reports] = {
  val dataFromUrlDoc = browser.get(url)
  val columns: List[JsoupElement] = (dataFromUrlDoc >> elementList("thead tr th font")).map(_.asInstanceOf[JsoupElement])
  val data: List[JsoupElement] = (dataFromUrlDoc >> elementList("tbody tr")).map(_.asInstanceOf[JsoupElement]) // <tr> tds... </tr>
  Reports(columns.map(_.innerHtml), data.map(Report.fromJSoup))
}

private def beautifyPrintln(str: String): Unit = println(
    f"""
      |${"-" * (str.length*2 +2)}
      ||${" " * (str.length/2) + str + " " * (str.length/2)}|
      |${"-" * (str.length*2 +2)}
    """.stripMargin
  )
