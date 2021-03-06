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

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

import cats.data.Validated.Valid
import cats.data.{Validated, ValidatedNel}
import common.configuration.{Directories, User}
import common.files.Extension._
import common.message.Messages.{NOT_A_DIRECTORY, NOT_A_FILE}
import common.message.{Message, MessageService, Messaging}
import common.music.{Tags, TagsService}
import javax.inject.Inject

import scala.collection.JavaConverters._
import scala.collection.{SortedMap, SortedSet, mutable}
/**
  * The default implementation of [[Repositories]]
  **/
class RepositoriesImpl @Inject()(val directories: Directories, val tagsService: TagsService, val flacFileChecker: FlacFileChecker) extends Repositories with Messaging {

  override def flac(implicit messageService: MessageService): FlacRepository =
    new RepositoryImpl[FlacFile]("flac", directories.flacPath, new FlacFileImpl(_, new PathTagsContainer(_))) with FlacRepository

  override def staging(implicit messageService: MessageService): StagingRepository =
    new RepositoryImpl[StagingFile]("staging", directories.stagingPath, new StagingFileImpl(_, new PathTagsContainer(_))) with StagingRepository

  override def encoded(extension: Extension)(implicit messageService: MessageService): EncodedRepository =
    new RepositoryImpl[EncodedFile]("encoded", directories.encodedPath.resolve(extension.extension), new EncodedFileImpl(extension, _, new PathTagsContainer(_))) with EncodedRepository

  override def device(user: User, extension: Extension)(implicit messageService: MessageService): DeviceRepository = {
    val _user: User = user
    new RepositoryImpl[DeviceFile]("device", directories.devicesPath.resolve(user.name).resolve(extension.extension), new DeviceFileImpl(user, extension, _, new PathTagsContainer(_))) with DeviceRepository {
      val user: User = _user
    }
  }

  /**
    * A tags container where the tags are stored in a file on the file system.
    *
    * @param absolutePath   The absolute path of the file where the tags are stored.
    * @param messageService The message service used to report progress and errors.
    */
  class PathTagsContainer(absolutePath: Path)(implicit val messageService: MessageService) extends TagsContainer {
    lazy val tags: ValidatedNel[Message, Tags] = tagsService.read(absolutePath)

    override def read(): ValidatedNel[Message, Tags] = tags
  }

  /**
    * A tags container that stores a static set of tags. This is used for transferring tags between different
    * types of file.
    *
    * @param staticTags The tags stored by this container.
    */
  class StaticTagsContainer(staticTags: Tags) extends TagsContainer {
    val tags: ValidatedNel[Message, Tags] = Validated.valid(staticTags)

    override def read(): ValidatedNel[Message, Tags] = tags
  }

  /**
    * The base class for implementations of [[File]]
    *
    * @param readOnly              True if this file should be read only, false otherwise.
    * @param rootPath              The top level directory of the repository this file is in.
    * @param basePath              The base directory of the repository this file is in.
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  abstract class FileImpl(val readOnly: Boolean,
                          val rootPath: Path,
                          val basePath: Path,
                          val relativePath: Path,
                          tagsContainerProvider: Path => TagsContainer)(implicit val messageService: MessageService) {

    lazy val absolutePath: Path = basePath.resolve(relativePath)
    lazy val exists: Boolean = Files.isSymbolicLink(absolutePath) || Files.exists(absolutePath)
    lazy val lastModified: Instant = Files.getLastModifiedTime(absolutePath, LinkOption.NOFOLLOW_LINKS).toInstant
    val tags: TagsContainer = tagsContainerProvider(absolutePath)

    /**
      * Compare files by their relative path.
      *
      * @param other The file to compare.
      * @return True if the two files have the same path, false otherwise.
      */
    override def equals(other: Any): Boolean = other match {
      case f: File => absolutePath.equals(f.absolutePath)
      case _ => false
    }

    override def hashCode(): Int = absolutePath.hashCode()

    override def toString: String = absolutePath.toString
  }

  /**
    * The base class for implementations of [[Directory]]
    *
    * @param basePath       The base path of the repository this directory is in.
    * @param relativePath   The relative path of the directory in the repository.
    * @param fileBuilder    A function used to create a [[File]] from a path in this directory's repository.
    * @param messageService The message service used to report progress and errors.
    * @tparam F The type of files in the repository.
    */
  class DirectoryImpl[F <: File](val basePath: Path, val relativePath: Path, fileBuilder: Path => F)(implicit val messageService: MessageService) extends Directory[F] {
    override val absolutePath: Path = basePath.resolve(relativePath)

    override def toString: String = absolutePath.toString

    override def list: SortedSet[F] = list(Int.MaxValue)

    override def list(maxDepth: Int): SortedSet[F] = {
      val empty: SortedSet[F] = SortedSet.empty
      walk(maxDepth).foldLeft(empty) { (files, directoryOrFile) =>
        val maybeFile: Option[F] = directoryOrFile.fold(_ => None, Some(_))
        files ++ maybeFile
      }
    }

    /**
      * Walk through a directory.
      *
      * @param maxDepth The maximum depth to look for files or directories.
      * @return A sequence of the directories or files found.
      */
    def walk(maxDepth: Int): Seq[Either[Directory[F], F]] = {
      val paths: mutable.Buffer[Either[Directory[F], F]] = mutable.Buffer.empty
      def relativise(path: Path): Path = basePath.relativize(path)
      val visitor: FileVisitor[Path] = new FileVisitor[Path] {
        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (!Files.isHidden(file)) {
            paths += Right(fileBuilder(relativise(file)))
          }
          FileVisitResult.CONTINUE
        }

        override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.TERMINATE

        override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (Files.isHidden(dir)) {
            FileVisitResult.SKIP_SUBTREE
          }
          else {
            paths += Left(new DirectoryImpl[F](basePath, relativise(dir), fileBuilder))
            FileVisitResult.CONTINUE
          }
        }
      }

      Files.walkFileTree(absolutePath, mutable.Set.empty[FileVisitOption].asJava, maxDepth, visitor)
      paths
    }

    override def group: SortedMap[Directory[F], SortedSet[F]] = {
      case class State(directories: Map[Path, Directory[F]] = Map.empty, groupedFiles: SortedMap[Directory[F], SortedSet[F]] = SortedMap.empty) {
        def directory(directory: Directory[F]): State = copy(directories = directories + (directory.relativePath -> directory))

        def +(file: F): State = {
          directories.get(file.relativePath.getParent) match {
            case Some(directory) =>
              val files: SortedSet[F] = groupedFiles.getOrElse(directory, SortedSet.empty[F]) + file
              copy(groupedFiles = groupedFiles + (directory -> files))
            case None => this
          }
        }
      }
      val state: State = walk(Int.MaxValue).foldLeft(State()) { (state, directoryOrFile) =>
        directoryOrFile match {
          case Left(directory) => state.directory(directory)
          case Right(file) => state + file
        }
      }
      state.groupedFiles
    }
  }

  /**
    * The default implementation of [[FlacFile]]
    *
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  class FlacFileImpl(override val relativePath: Path, tagsContainerProvider: Path => TagsContainer)(implicit messageService: MessageService) extends FileImpl(true, directories.flacPath, directories.flacPath, relativePath, tagsContainerProvider)(messageService) with FlacFile {
    override def toStagingFile: StagingFile = {
      new StagingFileImpl(relativePath, _ => tags)
    }

    override def toEncodedFile(extension: Extension): EncodedFile = {
      new EncodedFileImpl(extension, relativePath.withExtension(extension), _ => tags)
    }
  }

  /**
    * The default implementation of [[StagingFile]]
    *
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  class StagingFileImpl(override val relativePath: Path, tagsContainerProvider: Path => TagsContainer)(implicit messageService: MessageService) extends FileImpl(false, directories.stagingPath, directories.stagingPath, relativePath, tagsContainerProvider)(messageService) with StagingFile {
    override def isFlacFile: Boolean = flacFileChecker.isFlacFile(absolutePath)

    override def toFlacFileAndTags: ValidatedNel[Message, (FlacFile, Tags)] = {
      tags.read().map { tags =>
        val flacFile = new FlacFileImpl(tags.asPath(relativePath.getFileSystem, FLAC), _ => new StaticTagsContainer(tags))
        (flacFile, tags)
      }
    }

    override def writeTags(tags: Tags): StagingFile = {
      tagsService.write(absolutePath, tags)
      new StagingFileImpl(relativePath, _ => new StaticTagsContainer(tags))
    }
  }

  /**
    * The default implementation of [[EncodedFile]]
    *
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  class EncodedFileImpl(override val extension: Extension, override val relativePath: Path, tagsContainerProvider: Path => TagsContainer)(implicit messageService: MessageService) extends FileImpl(true, directories.encodedPath, directories.encodedPath.resolve(extension.extension), relativePath, tagsContainerProvider)(messageService) with EncodedFile {
    override def toTempFile: TempFile = {
      val baseDirectory: Path = directories.temporaryPath
      val tempPath: Path = Files.createTempFile(baseDirectory, "flacmanager-encoding-", s".${extension.extension}")
      new TempFileImpl(baseDirectory, baseDirectory.resolve(tempPath), _ => tags)
    }

    override def toDeviceFile(user: User): DeviceFile = new DeviceFileImpl(user, extension, relativePath, _ => tags)
  }

  /**
    * The default implementation of [[DeviceFile]]
    *
    * @param user                  The user who owns the repository that contains this file.
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  class DeviceFileImpl(override val user: User, override val extension: Extension, override val relativePath: Path, tagsContainerProvider: Path => TagsContainer)(implicit messageService: MessageService) extends FileImpl(true, directories.devicesPath, directories.devicesPath.resolve(user.name).resolve(extension.extension), relativePath, tagsContainerProvider)(messageService) with DeviceFile

  /**
    * The default implementation of [[TempFile]]
    *
    * @param basePath              The base directory of the repository this file is in.
    * @param relativePath          The path this file points to.
    * @param tagsContainerProvider A function to generate a tags container from a path.
    * @param messageService        The message service used to report progress and errors.
    */
  class TempFileImpl(override val basePath: Path, override val relativePath: Path, tagsContainerProvider: Path => TagsContainer)(implicit messageService: MessageService) extends FileImpl(false, basePath, basePath, relativePath, tagsContainerProvider)(messageService) with TempFile {
    override def writeTags(): TempFile = {
      tags.read().foreach { tags =>
        tagsService.write(absolutePath, tags)
      }
      new TempFileImpl(basePath: Path, relativePath, _ => tags)
    }
  }

  /**
    * The default implementation of [[Repository]]
    *
    * @param repositoryType The type of the repository used only to identify it in logs and messages.
    * @param basePath       The base path of the repository.
    * @param fileBuilder    A function used to build files in this repositories from paths.
    * @param messageService The message service used to report progress and errors.
    * @tparam F The type of files in this repository.
    */
  class RepositoryImpl[F <: File](val repositoryType: String, basePath: Path, fileBuilder: Path => F)(implicit val messageService: MessageService) extends Repository[F] {
    override def root: ValidatedNel[Message, Directory[F]] = {
      directory(basePath.relativize(basePath))
    }

    override def directory(path: Path): ValidatedNel[Message, Directory[F]] = {
      val newDirectory = new DirectoryImpl[F](basePath, path, fileBuilder)
      if (Files.isDirectory(newDirectory.absolutePath)) {
        Valid(newDirectory)
      }
      else {
        Validated.invalidNel(NOT_A_DIRECTORY(path, repositoryType))
      }
    }

    override def file(path: Path): ValidatedNel[Message, F] = {
      val newFile = fileBuilder(path)
      if (Files.isDirectory(newFile.absolutePath)) {
        Validated.invalidNel(NOT_A_FILE(path, repositoryType))
      }
      else {
        Valid(newFile)
      }
    }
  }

}
