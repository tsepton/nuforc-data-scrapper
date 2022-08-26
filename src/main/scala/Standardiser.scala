import ReportRepresentation.{Report, Table}

import scala.concurrent.Future

object Standardiser {

  // TODO - tedious and boring work
  // Write the corresponding tests inside the StandardiserSuite.scala file

  private def removedParenthesis(str: String): String = str.replaceAll("[(.*)]", " ").trim

  private def normalisedParticle(str: String): String = {
    str
      .split(" +")
      .map {
        _.toLowerCase() match
          case "mt" | "mt." => "mount"
          case "st" | "st." => "saint"
          case "ft" | "ft." => "fort"
          case x: String    => x.toLowerCase()
      }
      .mkString(" ")
  }

  private def normalisedCityName(city: String): String =
    normalisedParticle(removedParenthesis(city)).replaceAll(" +", " ").trim

  private def normaliseState(state: String): String = ???

  private def normaliseCountry(country: String): String = ???

  private def normaliseShape(shape: String): String = ???

  def apply(reports: Table[Report]): Future[Table[Report]] = {
    import concurrent.ExecutionContext.Implicits.global

    Future {
      reports.map(report =>
        report
          .copy(
            city = normalisedCityName(report.city)
          )
      )
    }
  }

}
