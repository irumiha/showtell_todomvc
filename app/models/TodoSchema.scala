package models

import scala.slick.driver.H2Driver.simple._

object TodoSchema {
  class Todo(tag: Tag) extends Table[(Int, String, Boolean)](tag, "TODO") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def todo = column[String]("TODO", O.NotNull)
    def done = column[Boolean]("DONE", O.NotNull, O.Default(false))

    def * = (id, todo, done)
  }

  val todos = TableQuery[Todo]
}
