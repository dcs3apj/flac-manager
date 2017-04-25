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

package common.commands

import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.{Files, StandardCopyOption}
import javax.inject.Inject

import com.google.common.collect.Sets

/**
  * Copy the flac2mp3.sh command to a temporary directory.
  */
class TempFileCommandService  @Inject() extends CommandService {

  private def create(resourceName: String): String = {
    val commandPath = Files.createTempFile("flacman-", s"-$resourceName")
    commandPath.toFile.deleteOnExit()
    val in = getClass.getResourceAsStream(resourceName)
    if (in == null) {
      throw new IllegalStateException(s"Cannot find resource $resourceName")
    }
    Files.copy(in, commandPath, StandardCopyOption.REPLACE_EXISTING)
    val permissions = Sets.newHashSet(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, OTHERS_READ)
    Files.setPosixFilePermissions(commandPath, permissions)
    in.close()
    commandPath.toAbsolutePath.toString
  }

  /**
    * @inheritdoc
    */
  override val flac2mp3Command: String = create("flac2mp3.sh")
}