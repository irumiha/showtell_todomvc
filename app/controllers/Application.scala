package controllers

import play.api._
import play.api.db.slick._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  def index = Action { implicit rs =>
    Ok(views.html.index())
  }

}