package ReportRepresentation

import ReportRepresentation.Report

// Meant to be all the reports contained on a single page of the NUFORC website
// For instance, all lines from https://nuforc.org/webreports/ndxp220622.html
sealed case class Table[A <: Data](
    fields: List[A]
) {

  def columns: List[String] = {
    // FIXME :
    // Because of type erasure, I came up with this solution
    // However, if fields.isEmpty, return value will be Nil
    if (fields.isEmpty) Nil
    else
      fields.head match {
        case _: ReportEnhanced => ReportEnhanced.columns
        case _                 => Report.columns
      }
  }

  def toCSVFormat: String =
    columns.mkString(",") + "\n" + fields.map(_.toCSVFormat).mkString("\n")

  def length: Int = fields.length

  def map[B <: Data](f: A => B): Table[B] = Table(fields.map(f))

  def flatMap(f: A => List[A]): Table[A] = Table(fields.flatMap(f))

  def filter(f: A => Boolean): Table[A] = Table(fields.filter(f))

}

object Table {
  def getEmpty: Table[Report] = Table(Nil)
}
