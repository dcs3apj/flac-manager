/*
 * Copyright 2014 Alex Jones
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package common.message

import play.api.i18n.Messages

/**
 * A `MessageServiceBuilder` that builds messages using Play's bundle support.
 * Created by alex on 06/11/14.
 */
class I18nMessageServiceBuilder(printers: Seq[String => Unit], exceptionHandlers: Seq[Throwable => Unit]) extends MessageServiceBuilder {

  override def build: MessageService = new MessageService() {

    override def printMessage(template: MessageType): Unit = {
      val message = Messages(template.key, template.parameters)
      printers.foreach(printer => printer(message))
    }

    override def exception(t: Throwable): Unit = {
      exceptionHandlers.foreach(exceptionHandler => exceptionHandler(t))
    }

  }

  override def withPrinter(printer: String => Unit): MessageServiceBuilder = {
    new I18nMessageServiceBuilder(printers :+ printer, exceptionHandlers)
  }

  override def withExceptionHandler(exceptionHandler: Throwable => Unit): MessageServiceBuilder = {
    new I18nMessageServiceBuilder(printers, exceptionHandlers :+ exceptionHandler)
  }
}

object I18nMessageServiceBuilder {

  def apply: I18nMessageServiceBuilder = new I18nMessageServiceBuilder(Seq(), Seq())
}