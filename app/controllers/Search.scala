package controllers

import utils.Twitter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.mvc.{Action, Controller}
import play.api.libs.ws.WS
import play.api.Logger
import play.api.cache.Cached
import play.api.Play.current

object Search extends Controller {

  def tweets(query: String) = {
    FailFast {
      WaitOneMinute {
        Cached(query, 60 * 15) {
          Action {
            Async {
              Logger.info(s"Cache miss for $query")
              try {
                Twitter.bearerToken.flatMap { bearerToken =>
                  Twitter.fetchTweets(bearerToken, query).map { response =>
                    Ok(response.json)
                  }
                }
              } catch {
                case illegalArgumentException: IllegalArgumentException =>
                  Logger.error("Twitter Bearer Token is missing", illegalArgumentException)
                  Future(InternalServerError("Error talking to Twitter"))
              }
            }
          }
        }
      }
    }
  }

}
