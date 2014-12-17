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

package common.musicbrainz


import org.specs2.mutable._

import scala.collection.JavaConversions._
import scala.collection.SortedSet
import scala.io.Source
import scala.util.Try

/**
 * Created by alex on 26/10/14.
 */
class MusicBrainzClientImplSpec extends Specification {

  implicit def rangeToSet(range: Range): Set[String] = {
    range.foldLeft(SortedSet.empty[String]) { (rs, r) => rs + f"${r}%03d"}
  }

  "The MusicBrainzClient" should {

    "Fail if the user does not have any collections" in new Server("ws-no-collections/root.txt") {
      val response = Try(client.relasesForOwner(user))
      response must beAFailedTry.withThrowable[IllegalStateException]
    }

    "Fail if the user's only collection is not correctly named" in new Server("ws-one-collection/root.txt") {
      val response = Try(client.relasesForOwner(user))
      response must beAFailedTry.withThrowable[IllegalStateException]
    }

    "Fail if the user has more than one collection and none of them are correctly named" in new Server("ws-two-collections-fail/root.txt") {
      val response = Try(client.relasesForOwner(user))
      response must beAFailedTry.withThrowable[IllegalStateException]
    }


    "Get the correct release IDs from a user's collection" in new Server("ws-two-collections-success/root.txt") {
      val expectedReleasesUrl = this.getClass.getClassLoader.getResource("expected-releases.txt")
      val expectedReleaseIds = Source.fromURL(expectedReleasesUrl).getLines().toList
      val response = client.relasesForOwner(user)
      response must containTheSameElementsAs(expectedReleaseIds)
    }

    "Send a PUT request for adding releases to a collection" in new Server("ws-two-collections-success/root.txt") {
      val response = logs(client.addReleases(user, 0 until 150))
      response must containTheSameElementsAs(expectedResultsForAlteringCollection("PUT", 150))
    }

    "Send DELETE request for removing releases from a collection" in new Server("ws-two-collections-success/root.txt") {
      val response = logs(client.removeReleases(user, 0 until 150))
      response must containTheSameElementsAs(expectedResultsForAlteringCollection("DELETE", 150))
    }

    def expectedResultsForAlteringCollection(method: String, releaseCount: Int) = (0 until releaseCount).toList.grouped(100).map { ids =>
      val formattedIds = ids.map(id => f"${id}%03d")
      s"$method:/ws/2/collection/bba2a722-0540-4260-b12d-1eae32760b9d/releases/${formattedIds.mkString(";")}"
    }.toList
  }

  class Server(val rootResource: String) extends After {

    lazy val context = new MusicBrainzTestContext(rootResource).setup

    lazy val client = context.musicBrainzClient
    lazy val user = context.user

    lazy val logs: Unit => List[String] = { _ =>
      context.requestLog.toList
    }

    override def after: Any = {
      context.shutdown
    }
  }

}
