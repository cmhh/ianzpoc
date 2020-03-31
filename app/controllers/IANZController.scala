package controllers

import models._

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * 
 */
@Singleton
class IANZController @Inject()(val data: Data, val controllerComponents: ControllerComponents) extends BaseController {

  def index() = Action{ implicit request: Request[AnyContent] =>
    Ok(views.html.index(data))
  }

  def indicator(name: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.indicator(name)(data))
  }

  def title(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.config(data.ids(id)).name)
  }

  def description(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.config(data.ids(id)).description).as("text/html")
  }
  
  def image(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok("<img src=\"" + data.images(data.ids(id)) + "\">").as("text/html")
  }

  def metadata(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.metadata(data.ids(id)).toString).as("application/json")
  }

  def dataset(id: Int) = Action { implicit request: Request[AnyContent] =>
    Ok(data.datasets(data.ids(id)).toCSV).as("text/csv")
  }
}
