import java.util.concurrent.TimeUnit

import controllers.{WaitForNoReason, Search}
import org.specs2.mutable.After
import play.api.cache.CacheApi
import play.api.libs.concurrent.Akka
import play.api.libs.ws.ning.{NingWSClientConfig, NingAsyncHttpClientConfigBuilder, NingWSClient}
import play.api.mvc.{ResponseHeader, Result}
import play.api.test._
import utils.Twitter
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

class SearchSpec extends PlaySpecification {

  class MemoryCache extends CacheApi {
    val theCache = scala.collection.mutable.Map.empty[String, Any]

    override def set(key: String, value: Any, expiration: Duration): Unit =
      theCache.put(key, value)

    override def get[T: ClassTag](key: String): Option[T] =
      theCache.get(key).map(_.asInstanceOf[T])

    override def getOrElse[A: ClassTag](key: String, expiration: Duration)(orElse: => A): A =
      theCache.getOrElse(key, orElse).asInstanceOf[A]

    override def remove(key: String): Unit =
      theCache.remove(key)
  }

  trait Context extends After {
    lazy val app = FakeApplication()
    lazy val wsClient = NingWSClient()
    lazy val config = app.configuration
    lazy val twitter = new Twitter(wsClient, config)
    lazy val cache = new MemoryCache()
    lazy val actorSystem = Akka.system(app)
    lazy val waitForNoReason = new WaitForNoReason(actorSystem)
    lazy val searchController = new Search(cache, twitter, waitForNoReason)
    def after = wsClient.close()
  }

  "Search" should {
    "return some tweets if the required config is set" in new Context {
      if (config.getString("twitter.consumer.key").isDefined && config.getString("twitter.consumer.secret").isDefined) {
        
        var successes, timeouts, failures = 0
        
        // call the controller 100 times
        
        val futures: Seq[Future[Result]] = (1 to 100).map { i =>
          searchController.tweets("typesafe")(FakeRequest())
        }
        
        await(Future.sequence(futures), 1, TimeUnit.MINUTES).foreach { result =>

          val responseStatus = result match {
            case Result(ResponseHeader(status, _, _), _, _) => status
          }

          responseStatus match {
            case OK =>
              successes += 1
            case INTERNAL_SERVER_ERROR =>
              failures += 1
            case REQUEST_TIMEOUT =>
              timeouts += 1
          }
        }
        
        // Of 100 requests about 60 - 95 should succeed
        successes must beGreaterThan (60) and beLessThan(95)

        // Of 100 requests about 5 - 20 should timeout
        timeouts must beGreaterThan (5) and beLessThan(20)

        // Of 100 requests about 5 - 20 should fail
        timeouts must beGreaterThan (5) and beLessThan(20)
      }
    }
  }
  
}