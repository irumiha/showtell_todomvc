package controllers

import play.api._
import play.api.db.slick._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models.TodoSchema._

import scala.slick.driver.H2Driver.simple._

object TodoApi extends Controller {

  implicit val todoReads: Reads[Todo] = (
    (JsPath \ "id").read[Option[Int]] and
      (JsPath \ "todo").read[String] and
      (JsPath \ "done").read[Boolean]
    )(Todo.apply _)

  def todoResourceList() = DBAction { implicit rs =>
    val currentTodos =
      todos.sortBy(_.id.desc).list.map {
        case Todo(id,todo,done) => JsObject(Seq(
          "id" -> id.map(JsNumber(_)).getOrElse(JsNull),
          "todo" -> JsString(todo),
          "done" -> JsBoolean(done)
        ))
      }

    Ok(JsObject(Seq("todos" -> JsArray(currentTodos))))
  }

  def createTodoResource() = DBAction(BodyParsers.parse.json) { implicit rs =>
    val newTodo = rs.request.body.validate[Todo]

    newTodo fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      todo => {
        todos.map(t => (t.todo, t.done)) +=(todo.todo, todo.done)
        Ok(Json.obj("status" -> "OK", "message" -> "Todo item saved."))
      })
  }

  def updateTodoResource(idx: Int) = DBAction(BodyParsers.parse.json) { implicit rs =>
    val todoObj = rs.request.body.validate[Todo]

    todoObj fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      todo => {
          todos
            .filter(_.id === idx)
            .map(t => (t.todo, t.done))
            .update(todo.todo, todo.done)

        Ok(Json.obj("status" -> "OK", "message" -> "Todo item updated."))
      })
  }

  def deleteTodoResource(idx: Int) = DBAction { implicit rs =>

    todos.filter(_.id === idx).delete

    Ok(Json.obj("status" -> "OK", "message" -> "Todo item maybe removed."))
  }

}
