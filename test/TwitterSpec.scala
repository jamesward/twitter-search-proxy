import org.specs2.mutable.After
import play.api.libs.json.Json
import play.api.libs.ws.ning.{NingWSClientConfig, NingAsyncHttpClientConfigBuilder, NingWSClient}
import play.api.test._
import utils.Twitter
import play.api.Configuration

class TwitterSpec extends PlaySpecification {

  trait Context extends After {
    lazy val wsClient = new NingWSClient(new NingAsyncHttpClientConfigBuilder(NingWSClientConfig()).build())
    lazy val config: Configuration = FakeApplication().configuration
    lazy val twitter: Twitter = new Twitter(wsClient, config)
    def after = wsClient.close()
  }

  case class ErrorMessage(message: String, code: Int)

  implicit val errorMessageReads = Json.reads[ErrorMessage]
  
  "Twitter.bearerToken" should {
    "get a bearerToken if the required config is set" in new Context {
      if (config.getString("twitter.consumer.key").isDefined && config.getString("twitter.consumer.secret").isDefined) {
        await(twitter.bearerToken) must not (beNull)
      }
    }
  }

  "Twitter.fetchTweets" should {
    "get tweets if the required config is set" in new Context {
      if (config.getString("twitter.consumer.key").isDefined && config.getString("twitter.consumer.secret").isDefined) {
        val bearerToken = await(twitter.bearerToken)
        bearerToken must not (beNull)
        
        val twitterResponse = await(twitter.fetchTweets(bearerToken, "typesafe"))

        (twitterResponse.json \ "errors").asOpt[List[ErrorMessage]] must beNone

        (twitterResponse.json \ "search_metadata" \ "count").as[Int] must beGreaterThan (0)
      }
    }
  }
  
}