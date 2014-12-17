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

package common.owners

import common.configuration.User
import common.message.MessageService
import common.music.Tags

/**
 * Created by alex on 03/11/14.
 */
trait OwnerService {

  def listCollections()(implicit messageService: MessageService): Tags => Set[User]

  def own(user: User, tags: Set[Tags])(implicit messageService: MessageService): Unit

  def unown(user: User, tags: Set[Tags])(implicit messageService: MessageService): Unit
}
