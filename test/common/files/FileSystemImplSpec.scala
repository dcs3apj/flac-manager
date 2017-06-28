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

import java.time.{Clock, Instant}
import java.nio.file.{FileSystem => JFS}
import common.configuration.User
import org.specs2.mutable._
import testfilesystem._

/**
 * @author alex
 *
 */
class FileSystemImplSpec extends Specification with PathMatchers with TestRepositories[JFSRepositoriesAndFileSystem] with RepositoryEntry.Dsl {

  val now: Instant = Clock.systemDefaultZone().instant()

  "Moving a file with siblings" should {
    "move only the file and not its siblings" in { jfsRepositoriesAndFs : JFSRepositoriesAndFileSystem =>
      val fs = jfsRepositoriesAndFs.fs
      val fileSystem = jfsRepositoriesAndFs.fileSystem
      val repositories = jfsRepositoriesAndFs.repositories
      fs.add(
        D("music",
          D("flac",
            D("dir",
              F("moveme.txt"),
              F("keepme.txt")
            )
          )
        )
      )
      val validatedSource = repositories.flac.file(fs.getPath("dir", "moveme.txt"))
      validatedSource.toEither must beRight { source : FlacFile =>
        val target = source.toStagingFile
        fileSystem.move(source, target)
        fs.entries must containTheSameElementsAs {
          fs.expected(
            D("music",
              D("flac",
                D("dir",
                  F("keepme.txt")
                )
              ),
              D("staging",
                D("dir",
                  F("moveme.txt")
                )
              )
            )
          )
        }
      }
    }
  }

  "Moving a file without siblings" should {
    "move only the file and remove empty directories" in { jfsRepositoriesAndFs : JFSRepositoriesAndFileSystem =>
      val fs = jfsRepositoriesAndFs.fs
      val fileSystem = jfsRepositoriesAndFs.fileSystem
      val repositories = jfsRepositoriesAndFs.repositories

      fs.add(
        D("music",
          D("flac",
            D("dir",
              F("moveme.txt")
            )
          )
        )
      )
      val validatedSource = repositories.flac.file(fs.getPath("dir", "moveme.txt"))
      validatedSource.toEither must beRight { source : FlacFile =>
        val target = source.toStagingFile
        fileSystem.move(source, target)
        fs.entries must containTheSameElementsAs {
          fs.expected(
            D("music",
              D("flac"),
              D("staging",
                D("dir",
                  F("moveme.txt")
                )
              )
            )
          )
        }
      }
    }
  }

  "Removing a file without siblings" should {
    "remove the  file and remove empty directories" in { jfsRepositoriesAndFs : JFSRepositoriesAndFileSystem =>
      val fs = jfsRepositoriesAndFs.fs
      val fileSystem = jfsRepositoriesAndFs.fileSystem
      val repositories = jfsRepositoriesAndFs.repositories

      fs.add(
        D("music",
          D("flac",
            D("dira",
              D("dirb",
                F("deleteme.txt", None)
              ),
              F("keepme.txt", None)
            )
          )
        )
      )
      val validatedSource = repositories.flac.file(fs.getPath("dira", "dirb", "deleteme.txt"))
      validatedSource.toEither must beRight { source : FlacFile =>
        fileSystem.remove(source)
        fs.entries must containTheSameElementsAs {
          fs.expected(
            D("music",
              D("flac",
                D("dira",
                  F("keepme.txt")
                )
              )
            )
          )
        }
      }
    }
  }

  "Copying a file" should {
    "create all required directories" in { jfsRepositoriesAndFs : JFSRepositoriesAndFileSystem =>
      val fs = jfsRepositoriesAndFs.fs
      val fileSystem = jfsRepositoriesAndFs.fileSystem
      val repositories = jfsRepositoriesAndFs.repositories

      fs.add(
        D("music",
          D("flac",
            D("dir",
              F("copyme.txt", None)
            )
          )
        )
      )
      val validatedSource = repositories.flac.file(fs.getPath("dir", "copyme.txt"))
      validatedSource.toEither must beRight { source : FlacFile =>
        val target = source.toStagingFile
        fileSystem.copy(source, target)
        fs.entries must containTheSameElementsAs {
          fs.expected(
            D("music",
              D("flac",
                D("dir",
                  F("copyme.txt")
                )
              ),
              D("staging",
                D("dir",
                  F("copyme.txt")
                )
              )
            )
          )
        }
      }
    }
  }

  "Linking to a file" should {
    "create all required directories and link to the file" in { jfsRepositoriesAndFs : JFSRepositoriesAndFileSystem =>
      val fs = jfsRepositoriesAndFs.fs
      val fileSystem = jfsRepositoriesAndFs.fileSystem
      val repositories = jfsRepositoriesAndFs.repositories

      fs.add(
        D("music",
          D("encoded",
            D("dir",
              F("linktome.txt", None)
            )
          )
        )
      )
      val validatedSource = repositories.encoded.file(fs.getPath("dir", "linktome.txt"))
      validatedSource.toEither must beRight { source : EncodedFile =>
        val target = source.toDeviceFile(User("freddie"))
        fileSystem.link(source, target)
        fs.entries must containTheSameElementsAs {
          fs.expected(
            D("music",
              D("devices",
                D("freddie",
                  D("dir",
                    L("linktome.txt", "../../../encoded/dir/linktome.txt")
                  )
                )
              ),
              D("encoded",
                D("dir",
                  F("linktome.txt")
                )
              )
            )
          )
        }
      }
    }
  }

  override def generate(fs: JFS, repositories: Repositories): JFSRepositoriesAndFileSystem = JFSRepositoriesAndFileSystem(fs, repositories, new FileSystemImpl)
}

case class JFSRepositoriesAndFileSystem(fs: JFS, repositories: Repositories, fileSystem: FileSystem)