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

package common.files

import java.nio.file.{Files, Path}

import common.configuration.Directories

/**
 * Created by alex on 16/11/14.
 */
class FileLocationExtensionsImpl extends FileLocationExtensions {

  implicit val fileLocationToPath: FileLocation => Path = _.toPath

  override def isDirectory(fileLocation: FileLocation) = {
    Files.exists(fileLocation) && !Files.isSymbolicLink(fileLocation) && Files.isDirectory(fileLocation)
  }

  def exists(fileLocation: FileLocation) = Files.exists(fileLocation)

  override def createTemporaryFileLocation(extension: Extension)(implicit directories: Directories): TemporaryFileLocation = {
    val path = Files.createTempFile(directories.temporaryPath, "flac-manager-", s".${extension.extension}")
    TemporaryFileLocation(directories.temporaryPath.resolve(path))
  }

  override def lastModified(fileLocation: FileLocation): Long = {
    Files.getLastModifiedTime(fileLocation).toMillis
  }
}
