package ReportRepresentation

import ReportRepresentation.Report

// Meant to be all the reports contained on a single page of the NUFORC website
// For instance, all lines from https://nuforc.org/webreports/ndxp220622.html
sealed case class Table[A <: Data](
    columns: List[String],
    fields: List[A]
) {

  def toCSVFormat: String =
    columns.mkString(",") + "\n" + fields.map(_.toCSVFormat).mkString("\n")

  def length: Int = fields.length

  def map(f: A => _ <: Data): Table[_ <: Data] = this.copy(fields = fields.map(f))

  def flatMap(f: A => List[A]): Table[A] = new Table(columns, fields.flatMap(f))

  def filter(f: A => Boolean): Table[A] = new Table(columns, fields.filter(f))

}

object Table {
  def getEmpty: Table[Report] = new Table(Nil, Nil)
}
