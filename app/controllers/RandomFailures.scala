package controllers

import play.api.mvc._
import play.api.libs.concurrent.Promise
import scala.concurrent.Future
import scala.util.Random
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

object WaitForNoReason extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    // wait 25 seconds about once in every 10 times
    if (Random.nextInt(10) == 0) {
      Promise.timeout(request, 25.seconds).flatMap(block)
    }
    else {
      block(request)
    }
  }
}