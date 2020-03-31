package models

import java.io.{ File }
import scala.io.Source

case class Config(
  name: String, description: String,
  factoidValue: String, factoidDescription: String
)

case object Config {
  def apply(file: File): Config = {
    val csv1 = CSV(file)
    val csv2 = csv1.copy(
      header =  
        csv1
          .header
          .map(h => h.toLowerCase.replace(".", "_").replace(" ", "_"))
    )

    def f(x: Seq[String]) = if (x.size == 0) "" else x(0)

    Config(
      f(csv2("title")),
      f(csv2("description")),
      f(csv2("factoid_value")),
      f(csv2("factoid_description"))
    )
  }
}