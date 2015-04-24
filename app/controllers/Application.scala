package controllers

import play.api._
import play.api.db.slick._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.TodoSchema

import scala.slick.driver.H2Driver.simple._

import scala.slick.jdbc.{StaticQuery => Q}
import Q.interpolation

object Application extends Controller {
  case class Todo(id: Int, todo: String, done: Boolean)

  def index = DBAction { implicit rs =>
    val todos = TodoSchema.todos.list.map{case (id, todo, done) => Todo(id, todo, done)}

    Ok(views.html.index(todos))
  }

}