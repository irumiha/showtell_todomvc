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

  val todoForm = Form(
    mapping(
      "id" -> default(number, 0),
      "todo" -> text,
      "done" -> default(boolean, false)
    )(Todo.apply)(Todo.unapply)
  )

  def index = DBAction { implicit rs =>
    val todos = TodoSchema.todos.list.map{case (id, todo, done) => Todo(id, todo, done)}

    Ok(views.html.index(todos, rs.request.flash.get("errors").getOrElse("")))
  }

  def createTodo = DBAction { implicit rs =>
    todoForm.bindFromRequest().fold(
      frm => {
        Redirect(routes.Application.index()).flashing(
          "errors" -> frm.errors.map(_.message).mkString(",")
        )
      },
      newTodo => {
        TodoSchema.todos.map(t => (t.todo, t.done)) += (newTodo.todo, newTodo.done)
        Redirect(routes.Application.index())
      }
    )
  }

  def removeTodo(idx: Int) = DBAction { implicit rs =>
    TodoSchema.todos.filter(t => t.id === idx ).delete
    Redirect(routes.Application.index())
  }

  def flipTodoDone(idx: Int) = DBAction { implicit rs =>

    sqlu"update todo set done = not done where id= $idx".first

    Redirect(routes.Application.index())
  }
}