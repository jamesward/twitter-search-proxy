package utils

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.ws.{WSClient, WSAuthScheme, WSResponse}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Twitter @Inject() (ws: WSClient, config: Configuration) {

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
    require(config.getString("twitter.consumer.key").isDefined)
    require(config.getString("twitter.consumer.secret").isDefined)
    
    ws.url("https://api.twitter.com/oauth2/token")
      .withAuth(config.getString("twitter.consumer.key").get, config.getString("twitter.consumer.secret").get, WSAuthScheme.BASIC)
      .post(Map("grant_type" ->  Seq("client_credentials")))
      .withFilter(response => (response.json \ "token_type").asOpt[String].contains("bearer"))
      .map(response => (response.json \ "access_token").as[String])
  }

  def fetchTweets(bearerToken: String, query: String): Future[WSResponse] = {
    ws.url("https://api.twitter.com/1.1/search/tweets.json")
      .withQueryString("q" -> query)
      .withHeaders("Authorization" -> s"Bearer $bearerToken")
      .get()
  }
  
}

