package models

case class Date(
  year: Int, 
  period: Option[Int] = None, frequency: Option[Int] = None, 
  month: Option[Int] = None, day: Option[Int] = None
) {
  override def toString: String = year.toString // just for now
}

case object Date {
  def apply(value: String): Date = {
    if (value.matches("^\\d{4}$")) new Date(value.toInt, None)
    else if (value.matches("^\\d{4}(-|/)\\d{2}(-|/)\\d{2}$")) {
      val parts = value.replace("/","-").split('-').map(_.toInt)
      new Date(parts(0), month = Some(parts(1)), day = Some(parts(2)))
    }
    else if (value.matches("^(P)\\d{1,2}(Y|Q|M)(/)\\d{4}$")) {
      val year = value.takeRight(4).toInt
      val frequency = value.dropRight(5).takeRight(1) match {
        case "M" => 12
        case "Q" => 4
        case "Y" => 1
        case _ => sys.error("Invalid frequency.")
      }      
      val period = value.dropRight(6).drop(1).toInt
      new Date(year, period = Some(period), frequency = Some(frequency))
    }
    else sys.error("Unknown date pattern.")
  }
}