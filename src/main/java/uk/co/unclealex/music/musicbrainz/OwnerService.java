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
 *   http://www.apache.org/licenses/LICENSE-2.0
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

package uk.co.unclealex.music.musicbrainz;

import java.util.Set;

import uk.co.unclealex.music.MusicFile;
import uk.co.unclealex.music.configuration.User;

/**
 * An interface for classes that can determine who owns a release. Owning a release is synonymous with a release
 * being in a MusicBrainz user's collection.
 * 
 * @author alex
 * 
 */
public interface OwnerService {

  /**
   * Get a set of users who own a {@link MusicFile}.
   * @param musicFile The {@link MusicFile} to check.
   * @return A set of users who own the file.
   */
  public Set<User> getOwnersForMusicFile(MusicFile musicFile);
  
  /**
   * Check to see if a {@link MusicFile} is owned by anyone.
   * @param musicFile The {@link MusicFile} to check.
   * @return True if the file has at least one owner, false otherwise.
   */
  public boolean isFileOwnedByAnyone(MusicFile musicFile);
  
  /**
   * Get the set of users who have valid collections.
   * @return The set of users who have valid collections.
   */
  public Set<User> getAllInvalidOwners();
}
