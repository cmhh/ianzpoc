package models

import java.io.File

/**
 * Bespoke data frame representation.
 *
 * Assumes a period column (Date), several columns for categorical variables (String), 
 * a column for values (Value), and an optional column for sampling errors (Value).
 */
case class DataSet(
  dates: Column[Date], values: Column[Value], 
  byvars: IndexedSeq[Column[String]], names: IndexedSeq[String],
  ses: Option[Column[Double]] = None,
) {
  require(byvars.size == names.size)

  val nrow = values.value.size

  def col(name: String): Column[String] = byvars(names.indexOf(name))

  /**
   * Use a Boolean sequence to subset--values are returned where the input sequence is true.
   */
  def subset(x: Seq[Boolean]): DataSet = {
    val newdates = dates.subset(x)
    val newvals = values.subset(x)
    val newbyvars = byvars.map(_.subset(x))
    val newses = ses match {
      case None => None
      case Some(y) => Some(y.subset(x))
    }
    new DataSet(newdates, newvals, newbyvars, names, newses)
  }

  /**
   * Drop all but named columns and return as a dataset.
   * Rows are only retained where dropped columns are 'totals'.
   */
  def keep(cols: IndexedSeq[String]): DataSet = {
    val drop = names.diff(cols)

    def getMask(drop: Seq[String], accum: Seq[Boolean]): Seq[Boolean] = {
      if (drop.size == 0) accum
      else {
        val h = drop.head
        val t = drop.tail
        val m = col(h).matches(x => x == "" | x.toLowerCase.matches("^(total){1}.*$"))
        getMask(t, if (accum.size == 0) m else and(accum, m))
      }
    }

    def keep_(cols: List[String], mask: Seq[Boolean], accum: IndexedSeq[Column[String]]): IndexedSeq[Column[String]] = cols match {
      case Nil => accum
      case h::t => {
        keep_(t, mask, accum :+ col(h).subset(mask))
      }
    }

    val mask = getMask(drop, (1 to nrow).toVector.map(i => true))
    val newbyvars = keep_(cols.toList, mask, IndexedSeq[Column[String]]())
    val newdates = dates.subset(mask)
    val newvals = values.subset(mask)
    ses match {
      case None => new DataSet(newdates, newvals, newbyvars, cols)
      case Some(x) => new DataSet(newdates, newvals, newbyvars, cols, Some(x.subset(mask)))
    }
  }

  def keep(col: String): DataSet = keep(Vector(col))

  private def and(x: Seq[Boolean], y: Seq[Boolean]): Seq[Boolean] = {
    require(x.size == y.size)
    x.zip(y).map(z => z._1 & z._2)
  }

  /**
   * CSV representation.
   */
  def toCSV: String =  {
    def row(i: Int): String = {
      val x = {
        Vector(dates.value(i).toString) ++
        names.map(name => col(name).value(i).toString) :+ 
        values.value(i).toString
      } ++
      { 
        ses match {
          case None => Nil
          case Some(x) => x.value(i).toString
        }
      }
      "\n" + x.mkString(",")
    }

    val hdr = {
      Vector("Period") ++ names :+ "value"
    } ++ 
    { 
      ses match {
        case None => Nil
        case Some(x) => "SE"
      }
    }

    val rows = (0 until nrow).map(i => row(i)).mkString

    hdr.mkString(",") + rows
  }

  /**
   * HTML representation.
   */
  def toHTML: String = {
    def row(i: Int): String = {
      val x = {
        Vector(dates.value(i).toString) ++
        names.map(name => col(name).value(i).toString) :+ 
        values.value(i).toString
      } ++
      { 
        ses match {
          case None => Nil
          case Some(x) => x.value(i).toString
        }
      }
      "<tr>" + x.map("<td>" + _ + "</td>").mkString + "</tr>"
    }

    val hdr = {
      Vector("Period") ++ names :+ "value"
    } ++ 
    { 
      ses match {
        case None => Nil
        case Some(x) => "SE"
      }
    }

    val rows = (0 until nrow).map(i => row(i)).mkString

    "<table>" + "<tr>" + hdr.map("<th>" + _ + "</th>").mkString + "</tr>" + rows + "</table>"
  }

  /**
   * Simple barplot (using Plotly).
   * A single byvar is selected, and Period is used to group.
   */
  def barplot(byvar: String, title: Option[String] = None): String = {
    val data = keep(byvar)
    val periods = data.dates.value.distinct
    val cats = data.col(byvar).value.distinct    
    var no = 0
    val trace = cats.map(cat => {
      no += 1
      val indices = data.col(byvar).matches(_ == cat)
      val sub = data.subset(indices)
      s"var trace$no = {x: [${sub.dates.value.map("'" + _ + "'").mkString(", ")}], " + 
        s"""y: [${sub.values.value.mkString(", ")}], name: "$cat", type: 'bar'};"""
    })
    val id = "p" + scala.util.Random.nextInt.toString
    val tt = title match {
      case None => ""
      case Some(t) => s"title: '$t', "
    }
    s"<div id='$id'></div><script>" +
      s"${trace.mkString}" + 
      s"""var data = [${(1 to no).map("trace" + _).mkString(", ")}];""" +
      s"var layout = {${tt}barmode: 'group', legend: {orientation: 'h'}, xaxis: {type: 'category'}};" +
      s"Plotly.newPlot('$id', data, layout);" +
      s"</script>" 
  }
}

case object DataSet {
  def apply(csv: CSV): DataSet = {
    val h = csv.header.map(_.toLowerCase.replace(" ", "_"))
    val datepos = h.indexOf("period")
    val vals = Vector("count", "value", "percentage", "estimate")
    val valpos = h.map(vals.contains(_)).indexOf(true)
    val sepos = h.indexOf("sampling_error")
    if (datepos == -1) sys.error("No date field found.")
    if (valpos == -1) sys.error("No value candidate found.")

    val bycols = 
      if (sepos == -1) csv.header.diff(Vector(csv.header(datepos), csv.header(valpos)))
      else csv.header.diff(Vector(csv.header(datepos), csv.header(valpos), csv.header(sepos)))
    
    val byvars = bycols.toVector
      .map(col => {
        new Column[String](csv(col))
      })

    val dates = new Column[Date](csv(csv.header(datepos)).map(Date(_)))
    val values = new Column[Value](csv(csv.header(valpos)).map(Value(_)))
    val ses: Option[Column[Double]] = 
      if (sepos == -1) None
      else Some(new Column[Double](csv(csv.header(sepos)).map(_.toDouble)))

    DataSet(dates, values, byvars, bycols.toVector, ses)
  }

  def apply(file: String): DataSet = DataSet(CSV(file))
  def apply(file: File): DataSet = DataSet(CSV(file))
}
