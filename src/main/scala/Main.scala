import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*
import net.sourceforge.htmlunit.cyberneko.HTMLElements.ElementList

@main def main(): Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()
  val scrappedData = allLinksToPages.zipWithIndex.map {
    case (page, 0) => downloadDataFromPage(page)
    case _         => Left(ReportsError)
  }.zipWithIndex.map {
    case (Right(data), _) => List(data)
    case (Left(error), i) =>
      println(f"Error (index $i): $error")
      Nil
  }.filter(_.nonEmpty).flatten
  // TODO save to CSV file
}

def allLinksToPages(using browser: JsoupBrowser): List[String] = {
  val dataByDateDoc = browser.get("https://nuforc.org/webreports/ndxevent.html")
  (dataByDateDoc >> elementList("tbody tr td a") >> attr("href")).map(x => f"https://nuforc.org/webreports/${x}")
}

def downloadDataFromPage(url: String)(using browser: JsoupBrowser): Either[ReportsError, Reports] = {
  val dataFromUrlDoc = browser.get(url)
  val columns: List[JsoupElement] = (dataFromUrlDoc >> elementList("thead tr th font")).map(_.asInstanceOf[JsoupElement])
  val data: List[JsoupElement] = (dataFromUrlDoc >> elementList("tbody tr")).map(_.asInstanceOf[JsoupElement]) // <tr> tds... </tr>
  Reports(columns.map(_.innerHtml), data.map(Report.fromJSoup))
}

def saveToCSV(): Unit = ???

