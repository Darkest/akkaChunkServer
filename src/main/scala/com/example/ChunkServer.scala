package com.example

//#quick-start-server
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.example.NumberJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn


object ChunkServer extends App {

  implicit val system: ActorSystem = ActorSystem("Akka-streamingTest")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val persistMetrics = Flow[Number]//.alsoTo(Sink.foreach(println))

  val route =
    withoutSizeLimit {
      path("numbers") {
        entity(asSourceOf[Number])
        { measurements =>
          val measurementsSubmitted: Future[Int] =
            measurements
              .via(persistMetrics)
              //.runForeach(println)
              .runFold(0) { (cnt, el) =>
              println(cnt)
              cnt + el.value }

          complete {
            measurementsSubmitted.map(n => Map("msg" -> s"""Total sum of elements: $n"""))
          }
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
