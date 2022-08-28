package ReportRepresentation

import ReportRepresentation.Report

import scala.reflect.ClassTag

// Meant to be all the reports contained on a single page of the NUFORC website
// For instance, all lines from https://nuforc.org/webreports/ndxp220622.html
sealed case class Table[A <: Data](
    fields: List[A]
) {

  def columns: List[String] = {
    // FIXME :
    // 1. We cannot get the header if the table is empty...
    //    objectOf[A] not working,  match { case _: List[Report] ... } cannot be checked at runtime...
    // 2. Also there is no guarantee the fields will be ordered as their declaration is
    fields.headOption.map(_.getClass.getDeclaredFields.toList.map(_.getName)).getOrElse(Nil)
  }

  def toCSVFormat: String =
    columns.mkString(",") + "\n" + fields.map(_.toCSVFormat).mkString("\n")

  def length: Int = fields.length

  def map(f: A => _ <: Data): Table[_ <: Data] = this.copy(fields = fields.map(f))

  def flatMap(f: A => List[A]): Table[A] = new Table(fields.flatMap(f))

  def filter(f: A => Boolean): Table[A] = new Table(fields.filter(f))

}

object Table {
  def getEmpty: Table[Report] = new Table(Nil)
}
