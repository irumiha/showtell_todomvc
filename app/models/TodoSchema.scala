package models

import scala.slick.driver.H2Driver.simple._

object TodoSchema {

  case class Todo(id: Option[Int], todo: String, done: Boolean)

  class Todos(tag: Tag) extends Table[Todo](tag, "TODO") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def todo = column[String]("TODO", O.NotNull)
    def done = column[Boolean]("DONE", O.NotNull, O.Default(false))

    def * = (id.?, todo, done) <> (Todo.tupled, Todo.unapply)
  }

  val todos = TableQuery[Todos]
}
