package models

/**
 * Simple generic class to represent a column in a dataset.
 */
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