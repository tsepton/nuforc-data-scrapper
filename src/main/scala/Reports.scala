import ReportsError.NotEnoughColumnNames

// Meant to be all the reports contained on a single page of the NUFORC website
// For instance, all lines from https://nuforc.org/webreports/ndxp220622.html
sealed private case class Reports(
    columns: List[String],
    fields: List[Report]
) {

  def toCSVFormat: String =
    columns.mkString(",") + "\n" + fields.map(_.toCSVFormat).mkString("\n")

  def length: Int = fields.length

  def map(f: Report => Any): List[Any] = fields.map(f)

  def flatMap(f: Report => List[Report]): Reports = new Reports(columns, fields.flatMap(f))

  def filter(f: Report => Boolean): Reports = new Reports(columns, fields.filter(f))

}

object Reports {
  def apply(
      columns: List[String],
      fields: List[Report]
  ): Either[ReportsError, Reports] = {
    if (classOf[Report].getDeclaredFields.length != columns.length)
      Left(NotEnoughColumnNames)
    else
      Right(new Reports(columns, fields))
  }

  def getEmpty: Reports = new Reports(Nil, Nil)
}

sealed trait ReportsError

case object ReportsError {
  case object NotEnoughColumnNames extends ReportsError
}
