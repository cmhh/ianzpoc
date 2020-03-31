package models

case class Value(value: Option[Double]) {
  override def toString = value match {
    case None => ""
    case Some(x) => x.toString
  }
}

case object Value {
  def apply(value: String): Value = {
    if (value.toLowerCase == "na") new Value(None)
    else if (value == "") new Value(None)
    else new Value(Some(value.toDouble))
  }
}