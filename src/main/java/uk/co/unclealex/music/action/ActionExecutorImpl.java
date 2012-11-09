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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import uk.co.unclealex.music.command.checkin.covers.ArtworkService;
import uk.co.unclealex.music.command.checkout.EncodingService;
import uk.co.unclealex.music.files.FileLocation;
import uk.co.unclealex.music.files.FileUtils;
import uk.co.unclealex.music.message.MessageService;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The default implementation of {@link ActionExecutor}.
 * 
 * @author alex
 * 
 */
public class ActionExecutorImpl extends ActionVisitor.Default implements ActionExecutor {

  /**
   * The {@link MessageService} used to display messages to the user.
   */
  private final MessageService messageService;

  /**
   * The {@link FileUtils} used to move and write protect files.
   */
  private final FileUtils fileUtils;

  /**
   * The {@link ArtworkService} used to add cover art to a file.
   */
  private final ArtworkService artworkService;
  
  /**
   * The {@link EncodingService} used to encode FLAC files to MP3 files.
   */
  private final EncodingService encodingService;
  
  @Inject
  public ActionExecutorImpl(
      MessageService messageService,
      FileUtils fileUtils,
      ArtworkService artworkService,
      EncodingService encodingService) {
    super();
    this.messageService = messageService;
    this.fileUtils = fileUtils;
    this.artworkService = artworkService;
    this.encodingService = encodingService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Action action) throws IOException {
    action.accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(ProtectAction protectAction) throws IOException {
    FileLocation fileLocation = protectAction.getFileLocation();
    getMessageService().printMessage(MessageService.PROTECT, fileLocation.resolve());
    getFileUtils().alterWriteable(fileLocation, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */
  @Override
  public void visit(UnprotectAction unprotectAction) throws IOException {
    FileLocation fileLocation = unprotectAction.getFileLocation();
    getMessageService().printMessage(MessageService.UNPROTECT, fileLocation.resolve());
    getFileUtils().alterWriteable(fileLocation, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(MoveAction moveAction) throws IOException {
    FileLocation sourceFileLocation = moveAction.getFileLocation();
    FileLocation targetFileLocation = moveAction.getTargetFileLocation();
    getMessageService().printMessage(MessageService.MOVE, sourceFileLocation.resolve(), targetFileLocation.resolve());
    getFileUtils().move(sourceFileLocation, targetFileLocation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(DeleteAction deleteAction) throws IOException {
    FileLocation fileLocation = deleteAction.getFileLocation();
    getMessageService().printMessage(MessageService.DELETE, fileLocation.resolve());
    getFileUtils().remove(fileLocation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(EncodeAction encodeAction) throws IOException {
    FileLocation flacFileLocation = encodeAction.getFileLocation();
    FileLocation encodedFileLocation = encodeAction.getEncodedFileLocation();
    getMessageService().printMessage(MessageService.ENCODE, flacFileLocation.resolve(), encodedFileLocation.resolve());
    getEncodingService().encode(flacFileLocation, encodeAction.getFlacMusicFile(), encodedFileLocation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(AddArtworkAction addArtworkAction) throws IOException {
    URI coverArtUrl = addArtworkAction.getCoverArtUri();
    getMessageService().printMessage(MessageService.ARTWORK, addArtworkAction.getFileLocation(), coverArtUrl);
    getArtworkService().addArwork(addArtworkAction.getFileLocation().resolve(), coverArtUrl);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(FailureAction failureAction) {
    List<Object> allParameters = Lists.newArrayList((Object) failureAction.getFileLocation().resolve());
    allParameters.addAll(Arrays.asList(failureAction.getParameters()));
    getMessageService()
        .printMessage(failureAction.getMessageTemplate(), Iterables.toArray(allParameters, Object.class));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CoverArtAction coverArtAction) {
    // Default is to do nothing.
  }

  public FileUtils getFileUtils() {
    return fileUtils;
  }

  public MessageService getMessageService() {
    return messageService;
  }

  public ArtworkService getArtworkService() {
    return artworkService;
  }

  public EncodingService getEncodingService() {
    return encodingService;
  }

}