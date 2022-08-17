import com.github.nscala_time.time.Imports.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import net.sourceforge.htmlunit.cyberneko.HTMLElements.ElementList

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

@main def main(): Unit = downloadAndSaveReports()

private def downloadAndSaveReports(): Unit = {
  import scala.language.implicitConversions
  given JsoupBrowser()

  // Scrapping data
  beautifyPrintln("Scrapping...")
  val reports = Scrapper.getReports
  println("done.")

  // Saving data
  beautifyPrintln("Saving data...")
  Files.write(Paths.get("./data.csv"), reports.toCSVFormat.getBytes(StandardCharsets.UTF_8))
  println("done.")
}

private def beautifyPrintln(str: String): Unit = println(
  f"""
      |${"-" * (str.length * 2 + 2)}
      ||${" " * (str.length / 2) + str + " " * (str.length / 2)}|
      |${"-" * (str.length * 2 + 2)}""".stripMargin
)
