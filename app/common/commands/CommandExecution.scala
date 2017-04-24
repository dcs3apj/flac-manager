/*
 * Copyright 2015 Alex Jones
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

package common.commands

/**
 * A trait used to execute a command either synchronously (and does not explicitly send a finish message to the
  * message service) or asynchronously.
 * Created by alex on 14/01/15.
 */
sealed trait CommandExecution {

  /**
    * Execute the command
    */
  def execute(): Unit

  /**
    * A flag to indicate whether a finish message needs to be sent to a [[common.message.MessageService]]
    * @return True if the message service requires finishing, false otherwise.
    */
  def requiresFinish: Boolean
}

/**
  * An object used to create implementations of [[CommandExecution]]
  */
object CommandExecution {

  private def simpleCommandType(block: => Unit, _requiresFinish: Boolean) = new CommandExecution {
    override def execute(): Unit = {
      block
    }
    override def requiresFinish: Boolean = _requiresFinish
  }

  /**
    * Create a synchronous command execution.
    * @param block The code to run.
    * @return A synchronous command execution
    */
  def synchronous(block: => Unit): CommandExecution = simpleCommandType(block, _requiresFinish = true)

  /**
    * Create an asynchronous command execution.
    * @param block The code to run.
    * @return A synchronous command execution
    */
  def asynchronous(block: => Unit): CommandExecution = simpleCommandType(block, _requiresFinish = false)
}
