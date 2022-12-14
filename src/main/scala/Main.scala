import ReportRepresentation.{Report, ReportEnhanced, Table, Data}
import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.sourceforge.htmlunit.cyberneko.HTMLElements.ElementList

import scala.concurrent.duration.Duration
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@main def main(): Unit = downloadAndSaveReports()

private def downloadAndSaveReports(): Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()

  beautifyPrintln("Scrapping...")
  val reports = Scrapper.getReports
  println(f"scrapped ${reports.length} reports")

  beautifyPrintln("Saving and cleaning...")
  val savingRawF = saveTable(reports, "./data/raw_data.csv")
  savingRawF.onComplete {
    case Failure(exception) => println(f"Could not save raw data: $exception")
    case Success(_)         => println("Raw data saved inside ./data/raw.csv")
  }
  val savingEnhancedF =
    Standardiser(reports)
      .map(_.map(rep => ReportEnhanced.fromReport(rep)))
      .map(standardized => saveTable(standardized, "./data/enhanced.csv"))
  savingEnhancedF.onComplete {
    case Failure(exception) => println(f"Could not save enhanced data: $exception")
    case Success(_)         => println("Enhanced data saved inside ./data/enhanced_data.csv")
  }
  Await.result(savingRawF zip savingEnhancedF, Duration.Inf)
}

private def saveTable[A <: Data](reports: Table[A], filename: String): Future[Unit] = Future {
  Files.write(Paths.get(filename), reports.toCSVFormat.getBytes(StandardCharsets.UTF_8))
}

private def beautifyPrintln(str: String): Unit = println(
  f"""
      |${"-" * (str.length * 2 + 2)}
      ||${" " * (str.length / 2) + str + " " * (str.length / 2)}|
      |${"-" * (str.length * 2 + 2)}""".stripMargin
)
