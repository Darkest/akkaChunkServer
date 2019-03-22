package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.FlatSpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

class ChunkTester extends FlatSpec  with SprayJsonSupport{

  val address = "localhost"
  val port = 8080

  implicit val system: ActorSystem = ActorSystem("Akka-streamingTest")
  implicit val ec = system.dispatcher

  "test" should "send infinite post request to server with infinite stream of random numbers2" in {


    def numberToJson(n: Int) = {
      s"""{"value":$n}"""
    }

    val numbers: Source[ByteString, NotUsed] = Source.fromIterator(() =>
      Iterator.continually(ByteString(numberToJson(Random.nextInt()))) //Could not do marshaling using spray
    )

    val httpRequest = HttpRequest(HttpMethods.POST, uri = s"http://$address:$port/numbers", entity = HttpEntity(ContentTypes.`application/json`, data = numbers))
    val response = Http().singleRequest(httpRequest)
    val result = Await.result(response, Duration.Inf)
  }
}
