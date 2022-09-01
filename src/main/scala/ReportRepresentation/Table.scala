package ReportRepresentation

import ReportRepresentation.Report

import scala.reflect.ClassTag

// Meant to be all the reports contained on a single page of the NUFORC website
// For instance, all lines from https://nuforc.org/webreports/ndxp220622.html
sealed case class Table[A <: Data](
    fields: List[A]
) {

  def columns: List[String] = if (List[Data] == List[A]) Report.columns else ReportEnhanced.columns

  def toCSVFormat: String =
    columns.mkString(",") + "\n" + fields.map(_.toCSVFormat).mkString("\n")

  def length: Int = fields.length

  def map[B <: Data](f: A => B): Table[B] = this.copy(fields = fields.map(f))

  def flatMap(f: A => List[A]): Table[A] = new Table(fields.flatMap(f))

  def filter(f: A => Boolean): Table[A] = new Table(fields.filter(f))

}

object Table {
  def getEmpty: Table[Report] = new Table(Nil)
}
