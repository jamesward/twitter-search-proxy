package controllers

import javax.inject.Inject

import play.api.cache.CacheApi
import play.api.libs.json.JsValue
import utils.Twitter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.mvc._
import play.api.Logger


class Search @Inject() (cache: CacheApi) extends Controller {

  def tweets(query: String) = (FailFast andThen WaitForNoReason) async {
    cache.get[JsValue](query).fold {
      try {
        Twitter.bearerToken.flatMap { bearerToken =>
          Twitter.fetchTweets(bearerToken, query).map { response =>
            cache.set(query, response.json, 1.hour)
            Ok(response.json)
          }
        }
      } catch {
        case illegalArgumentException: IllegalArgumentException =>
          Logger.error("Twitter Bearer Token is missing", illegalArgumentException)
          Future(InternalServerError("Error talking to Twitter"))
      }
    } { result =>
      Future.successful(Ok(result))
    }
  }

}
