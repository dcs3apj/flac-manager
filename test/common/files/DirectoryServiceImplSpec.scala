/**
 * Copyright 2012 Alex Jones
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 *
 * @author unclealex72
 *
 */

package common.files

import java.nio.file.{Paths, Files, Path}

import common.configuration.Directories
import common.message.{MessageType, MessageService}
import org.specs2.mutable._
import org.specs2.mock._
import tempfs.TempFileSystem

/**
 * @author alex
 *
 */
class DirectoryServiceImplSpec extends Specification with Mockito {

  trait fs extends TempFileSystem {

    lazy implicit val directories = Directories(rootDirectory, rootDirectory, rootDirectory, rootDirectory)

    def fl(path: String, paths: String*): FlacFileLocation = FlacFileLocation(path, paths: _*)

    implicit object NullMessageService extends MessageService {
      override def printMessage(template: MessageType): Unit = {}
    }

    implicit val directoryService = new DirectoryServiceImpl

    def before(rootDirectory: Path) = {
      val paths = Seq(
        Paths.get("dir.flac", "myfile.flac"),
        Paths.get("dir.flac", "myfile.xml"),
        Paths.get("dir.flac", "inner", "myfile.flac"),
        Paths.get("dir.flac", "inner", "myfile.xml"),
        Paths.get("my.flac"),
        Paths.get("my.xml"),
        Paths.get("dir", "your.flac"),
        Paths.get("dir", "your.mp3"))
      paths.foreach { path =>
        val fullPath = rootDirectory.resolve(path)
        Files.createDirectories(fullPath.getParent)
        Files.createFile(fullPath)
      }

    }
  }

  "listing files in valid directories" should {
    "list the files" in new fs {
      val fileLocations = directoryService.listFlacFiles(Seq("dir.flac", "dir").map(f => rootDirectory.resolve(f)))
      fileLocations must beASuccessfulTry
      fileLocations.get must contain(exactly(
        fl("dir.flac", "myfile.flac"),
        fl("dir.flac", "inner", "myfile.flac"),
        fl("dir", "your.flac"),
        fl("dir.flac", "myfile.xml"),
        fl("dir.flac", "inner", "myfile.xml"),
        fl("dir", "your.mp3")
      ))
    }
  }

}
