import concurrent.ExecutionContext.Implicits.global

class StandardiserSuite extends munit.FunSuite {
  test("""Check city name standardiser""") {
    val cityReports = DataMockup.emptyReports.copy(
      fields = List(
        Report("1/1/97", "Ft worth", "Tx", "USA", None, None, None, "1/1/1997", false),
        Report("1/1/97", "st. worth", "Tx", "USA", None, None, None, "1/1/1997", false),
        Report("1/1/97", " mt. worth", "Tx", "USA", None, None, None, "1/1/1997", false),
        Report("1/1/97", "Ft. wORth ", "tX", "USA", None, None, None, "1/1/1997", false),
        Report("1/1/97", "sT. worth", "tx", "USA", None, None, None, "1/1/1997", false),
        Report("1/1/97", " (MT) worTh  ", "TexaS", "USA", None, None, None, "1/1/1997", false)
      )
    )
    for {
      standardised <- Standardiser.apply(cityReports)
      errors = standardised.fields.zipWithIndex
        .map {
          case (report, i) if (i % 3 == 0) => report.city.toLowerCase() == "fort worth"
          case (report, i) if (i % 3 == 1) => report.city.toLowerCase() == "saint worth"
          case (report, i) if (i % 3 == 2) => report.city.toLowerCase() == "mount worth"
          case _                           => false // Something isn't right
        }
      _ = assert(errors.foldRight(true)(_ && _))
    } yield ()
  }
}

object DataMockup {

  def emptyReports: Table = Table(
    columns = List(
      "date",
      "city",
      "state",
      "country",
      "shape",
      "duration",
      "summary",
      "posted",
      "hasImages"
    ),
    fields = Nil
  ).getOrElse(Table.getEmpty)

}
