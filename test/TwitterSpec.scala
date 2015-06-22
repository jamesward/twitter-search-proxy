import org.specs2.mutable.Specification

import play.api.libs.json.Json
import play.api.test._
import scala.concurrent.Await
import scala.concurrent.duration._
import utils.Twitter
import play.api.Play

class TwitterSpec extends Specification {

  case class ErrorMessage(message: String, code: Int)

  implicit val errorMessageReads = Json.reads[ErrorMessage]
  
  "Twitter.bearerToken" should {
    "get a bearerToken if the required config is set" in new WithApplication {
      if (Play.current.configuration.getString("twitter.consumer.key").isDefined && Play.current.configuration.getString("twitter.consumer.secret").isDefined) {
        Await.result(Twitter.bearerToken, Duration(1, MINUTES)) must not beNull
      }
    }
  }

  "Twitter.fetchTweets" should {
    "get tweets if the required config is set" in new WithApplication {
      if (Play.current.configuration.getString("twitter.consumer.key").isDefined && Play.current.configuration.getString("twitter.consumer.secret").isDefined) {
        val bearerToken = Await.result(Twitter.bearerToken, Duration(1, MINUTES))
        bearerToken must not beNull
        
        val twitterResponse = Await.result(Twitter.fetchTweets(bearerToken, "typesafe"), 1 minute)

        (twitterResponse.json \ "errors").asOpt[List[ErrorMessage]] must beNone

        (twitterResponse.json \ "search_metadata" \ "count").as[Int] must beGreaterThan (0)
      }
    }
  }
  
}