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

package common.files

import com.typesafe.scalalogging.StrictLogging
import common.message.MessageService

/**
 * An implementation of {@link FileUtils} that decorates another {@link FileUtils}
 * @author alex
 *
 */
abstract class DecoratingFileUtils(val delegate: FileUtils) extends FileUtils with StrictLogging {

  def wrap(block: => FileUtils => Unit)(fileLocations: FileLocation*): Unit = {
    before(fileLocations)
    try {
      block(delegate)
    }
    finally {
      after(fileLocations)
    }
  }

  override def move(sourceFileLocation: FileLocation, targetFileLocation: FileLocation)(implicit messageService: MessageService): Unit =
    wrap(_.move(sourceFileLocation, targetFileLocation))(sourceFileLocation, targetFileLocation)

  override def copy(sourceFileLocation: FileLocation, targetFileLocation: FileLocation)(implicit messageService: MessageService): Unit =
    wrap(_.copy(sourceFileLocation, targetFileLocation))(targetFileLocation)

  override def remove(fileLocation: FileLocation)(implicit messageService: MessageService): Unit =
    wrap(_.remove(fileLocation))(fileLocation)

  override def link(fileLocation: FileLocation, linkLocation: FileLocation)(implicit messageService: MessageService): Unit =
    wrap(_.link(fileLocation, linkLocation))(fileLocation, linkLocation)

  override def isDirectory(fileLocation: FileLocation) = delegate.isDirectory(fileLocation)

  def before(fileLocations: Seq[FileLocation]): Unit

  def after(fileLocations: Seq[FileLocation]): Unit
}
