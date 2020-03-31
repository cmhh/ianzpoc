package models

import java.io.File
import scala.io.Source

/**
 * Bespoke CSV representation.
 * Stores contents of a CSV file as a vector of String arrays.
 */
case class CSV(header: Array[String], body: IndexedSeq[Array[String]]) {
  require(body.map(_.size).distinct.size == 1)
  require(header.size == body(0).size)

  val nrow = body.size
  val ncol = header.size 

  def apply(name: String): IndexedSeq[String] = {
    val pos = header.indexOf(name)
    if (pos == -1) Vector[String]()
    else
     body.map(row => row(pos))
  }
}

case object CSV {
  def apply(source: Source): CSV = {
    val content = parseLines(source.getLines.toVector)
    new CSV(content.head, content.tail)
  }

  def apply(file: File): CSV = CSV(Source.fromFile(file))

  def apply(file: String): CSV = CSV(new File(file))

  private def parseLines(s: IndexedSeq[String]): IndexedSeq[Array[String]] = 
    s.map((line: String) => parseLine(line))

  private def parseLine(s: String): Array[String] = {
    def parse_(s: List[Char], numquote: Int, buffer: String, accum: Array[String]): Array[String] = s match {
      case Nil => if (buffer == "") accum else accum :+ buffer
      case h::t => {
        val inside = numquote % 2 != 0
        if (h == 65279)
          parse_(t, numquote, buffer, accum)/*
        else if (h == '\\' & t.head == '"') 
          parse_(t.tail, numquote, buffer + '"', accum)*/
        else if (h == ',' & !inside)
          parse_(t, numquote, "", accum :+ buffer)
        else if (h == '"')
          parse_(t, numquote + 1, buffer, accum)
        else 
          parse_(t, numquote, buffer + h, accum) 
      }
    }
    parse_(s.toList, 0, "", Array[String]())
  }
}