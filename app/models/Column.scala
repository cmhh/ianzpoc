package models

class Column[T](val value: IndexedSeq[T]) {
  def filter(f: T => Boolean): Column[T] = 
    new Column(value.filter(f))

  def matches(f: T => Boolean): Seq[Boolean] = 
    value.map(f)

  def eq(that: T): Column[T] = 
    new Column(value.filter(_ == that))

  def neq(that: T): Column[T] = 
    new Column(value.filter(_ != that))

  def subset(x: Seq[Boolean]): Column[T] = {
    require(x.size == value.size)
    new Column(value.zip(x).filter(_._2 == true).map(_._1))
  }
}

class NumericColumn(value: IndexedSeq[Double]) extends Column[Double](value)
class StringColumn(value: IndexedSeq[String]) extends Column[String](value)
class DateColumn(value: IndexedSeq[Date]) extends Column[Date](value)