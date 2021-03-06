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

package common.files

import java.nio.file.Path

/**
 * An interface for classes that determine whether a file contains FLAC information or not.
 *
 * @author alex
 *
 */
trait FlacFileChecker {

  /**
   * Check whether a file is a FLAC encoded file or not.
   * @param path The file to check.
   * @return True if the file is a FLAC file or false otherwise.
   */
  def isFlacFile(path: Path): Boolean
}
