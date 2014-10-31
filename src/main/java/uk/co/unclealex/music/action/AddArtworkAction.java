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

package uk.co.unclealex.music.action;

import java.io.IOException;
import java.net.URI;

import uk.co.unclealex.music.files.FileLocation;

/**
 * An action used add artwork to a FLAC file.
 * 
 * @author alex
 * 
 */
public class AddArtworkAction extends AbstractAction implements Action {

  /**
   * The URL where cover art information is available.
   */
  private final URI coverArtUri;
    
  /**
   * Instantiates a new adds the artwork action.
   *
   * @param fileLocation the location
   * @param coverArtUri the cover art url
   */
  public AddArtworkAction(FileLocation fileLocation, URI coverArtUri) {
    super(fileLocation);
    this.coverArtUri = coverArtUri;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(ActionVisitor actionVisitor) throws IOException {
    actionVisitor.visit(this);
  }

  /**
   * Gets the URL where cover art information is available.
   *
   * @return the URL where cover art information is available
   */
  public URI getCoverArtUri() {
    return coverArtUri;
  }
}
