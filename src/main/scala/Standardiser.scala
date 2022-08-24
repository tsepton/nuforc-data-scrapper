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

  private def normalisedCityName(report: Report): Report = report.copy(city =
    normalisedParticle(removedParenthesis(report.city)).replaceAll(" +", " ").trim
  )

  private def normaliseState(report: Report): Report = ???

  private def normaliseCountry(report: Report): Report = ???

  private def normaliseShape(report: Report): Report = ???

  def apply(reports: Table): Future[Table] = {
    import concurrent.ExecutionContext.Implicits.global

    Future { reports.map(normalisedCityName) }
  }

}
