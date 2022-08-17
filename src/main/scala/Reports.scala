import ReportsError.NotEnoughColumnNames

sealed private case class Reports(
    columns: List[String],
    fields: List[Report]
)

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
}

sealed trait ReportsError

case object ReportsError {
  case object NotEnoughColumnNames extends ReportsError
}
