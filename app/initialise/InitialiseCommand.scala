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

package initialise

import cats.data.ValidatedNel
import common.message.{Message, MessageService}

import scala.concurrent.Future

/**
  * The command to initialise the database.
  */
trait InitialiseCommand {

  /**
    * Initialise the database with all device files.
    * @param messageService The [[MessageService]] used to report progress and log errors.
    * @return A [[Future]] that will initialise the database.
    */
  def initialiseDb(implicit messageService: MessageService): Future[ValidatedNel[Message, Unit]]
}
