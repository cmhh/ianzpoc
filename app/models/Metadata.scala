package models

import java.io.{ File }
import scala.io.Source

case class Metadata(
  name: String, source: String, url: String, agency: String,
  date: String, measurementType: String, measure: String, 
  period: String, frequency: String, available: String, 
  caveats: Array[String], notes: Array[String]
) {
  override def toString: String = {
    val caveatList = 
      if (caveats.size == 0) ""
      else caveats.map("\"" + _ + "\"").mkString(",")
    val noteList = 
      if (notes.size == 0) ""
      else notes.map("\"" + _ + "\"").mkString(",")
    s"""{"name":"$name","source":"$source","url":"$url","agency":"$agency","date":"$date",""" +
    s""""measurement_type":"$measurementType","measure":"$measure","period":"$period",""" +
    s""""frequency":"$frequency","available":"$available","caveats":[$caveatList],"notes":[$noteList]}"""
  }
}

case object Metadata {
  def apply(file: File): Metadata = {
    val csv1 = CSV(file)
    val csv2 = csv1.copy(
      header =  
        csv1
          .header
          .map(h => h.toLowerCase.replace(".", "_").replace(" ", "_"))
          .map(h => h match {
            case "note" => "notes"
            case "caveat" => "caveats"
            case _ => h
          })
    )
    
    val caveats = csv2("caveats") 
    val notes = csv2("notes")

    def f(x: Seq[String]) = if (x.size == 0) "" else x(0)

    Metadata(
      f(csv2("indicator")),
      f(csv2("source")),
      f(csv2("url")),
      f(csv2("source_agency")),
      f(csv2("date")),
      f(csv2("measurement_type")),
      f(csv2("measure")),
      f(csv2("iso_period")),
      f(csv2("frequency")),  
      f(csv2("data_available")),
      if (caveats.size == 0) Array[String]() else caveats(0).split('~').toArray,
      if (notes.size == 0) Array[String]() else notes(0).split('~').toArray
    )
  }
}