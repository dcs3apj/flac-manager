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
import java.util.Arrays;
import java.util.List;

import uk.co.unclealex.music.configuration.User;
import uk.co.unclealex.music.files.FileLocation;

/**
 * An action used to delete a symbolic link from a device repository to the
 * encoded repository.
 * 
 * @author alex
 * 
 */
public class UnlinkAction extends AbstractAction implements Action {

  /**
   * The old owners of the file.
   */
  private final List<User> owners;

  /**
   * Instantiates a new unlink action.
   * 
   * @param fileLocation
   *          the file location
   * @param owners
   *          the owners
   */
  public UnlinkAction(FileLocation fileLocation, List<User> owners) {
    super(fileLocation);
    this.owners = owners;
  }

  /**
   * Instantiates a new unlink action.
   * 
   * @param fileLocation
   *          the file location
   * @param owners
   *          the owners
   */
  public UnlinkAction(FileLocation fileLocation, User... owners) {
    this(fileLocation, Arrays.asList(owners));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(ActionVisitor actionVisitor) throws IOException {
    actionVisitor.visit(this);
  }

  /**
   * Gets the old owners of the file.
   * 
   * @return the old owners of the file
   */
  public List<User> getOwners() {
    return owners;
  }
}
