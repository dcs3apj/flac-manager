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

import uk.co.unclealex.music.common.files.FileLocation;

/**
 * An Action is an instruction that tells an {@link ActionVisitor} to do
 * something. Each command calculates a list of actions that are then executed.
 * The idea behind this is that it is then easier to test each command as all
 * that is needed is to check the list of actions is as expected.
 * 
 * @author alex
 * 
 */
public interface Action {

  /**
   * Get the {@link FileLocation} this action is acting upon.
   * @return The {@link FileLocation} this action is acting upon.
   */
  public FileLocation getFileLocation();
  
  /**
   * Accept a call from an {@link ActionVisitor}.
   * @param actionVisitor The calling {@link ActionVisitor}.
   * @throws IOException 
   */
  public void accept(ActionVisitor actionVisitor) throws IOException;
}
