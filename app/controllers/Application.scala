package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Todo

object Application extends Controller {

  var todos: List[Todo]= List()

  val todoForm = Form(
    mapping(
      "todo" -> text,
      "done" -> default(boolean, false)
    )(Todo.apply)(Todo.unapply)
  )

  def index = Action { request =>
    Ok(views.html.index(todos, request.flash.get("errors").getOrElse("")))
  }

  def createTodo = Action { implicit request =>
    todoForm.bindFromRequest().fold(
      frm => {
        Redirect(routes.Application.index()).flashing(
          "errors" -> frm.errors.map(_.message).mkString(",")
        )
      },
      newTodo => {
        todos = todos :+ newTodo
        Redirect(routes.Application.index())
      }
    )
  }

  def removeTodo(idx: Int) = Action {
    todos = todos.zipWithIndex.filter{case(_, i) => i != idx}.map(_._1)
    Redirect(routes.Application.index())
  }

  def flipTodoDone(idx: Int) = Action {
    todos = todos.zipWithIndex.map{
      case (t, i) =>
        if (i == idx) {
          if (t.done)
            t.copy(done = false)
          else
            t.copy(done = true)
        }
        else t
    }
    Redirect(routes.Application.index())
  }
}