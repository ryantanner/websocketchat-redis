package models

import akka.actor.{ ActorSystem, Props }

import play.api.libs.iteratee._
import play.api.libs.json._

import com.redis._
import serialization._

class Pub {

  val system = ActorSystem("pub")
  val r = new RedisClient("localhost", 6377)
  val p = system.actorOf(Props(new Publisher(r)))

  private def publish(channel: String, message: String) = {
    p ! Publish(channel, message)
  }

  def sendChatMsg(msg: String) = {
    publish("demo-channel", msg)
  }
}

class Sub(pChannel: Concurrent.Channel[JsValue]) {
// from = type read from redis
// to = type returned

  val system = ActorSystem("sub")
  val r = new RedisClient("localhost", 6377)
  val s = system.actorOf(Props(new Subscriber(r)))
  s ! Register(callback) 

  def sub(channels: String*) = {
    s ! Subscribe(channels.toArray)
  }

  def unsub(channels: String*) = {
    s ! Unsubscribe(channels.toArray)
  }

  def callback(pubsub: PubSubMessage) = pubsub match {
    case E(exception) => println("Fatal error caused consumer dead. Please init new consumer reconnecting to master or connect to backup")
    case S(channel, no) => println("subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("unsubscribed from " + channel + " and count = " + no)
    case M(channel, msg) => 
      msg match {
        // exit will unsubscribe from all channels and stop subscription service
        case "exit" => 
          println("unsubscribe all ..")
          pChannel.end
          r.unsubscribe

        // message "+x" will subscribe to channel x
        case x if x startsWith "+" => 
          val s: Seq[Char] = x
          s match {
            case Seq('+', rest @ _*) => r.subscribe(rest.toString){ m => }
          }

        // message "-x" will unsubscribe from channel x
        case x if x startsWith "-" => 
          val s: Seq[Char] = x
          s match {
            case Seq('-', rest @ _*) => r.unsubscribe(rest.toString)
                                        pChannel.end
          }

        // other message receive
        // chatChannel.push(msg) goes in here somehow
        // println("[sub] received message on channel " + channel + " as : " + x)
        case x => 
          pChannel.push(Json.parse(x))

      }
  }
}
/*
class RedisSub[E](val channel: String)(implicit parse: Parse[A], client: RedisClient)  {

  private val enumerator: Enumerator[E]


}
*/
