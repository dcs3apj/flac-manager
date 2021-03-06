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

package checkout

import cats.data.ValidatedNel
import common.files.Directory.FlacDirectory
import common.message.{Message, MessageService}

import scala.collection.SortedSet
import scala.concurrent.Future

/**
  * The command used to checkout flac files from the flac repository. Files that get checked out are not
  * allowed to overwrite existing files in the staging repository.
  */
trait CheckoutCommand {

  /**
    * Check out a sequence of flac files.
    * @param directories The locations of the flac files to check out.
    * @param unown True if each flac file should also lose its owners, false otherwise.
    * @param messageService The [[MessageService]] used to report progress and errors.
    * @return A [[Future]] that checks out flac files.
    */
  def checkout(directories: SortedSet[FlacDirectory], unown: Boolean)(implicit messageService: MessageService): Future[ValidatedNel[Message, Unit]]
}
