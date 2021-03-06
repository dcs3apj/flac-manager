/*
 * Copyright 2017 Alex Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package client

import java.io.PrintStream
import java.net.URI

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import common.configuration.HttpStreams
import io.circe.Json
import play.api.libs.ws.{BodyWritable, InMemoryBody, StandaloneWSResponse, WSClient}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

/**
  * Connect to the server and run a command remotely.
  * Created by alex on 17/04/17
  **/
object RemoteCommandRunner {

  implicit val jsonWriteable: BodyWritable[Json] =
    BodyWritable(json => InMemoryBody(ByteString(json.noSpaces)), "application/json")

  def apply(
             ws: WSClient,
             body: Json,
             serverUri: URI,
             out: PrintStream)
           (implicit materializer: Materializer, executionContext: ExecutionContext): Future[Unit] = {
    val commandUri: URI = serverUri.resolve(new URI(s"/commands"))
    val futureResponse: Future[StandaloneWSResponse] =
      ws.url(commandUri.toString).withRequestTimeout(Duration.Inf).withMethod("POST").withBody(body).stream()
    futureResponse.flatMap { res =>
      val sink: Sink[ByteString, Future[Done]] = Sink.foreach[ByteString] { bytes =>
        val message = new String(bytes.toArray, HttpStreams.DEFAULT_CHARSET)
        if (HttpStreams.KEEP_ALIVE != message) {
          out.print(message)
        }
      }
      res.bodyAsSource.runWith(sink).andThen {
        case result =>
          result.get
      }
    }.map(_ => {})
  }
}
