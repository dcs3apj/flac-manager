/*
 * Copyright 2018 Alex Jones
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

package controllers

import java.nio.file.{FileSystem, Path}
import java.time.{Instant, ZonedDateTime}

import javax.inject.{Inject, Singleton}
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNel
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import common.async.CommandExecutionContext
import common.changes.{AddedChange, ChangeDao}
import common.configuration.{User, UserDao}
import common.dates.DateTime
import common.files.Extension
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent.Future

/**
  * A controller that provides information on what has changed in repositories.
  * @param userDao The [[UserDao]] used to get users from their usernames.
  * @param changeDao The [[ChangeDao]] used to get changes.
  */
@Singleton
class Changes @Inject()(val userDao: UserDao, val changeDao: ChangeDao, val controllerComponents: ControllerComponents, val fs: FileSystem)
                       (implicit val commandExecutionContext: CommandExecutionContext) extends BaseController with StrictLogging {

  /**
    * A helper method that will return either a JSON response or a 404 if a user is not find.
    * @param user The name of the user to look for.
    * @param extension The type of files to look for.
    * @param since The date to search from.
    * @param jsonBuilder A function that takes a user and a parsed date and returns a JSON value.
    * @return An action containing the JSON generated by the `jsonBuilder` or a 404 if the user could not be
    *         found or the date not parsed.
    */
  def action[V](
                user: User,
                extension: Extension,
                since: ZonedDateTime,
                requestType: String,
                dataFactory: (User, Extension, Instant) => Future[V])
              (jsonBuilder: RequestHeader => V => JsValue): Action[AnyContent] = {
    val username: String = user.name
    logger.info(s"Received a request for $username's $requestType since $since")
    val validatedUser: ValidatedNel[String, User] =
      userDao.allUsers().find(_.name == username).toValidNel(s"$username is not a valid user")
    validatedUser match {
      case Valid(validUser) => Action.async { implicit request: RequestHeader =>
        dataFactory(validUser, extension, since.toInstant).map { result =>
          Ok(jsonBuilder(request)(result))
        }
      }
      case Invalid(messages) => Action(BadRequest(messages.toList.mkString("\n")))
    }
  }

  /**
    * List all the changes for a user since a given date and time.
    * @param user The name of the user.
    * @param extension The type of files that have changed.
    * @param since The earliest date to search for changes.
    * @return A list of all the changes for the user since the given date or 404 if either is not valid.
    */
  def changes(user: User, extension: Extension, since: DateTime): Action[AnyContent] = action(user, extension, since, "changes", changeDao.getAllChangesSince) { implicit request => {
    changes =>
      val jsonChanges: Seq[JsObject] = changes.map { change =>
        val changeObj: JsObject = Json.obj(
          "action" -> change.action.action,
          "relativePath" -> change.relativePath.toString,
          "at" -> DateTime.formatter.format(change.at)
        )
        change.action match {
          case AddedChange => changeObj ++ links(user, extension, change.relativePath.toString)
          case _ => changeObj
        }
      }.distinct
      Json.obj("changes" -> jsonChanges)
    }
  }

  /**
    * Generate the links for a change or changelog that allow a client to download either music, tags or artwork.
    * @param user The user who whom the change is for.
    * @param relativePath The relative path of the music file for which the music will be downloaded.
    * @param request The request from the server.
    * @return A JSON object containing the music, tags and artwork links.
    */
  def links(user: User, extension: Extension, relativePath: String)(implicit request: RequestHeader): JsObject = {
    def url(callBuilder: (User, Extension, String) => Call, path: Path): String =
      callBuilder(user, extension, path.toString).absoluteURL().replace(' ', '+')

    val path: Path = fs.getPath(relativePath)
    Json.obj(
      "_links" -> Json.obj(
        "music" -> url(routes.Music.music, path),
        "tags" -> url(routes.Music.tags, path),
        "artwork" -> url(routes.Music.artwork, path.getParent)
      )
    )
  }

  /**
    * List all the changelog for a user since a given date and time.
    * @param user The name of the user.
    * @param extension The type of files that have changed.
    * @param since The earliest date to search for changes.
    * @return A list of all the changes for the user since the given date or 404 if either is not valid.
    */
  def changelog(user: User, extension: Extension, since: DateTime): Action[AnyContent] = action(user, extension, since, "changelog", changeDao.changelog) { implicit request => {
    changelog =>
      Json.obj(
        "total" -> changelog.size,
        "changelog" -> changelog.map { changelogItem =>
          Json.obj(
            "parentRelativePath" -> changelogItem.parentRelativePath.toString,
            "at" -> DateTime.formatter.format(changelogItem.at),
            "relativePath" -> changelogItem.relativePath.toString
          ) ++ links(user, extension, changelogItem.relativePath.toString)
        }
      )
    }
  }
}