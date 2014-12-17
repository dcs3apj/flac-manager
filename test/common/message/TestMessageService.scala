/*
 * Copyright 2014 Alex Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common.message

import com.typesafe.scalalogging.StrictLogging
import org.mockito.invocation.InvocationOnMock
import org.specs2.mock.Mockito

/**
 * Created by alex on 15/11/14.
 */
trait TestMessageService extends MessageService {

  def printMessage(template: MessageType): Unit
}

object TestMessageService extends Mockito with StrictLogging {

  def apply(): TestMessageService = {
    val loggingAnswer = (p1: InvocationOnMock) => logger.info(p1.toString)
    mock[TestMessageService].defaultAnswer(loggingAnswer)
  }
}