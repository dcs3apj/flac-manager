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

package sync

import java.nio.file.{Path, Paths}
import java.text.SimpleDateFormat

import common.configuration.{Directories, User}
import common.files._
import common.message.MessageTypes._
import common.message._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * Created by alex on 29/10/14.
 */
class SynchronisationManagerImplSpec extends Specification with Mockito {

  import sync.Implicits._

  trait context extends Scope {
    implicit val directories = Directories(Paths.get("/flac"), Paths.get("/devices"), Paths.get("/encoded"), Paths.get("/staging"), Paths.get("/temp"))
    val user: User = User("freddie", "", "", "")
    val deviceConnectionService = mock[DeviceConnectionService]
    implicit val messageService: TestMessageService = mock[TestMessageService]
    val ID = "ID"
  }

  "The synchronisation manager" should {
    "Add a music file that is not on the device" in new context {
      val fileLocation: DeviceFileLocation = DeviceFileLocation(user, "b.txt")
      implicit val fileLocationUtils = MapLastModified(
        fileLocation -> "05/09/1972 10:15:00")
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      device.listDeviceFiles returns Set()
      synchronisationManager.synchroniseFiles(device, fileLocationUtils.fileLocations)
      there was one(device).add(fileLocation)
      there was one(messageService).printMessage(SYNC_ADD(fileLocation))
      there were no(device).remove(any[DeviceFile])
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Remove a device file that no longer exists" in new context {
      implicit val fileLocationUtils = MapLastModified()
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      val deviceFile: DeviceFile = DeviceFile(ID, "a.txt", "05/09/1972 09:12:00")
      device.listDeviceFiles returns Set(deviceFile)
      synchronisationManager.synchroniseFiles(device, fileLocationUtils.fileLocations)
      there were no(device).add(any[DeviceFileLocation])
      there was one(device).remove(deviceFile)
      there was one(messageService).printMessage(SYNC_REMOVE(deviceFile))
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Overwrite an outdated device file" in new context {
      val fileLocation = DeviceFileLocation(user, "a.txt")
      implicit val fileLocationUtils = MapLastModified(fileLocation -> "05/09/1973 09:12:00")
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      val deviceFile: DeviceFile = DeviceFile(ID, "a.txt", "05/09/1972 09:12:00")
      device.listDeviceFiles returns Set(deviceFile)
      synchronisationManager.synchroniseFiles(device, fileLocationUtils.fileLocations)
      there was one(device).add(fileLocation)
      there were no(device).remove(any[DeviceFile])
      there was one(messageService).printMessage(SYNC_ADD(fileLocation))
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Keep a device file that is exactly one hour older than a music file" in new context {
      val fileLocation = DeviceFileLocation(user, "a.txt")
      implicit val fileLocationUtils = MapLastModified(fileLocation -> "05/09/1972 10:12:00")
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      val deviceFile: DeviceFile = DeviceFile(ID, "a.txt", "05/09/1972 09:12:00")
      device.listDeviceFiles returns Set(deviceFile)
      synchronisationManager.synchroniseFiles(device, fileLocationUtils.fileLocations)
      there were no(device).add(any[DeviceFileLocation])
      there were no(device).remove(any[DeviceFile])
      there was one(messageService).printMessage(SYNC_KEEP(deviceFile))
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Keep a device file that is exactly one hour newer than a music file" in new context {
      val fileLocation = DeviceFileLocation(user, "a.txt")
      implicit val fileLocationUtils = MapLastModified(fileLocation -> "05/09/1972 09:12:00")
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      val deviceFile: DeviceFile = DeviceFile(ID, "a.txt", "05/09/1972 10:12:00")
      device.listDeviceFiles returns Set(deviceFile)
      synchronisationManager.synchroniseFiles(device, fileLocationUtils.fileLocations)
      there were no(device).add(any[DeviceFileLocation])
      there were no(device).remove(any[DeviceFile])
      there was one(messageService).printMessage(SYNC_KEEP(deviceFile))
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Successfully mount and unmount a device after a successful transfer" in new context {
      implicit val fileLocationUtils = MapLastModified()
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      device.listDeviceFiles returns Set()
      val result = synchronisationManager.synchronise(device, fileLocationUtils.fileLocations)
      there was one(deviceConnectionService).mount(anyString)
      there was one(device).beforeMount
      there was one(device).afterMount(any[Path])
      there was one(deviceConnectionService).unmount(any[Path])
      there was one(device).beforeUnmount
      there was one(device).afterUnmount
      result must beASuccessfulTry[Unit]
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }

    "Successfully mount and unmount a device after an unsuccessful transfer" in new context {
      implicit val fileLocationUtils = MapLastModified()
      val synchronisationManager = new SynchronisationManagerImpl(deviceConnectionService)
      val device = mock[Device]
      device.listDeviceFiles throws new RuntimeException("D'oh!")
      val result = synchronisationManager.synchronise(device, fileLocationUtils.fileLocations)
      there was one(deviceConnectionService).mount(anyString)
      there was one(device).beforeMount
      there was one(device).afterMount(any[Path])
      there was one(deviceConnectionService).unmount(any[Path])
      there was one(device).beforeUnmount
      there was one(device).afterUnmount
      result must beAFailedTry[Unit]
      result.failed.get must beAnInstanceOf[RuntimeException]
      there were noMoreCallsTo(messageService, deviceConnectionService)
    }
  }


}

class MapLastModified(m: Map[DeviceFileLocation, String]) extends TestFileLocationExtensions {

  import sync.Implicits._

  val fileLocations = m.keys

  override def lastModified(fileLocation: FileLocation): Long = {
    m.get(fileLocation.asInstanceOf[DeviceFileLocation]).get
  }

  /**
   * Return true if the file location points to a directory, false otherwise.
   * @param fileLocation
   * @return
   */
  override def isDirectory(fileLocation: FileLocation): Boolean = false

  override def createTemporaryFileLocation()(implicit directories: Directories): TemporaryFileLocation = null

  override def exists(fileLocation: FileLocation): Boolean = false
}

object MapLastModified {

  def apply(es: (DeviceFileLocation, String)*): MapLastModified = new MapLastModified(Map(es: _*))
}

object Implicits {
  implicit def stringToInstance(str: String): Long = {
    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(str).getTime
  }

  implicit def stringToPath(str: String): Path = Paths.get(str)
}