package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import play.api.mvc._
import scala.concurrent.{Promise, Future}
import scala.util.{Try, Random}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


object FailFast extends ActionBuilder[Request] with ActionFilter[Request] with Results {
  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    // fail about once in every 10 times
    if (Random.nextInt(10) == 0) {
      Future.successful(Some(InternalServerError))
    }
    else {
      Future.successful(None)
    }
  }
}

class WaitForNoReason @Inject() (actorSystem: ActorSystem) extends ActionBuilder[Request] with ActionFilter[Request] with Results {
  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    // fail about once in every 10 times
    if (Random.nextInt(10) == 0) {
      val p = Promise[Option[Result]]()
      actorSystem.scheduler.scheduleOnce(25.seconds) {
        p.complete(Try(Some(RequestTimeout)))
      }
      p.future
    }
    else {
      Future.successful(None)
    }
  }
}