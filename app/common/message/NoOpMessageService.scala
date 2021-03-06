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

package common.message

import com.typesafe.scalalogging.Logger

/**
  * Created by alex on 23/04/17
  **/
object NoOpMessageService {

  def apply(loggerProvider: { val logger: Logger}): MessageService = new MessageService {
    val logger: Logger = loggerProvider.logger
    override def printMessage(template: Message): Unit = logger.info(template.toString)
    override def exception(t: Throwable): Unit = logger.error("Error message", t)

  }

}

