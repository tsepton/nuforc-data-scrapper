import com.github.nscala_time.time.Imports._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupElement
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*

case class Report(
    date: String, // TODO : DateTime
    city: String,
    state: String,
    country: String,
    shape: Option[String],
    duration: Option[String],
    summary: Option[String],
    posted: String, // TODO : DateTime
    hasImages: Boolean
)

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
            shape: Some[String],
            duration: Some[String],
            summary: Some[String],
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
