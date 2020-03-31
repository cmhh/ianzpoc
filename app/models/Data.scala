package models

import javax.inject._
import scala.io.Source  
import java.io.File
import play.api.Configuration

@Singleton
class Data @Inject() (conf: Configuration) {
  val (config, metadata, images, datasets) = load

  val ids: Map[Int, String] = 
    (1 to config.keys.size).zip(config.keys.toVector.sorted).toMap

  val names: Map[String, Int] = 
    ids.keys.map(key => (ids(key), key)).toMap

  def load: (Map[String, Config], Map[String, Metadata], Map[String, String], Map[String, DataSet]) = {
    val root = new File(conf.get[String]("appdata"))
    val files = root.listFiles.filter(_.isFile)    
    val indicators = files.map(_.getName).filter(_.matches("^.+(.config.csv){1}$"))
    
    val config = indicators.map(name => {
      val sname = name.replace(".config.csv", "")
      val pattern = "^.*(" + sname + ".config.csv){1}$"
      val f = files.filter(_.getName.matches(pattern))(0)
      (sname -> Config(f))
    }).toMap
    
    val metadata = indicators.map(name => {
      val sname = name.replace(".config.csv", "")
      val pattern = "^.*(" + sname + ".metadata.csv){1}$"
      val f = files.filter(_.getName.matches(pattern))(0)
      (sname -> Metadata(f))
    }).toMap
    
    val images = indicators.map(name => {
      val sname = name.replace(".config.csv", "")
      val pattern = "^.*(" + sname + ".jpg){1}$"
      val f = files.filter(_.getName.matches(pattern))(0)
      (sname -> Image64(f))
    }).toMap

    val datasets = indicators.map(name => {
      val sname = name.replace(".config.csv", "")
      val pattern = "^.*(" + sname + ".csv){1}$"
      val f = files.filter(_.getName.matches(pattern))(0)
      (sname -> DataSet(f))
    }).toMap

    (config, metadata, images, datasets)
  }
}