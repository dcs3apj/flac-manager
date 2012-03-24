/**
 * Copyright 2011 Alex Jones
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

package uk.co.unclealex.music.files;

import java.nio.file.Path;

import uk.co.unclealex.music.common.MusicTrack;
import uk.co.unclealex.music.common.MusicType;

/**
 * 
 * A service for translating between file names and {@link MusicTrack}s.
 * @author alex
 *
 */
public interface FilenameService {

	/**
	 * Convert a path into a music track.
	 * @param path The path to convert.
	 * @return The music track represented at the given path.
	 */
	public MusicTrack toMusicTrack(Path path);
	
	/**
	 * Convert a {@link MusicTrack} into a path.
	 * @param basePath The required parent path of the created path.
	 * @param musicTrack The {@link MusicTrack} 
	 * @param musicType The type of the track being converted.
	 * @param precedeWithFirstLetter True if the top level directory should be the first letter of the artist, false otherwise.
	 * @return A path representing the supplied {@link MusicTrack}.
	 */
	public Path toPath(Path basePath, MusicTrack musicTrack, MusicType musicType, boolean precedeWithFirstLetter);
}
