package controllers

import models._

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * App controller.
 */
@Singleton
class IANZController @Inject()(val data: Data, val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Main / landing page.
   */
  def index() = Action{ implicit request: Request[AnyContent] =>
    Ok(views.html.index(data))
  }

  /**
   * Indicator template--takes an indicator name and renders content as appropriate.
   */
  def indicator(name: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.indicator(name)(data))
  }

  /**
   * Return the set of ids and names.
   */
  def ids = Action { implicit request: Request[AnyContent] =>
    val ids = data.ids.keys.toVector.sorted
    val names = ids.map(id => {
      s""""${data.ids(id)}""""
    })
    Ok(s"""{"id":[${ids.mkString(",")}],"names":[${names.mkString(",")}]}""").as("application/json")
  }

  /**
   * Take id and return indicator name.
   */
  def title(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.config(data.ids(id)).name)
  }

  /**
   * Take id and return indicator description.
   */
  def description(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.config(data.ids(id)).description).as("text/html")
  }

  /**
   * Take id and return indicator image as base64 encoded string.
   */
  def image(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok("<img src=\"" + data.images(data.ids(id)) + "\">").as("text/html")
  }

  /**
   * Take id and return metadata for indicator.
   */
  def metadata(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.metadata(data.ids(id)).toString).as("application/json")
  }

  /**
   * Take id and return config for indicator.
   */
  def config(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.config(data.ids(id)).toString).as("application/json")
  }

  /**
   * Take id and return CSV data for indicator.
   */
  def dataset(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.datasets(data.ids(id)).toCSV).as("text/csv")
  }
}
