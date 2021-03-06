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
import java.nio.file.{FileSystem, FileSystems}
import java.util.logging.LogManager

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.{EitherT, NonEmptyList}
import cats.instances.future._
import io.circe.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * The main entry point for all commands. The first argument is expected to be the command name.
  **/
object Client extends App {

  LogManager.getLogManager.reset()

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val fs: FileSystem = FileSystems.getDefault

  private val isDev: Boolean = !Option(System.getenv("FLAC_DEV")).forall(_.isEmpty)
  if (isDev) {
    println("Running in development mode")
  }
  val ws = WS()
  try {
    val eventualAction: EitherT[Future, NonEmptyList[String], Unit] = for {
      serverDetails <- fetchServerDetails()
      body <- parseParameters(serverDetails)
      _ <- runCommand(body, serverDetails)
    }  yield {}
    Await.result(eventualAction.value, Duration.Inf) match {
      case Left(messages) => messages.toList.foreach(System.err.println)
      case _ => println("Finished.")
    }
  }
  finally {
    Try(ws.close())
    Try(system.terminate())
  }

  private type Response[T] = EitherT[Future, NonEmptyList[String], T]

  // Search for the server via UPNP
  private def fetchServerDetails(): Response[ServerDetails] = EitherT(ServerDetails(isDev))

  // Parse parameters from the server.
  private def parseParameters(serverDetails: ServerDetails): Response[Json] =
    EitherT(Future.successful(ParametersParser(serverDetails.datumFilename, args)))

  // Run the command on the server.
  private def runCommand(body: Json, serverDetails: ServerDetails): Response[Unit] = {
    EitherT.right(RemoteCommandRunner(ws, body, serverDetails.uri, System.out))
  }

}
