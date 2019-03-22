package com.example

  case class Number(value: Int)

  object NumberJsonProtocol
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
      with spray.json.DefaultJsonProtocol {

    implicit val measurementFormat = jsonFormat1(Number.apply)
  }
