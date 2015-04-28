package controllers

import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._
import akka.actor._

import play.api.db.slick._
import models.TodoSchema._

import scala.slick.driver.H2Driver.simple._

case class TodoAction(action: String,
                      todoid: Option[Int],
                      todo: Option[String],
                      done: Option[Boolean])

object RealtimeClient {
  case class Update(t: TodoAction)
  def props(out: ActorRef, hub: ActorRef) = Props(new RealtimeClient(out, hub))
}

class RealtimeClient(out: ActorRef, hub: ActorRef) extends Actor {
  import RealtimeClient.Update
  import RealtimeHub.{Publish, Join, Leave}

  def receive: Receive = {
    case msg: TodoAction =>

      val todoAction = DB.withSession { implicit s =>
        msg.action match {
          case "add" =>
            val newTodo = Todo(None, msg.todo.getOrElse(""), msg.done.getOrElse(false))
            val newId = (todos returning todos.map(_.id)).insert(newTodo)
            msg.copy(todoid = Some(newId))
          case "update" =>
            val updated = Todo(msg.todoid, msg.todo.getOrElse(""), msg.done.getOrElse(false))
            todos.filter(_.id === msg.todoid).update(updated)
            msg
          case "delete" =>
            todos.filter(_.id === msg.todoid).delete
            msg
        }
      }
      // Send the update to the hub
      hub ! Publish(todoAction)

    case Update(t) => out ! t
  }

  override def preStart() = {
    hub ! Join(self)
  }

  override def postStop() = {
    hub ! Leave(self)
  }
}

object RealtimeHub {
  case class Join(r: ActorRef)
  case class Leave(r: ActorRef)
  case class Publish(t: TodoAction)
  def props = Props(new RealtimeHub())
}
class RealtimeHub extends Actor {
  import RealtimeHub.{Join, Leave, Publish}
  var clients: Set[ActorRef] = Set()

  def receive = {
    case Join(c)    => clients = clients + c
    case Leave(c)   => clients = clients - c
    case Publish(t) => clients.foreach(_ ! RealtimeClient.Update(t))
  }
}

object RealtimeApi extends Controller {
  lazy val todoHub = Akka.system.actorOf(RealtimeHub.props)
  implicit val todoFormat = Json.format[TodoAction]
  implicit val todoFrameFormatter = FrameFormatter.jsonFrame[TodoAction]

  def wssocket = WebSocket.acceptWithActor[TodoAction, TodoAction] { implicit request => out =>
    RealtimeClient.props(out, todoHub)
  }

}
