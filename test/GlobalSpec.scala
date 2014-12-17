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

import controllers.{Commands, Music}
import org.specs2.mutable.Specification
import play.api.Play
import play.api.test.FakeApplication

/**
 * Created by alex on 20/11/14.
 */
class GlobalSpec extends Specification {

  "The application" should {
    "start and stop" in {
      val app = FakeApplication()
      Play.start(app)
      try {
        Global.getControllerInstance(classOf[Music]) must not(beNull)
        Global.getControllerInstance(classOf[Commands]) must not(beNull)
      }
      finally {
        Play.stop()
      }
    }
  }
}
