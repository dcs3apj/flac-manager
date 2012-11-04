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

import uk.co.unclealex.music.common.DataObject;
import uk.co.unclealex.music.common.files.FileLocation;

/**
 * An action used to move a file.
 * 
 * @author alex
 * 
 */
public class MoveAction extends DataObject implements Action {

  /**
   * The location of the file to move.
   */
  private final FileLocation sourceLocation;
  
  /**
   * The location of where to move the file.
   */
  private final FileLocation targetLocation;

  /**
   * Instantiates a new move action.
   *
   * @param sourceLocation the source location
   * @param targetLocation the target location
   */
  public MoveAction(FileLocation sourceLocation, FileLocation targetLocation) {
    super();
    this.sourceLocation = sourceLocation;
    this.targetLocation = targetLocation;
  }
  
  /**
   * {@inheritDoc}
   */
  public void accept(ActionVisitor actionVisitor) throws IOException {
    actionVisitor.visit(this);
  }

  /**
   * Gets the location of the file to move.
   *
   * @return the location of the file to move
   */
  public FileLocation getSourceLocation() {
    return sourceLocation;
  }

  /**
   * Gets the location of where to move the file.
   *
   * @return the location of where to move the file
   */
  public FileLocation getTargetLocation() {
    return targetLocation;
  }
}
