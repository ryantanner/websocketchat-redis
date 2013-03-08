package controllers

import play.api._
import play.api.Logger

import play.api.Play.current

import com.redis._

object RedisClients {

  val maybeRedisURL = scala.util.Properties.envOrNone("REDISTOGO_URL")

  Logger.info("Using redis url: " + maybeRedisURL)

  val (redisHost, redisPort, redisSecret) = maybeRedisURL match {
    case Some(redisURL) => {
      val redisURI = new java.net.URI(redisURL)
      (redisURI.getHost(), redisURI.getPort(), Some(redisURI.getUserInfo().split(":")(1)))
    }
    case None => {
      (Play.configuration.getString("redis.host").getOrElse("localhost"),
       Play.configuration.getInt("redis.port").getOrElse(6379),
       None)
    }
  }

  Logger.info(s"Redis host: $redisHost, Redis port: $redisPort")

  val clientPool = new RedisClientPool(redisHost, redisPort, secret = redisSecret)

}
