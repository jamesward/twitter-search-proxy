package utils

import play.api.{Logger, Play}
import play.api.libs.ws.{WSAuthScheme, WSResponse, WS}
import scala.concurrent.{Promise, Future}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object Twitter {

  /*
   * Docs: https://dev.twitter.com/docs/auth/application-only-auth
   * 
   * API:
   * 
   *     POST /oauth2/token HTTP/1.1
   *     Host: api.twitter.com
   *     User-Agent: My Twitter App v1.0.23
   *     Authorization: Basic eHZ6MWV2RlM0d0VFUFRHRUZQSEJvZzpMOHFxOVBaeVJnNmllS0dFS2hab2xHQzB2SldMdzhpRUo4OERSZHlPZw==
   *     Content-Type: application/x-www-form-urlencoded;charset=UTF-8
   *     Content-Length: 29
   *     Accept-Encoding: gzip
   *     
   *     grant_type=client_credentials
   *     
   *     
   *     HTTP/1.1 200 OK
   *     Status: 200 OK
   *     Content-Type: application/json; charset=utf-8
   *     Content-Encoding: gzip
   *     Content-Length: 140
   *     
   *     {"token_type":"bearer","access_token":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2FAAAAAAAAAAAAAAAAAAAA%3DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}
   *     
   */
  lazy val bearerToken: Future[String] = {
    require(Play.current.configuration.getString("twitter.consumer.key").isDefined)
    require(Play.current.configuration.getString("twitter.consumer.secret").isDefined)
    
    WS.url("https://api.twitter.com/oauth2/token")
      .withAuth(Play.current.configuration.getString("twitter.consumer.key").get, Play.current.configuration.getString("twitter.consumer.secret").get, WSAuthScheme.BASIC)
      .post(Map("grant_type" ->  Seq("client_credentials")))
      .withFilter(response => (response.json \ "token_type").asOpt[String] == Some("bearer"))
      .map(response => (response.json \ "access_token").as[String])
  }

  def fetchTweets(bearerToken: String, query: String): Future[WSResponse] = {
    WS.url("https://api.twitter.com/1.1/search/tweets.json")
      .withQueryString("q" -> query)
      .withHeaders("Authorization" -> s"Bearer $bearerToken")
      .get
  }
  
}

