import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.sourceforge.htmlunit.cyberneko.HTMLElements.ElementList

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@main def main(): Unit = downloadAndSaveReports()

private def downloadAndSaveReports(): Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()

  beautifyPrintln("Scrapping...")
  val reports = Scrapper.getReports
  println(f"Scrapped ${reports.length} reports")

  for {
    report <- reports

    _ = beautifyPrintln("Saving and cleaning...")
    saving = saveStringToDisk(reports.toCSVFormat)
    _ = saving.onComplete {
      case Failure(exception) => println(f"Exception happened while saving raw data : $exception")
      case Success(_)         => println("Raw data saved")
    }
    _ = ??? // TODO cleaning
  } yield ()
}

private def saveStringToDisk(str: String): Future[Unit] = Future {
  Files.write(Paths.get("./raw_data.csv"), str.getBytes(StandardCharsets.UTF_8))
}

private def beautifyPrintln(str: String): Unit = println(
  f"""
      |${"-" * (str.length * 2 + 2)}
      ||${" " * (str.length / 2) + str + " " * (str.length / 2)}|
      |${"-" * (str.length * 2 + 2)}""".stripMargin
)
