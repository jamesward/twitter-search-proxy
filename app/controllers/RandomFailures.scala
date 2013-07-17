package controllers

import play.api.mvc._
import play.api.libs.concurrent.Promise
import scala.util.Random
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

case class FailFast[A](action: Action[A]) extends Action[A] with Controller {

  def apply(request: Request[A]): Result = {
    // fail about once in every 10 times
    if (Random.nextInt(10) == 0) {
      Logger.info("FailFast")
      InternalServerError
    } else {
      action(request)
    }
  }

  lazy val parser = action.parser
}

case class WaitOneMinute[A](action: Action[A]) extends Action[A] with Controller {

  def apply(request: Request[A]): Result = {
    // wait one minute about once in every 10 times
    if (Random.nextInt(10) == 0) {
      Logger.info("WaitOneMinute")
      Async {
        Promise.timeout(RequestTimeout, 1 minute)
      }
    }
    else {
      action(request)
    }
  }

  lazy val parser = action.parser
}