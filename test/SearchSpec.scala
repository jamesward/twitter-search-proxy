import java.util.concurrent.TimeoutException
import org.specs2.mutable._

import play.api.libs.json.Json
import play.api.mvc.{Results, PlainResult, AsyncResult, Result}
import play.api.{Play, Logger}
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class SearchSpec extends Specification {

  "Search" should {
    "return some tweets if the required config is set" in new WithApplication {
      if (Play.current.configuration.getString("twitter.consumer.key").isDefined && Play.current.configuration.getString("twitter.consumer.secret").isDefined) {
        
        var successes, timeouts, failures = 0
        
        // call the controller 100 times
        
        val futures: Seq[Future[Result]] = (1 to 100).map { i =>
          Future[Result](controllers.Search.tweets("typesafe")(FakeRequest()))
        }
        
        Await.result(Future.sequence(futures), Duration(20, SECONDS)).foreach { result =>
          val responseStatus = result match {
            case PlainResult(status, _) =>
              status
            case AsyncResult(p) =>
              Try {
                Await.result(p, Duration(300, MILLISECONDS))
              } recover {
                case _ =>
                  Results.RequestTimeout
              } get match {
                case PlainResult(status, _) => status
              }
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
        
        // Of 100 requests about 60 - 90 should succeed
        successes must beGreaterThan (60) and beLessThan(90)

        // Of 100 requests about 5 - 20 should timeout
        timeouts must beGreaterThan (5) and beLessThan(20)

        // Of 100 requests about 5 - 20 should fail
        timeouts must beGreaterThan (5) and beLessThan(20)
      }
    }
  }
  
}