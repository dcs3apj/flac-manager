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

package uk.co.unclealex.music.files;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedSet;

import javax.inject.Inject;

import uk.co.unclealex.music.exception.InvalidDirectoriesException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author alex
 * 
 */
public class DirectoryServiceImpl implements DirectoryService {

  /**
   * The file system against which directories are resolved.
   */
  private final FileSystem fileSystem;

  @Inject
  public DirectoryServiceImpl(FileSystem fileSystem) {
    super();
    this.fileSystem = fileSystem;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */
  @Override
  public SortedSet<FileLocation> listFiles(Path requiredBasePath, Iterable<Path> flacDirectories)
      throws InvalidDirectoriesException,
      IOException {
    Function<Path, Path> absoluteFunction = new Function<Path, Path>() {
      public Path apply(Path path) {
        return path.toAbsolutePath();
      }
    };
    final Path absoluteRequiredBasePath = absoluteFunction.apply(requiredBasePath);
    Iterable<Path> absoluteFlacDirectories = Iterables.transform(flacDirectories, absoluteFunction);
    Predicate<Path> isValidPathPredicate = new Predicate<Path>() {
      public boolean apply(Path path) {
        return Files.isDirectory(path) && path.startsWith(absoluteRequiredBasePath);
      }
    };
    Iterable<Path> invalidPaths = Iterables.filter(flacDirectories, Predicates.not(isValidPathPredicate));
    if (!Iterables.isEmpty(invalidPaths)) {
      throw new InvalidDirectoriesException("The following paths are either not directories or not a subpath of "
          + absoluteRequiredBasePath, invalidPaths);
    }
    SortedSet<FileLocation> allFlacFileLocations = Sets.newTreeSet();
    for (Path flacDirectory : absoluteFlacDirectories) {
      SortedSet<Path> flacFiles = findAllFiles(flacDirectory);
      for (Path flacFile : flacFiles) {
        allFlacFileLocations.add(new FileLocation(absoluteRequiredBasePath, absoluteRequiredBasePath
            .relativize(flacFile)));
      }
    }
    return allFlacFileLocations;
  }

  /**
   * Find all files under a path.
   * 
   * @param basePath
   *          The path to search.
   * @retun A sorted set of all the found paths.
   */
  protected SortedSet<Path> findAllFiles(Path basePath) throws IOException {
    final SortedSet<Path> flacFiles = Sets.newTreeSet();
    FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        flacFiles.add(file);
        return super.visitFile(file, attrs);
      }
    };
    Files.walkFileTree(basePath, visitor);
    return flacFiles;
  }

  public FileSystem getFileSystem() {
    return fileSystem;
  }

}
