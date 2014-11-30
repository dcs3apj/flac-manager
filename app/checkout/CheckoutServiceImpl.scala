package checkout

import common.changes.{Change, ChangeDao}
import common.configuration.{Directories, User, Users}
import common.files._
import common.message.MessageService
import common.music.{Tags, TagsService}
import common.now.NowService
import common.owners.OwnerService

import scala.collection.{SortedMap, SortedSet}

/**
 * Created by alex on 17/11/14.
 */
class CheckoutServiceImpl(val fileSystem: FileSystem, val users: Users, val ownerService: OwnerService, val nowService: NowService)
                         (implicit val changeDao: ChangeDao, implicit val directories: Directories, val fileLocationExtensions: FileLocationExtensions, val tagsService: TagsService)
  extends CheckoutService {

  override def checkout(flacFileLocationsByParent: SortedMap[FlacFileLocation, SortedSet[FlacFileLocation]], unown: Boolean)(implicit messageService: MessageService): Unit = {
    val tagsForUsers = flacFileLocationsByParent.foldLeft(Map.empty[User, Set[Tags]])(findTagsAndDeleteFiles)
    if (unown) {
      tagsForUsers.foreach { case (user, tagsSet) =>
        ownerService.unown(user, tagsSet)
      }
    }
  }

  def findTagsAndDeleteFiles(tagsForUsers: Map[User, Set[Tags]], fls: (FlacFileLocation, SortedSet[FlacFileLocation]))(implicit messageService: MessageService): Map[User, Set[Tags]] = {
    val directory = fls._1
    val flacFileLocations = fls._2
    flacFileLocations.find(_ => true) match {
      case Some(firstFlacFileLocation) => findTagsAndDeleteFiles(tagsForUsers, directory, firstFlacFileLocation, flacFileLocations)
      case None => tagsForUsers
    }
  }

  def findTagsAndDeleteFiles(
                              tagsForUsers: Map[User, Set[Tags]], directory: FlacFileLocation,
                              firstFlacFileLocation: FlacFileLocation, flacFileLocations: SortedSet[FlacFileLocation])(implicit messageService: MessageService): Map[User, Set[Tags]] = {
    val owners = quickOwners(firstFlacFileLocation)
    val tags = firstFlacFileLocation.readTags match {
      case Right(tags) => Some(tags)
      case _ => None
    }
    flacFileLocations.foreach { flacFileLocation =>
      val encodedFileLocation = flacFileLocation.toEncodedFileLocation
      val deviceFileLocations: Set[DeviceFileLocation] = owners.map { user => encodedFileLocation.toDeviceFileLocation(user)}
      deviceFileLocations.foreach { deviceFileLocation =>
        Change.removed(deviceFileLocation, nowService.now()).store
      }
      deviceFileLocations.foreach(fileSystem.remove(_))
      fileSystem.remove(encodedFileLocation)
      fileSystem.move(flacFileLocation, flacFileLocation.toStagedFlacFileLocation)
    }
    owners.foldLeft(tagsForUsers) { (tagsForUsers, owner) =>
      tagsForUsers.get(owner) match {
        case Some(tagsSet) => tagsForUsers + (owner -> (tagsSet ++ tags))
        case _ => tagsForUsers + (owner -> tags.toSet)
      }
    }
  }


  /**
   * Find out quickly who owns an album by looking for links in the users' device repository.
   * @param flacFileLocation
   * @return
   */
  def quickOwners(flacFileLocation: FlacFileLocation): Set[User] = users.allUsers.filter { user =>
    flacFileLocation.toEncodedFileLocation.toDeviceFileLocation(user).exists
  }
}
