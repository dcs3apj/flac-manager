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

import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.unclealex.music.exception.InvalidDirectoriesException;
import uk.co.unclealex.music.files.DirectoryServiceImpl;

import com.google.common.collect.Lists;

/**
 * @author alex
 * 
 */
public class DirectoryServiceImplTest {

  private static final Logger log = LoggerFactory.getLogger(DirectoryServiceImplTest.class);

  Path testDirectory;
  DirectoryServiceImpl flacDirectoryServiceImpl;

  @Before
  public void createRepository() throws IOException {
    testDirectory = Files.createTempDirectory("directory-service-impl-test-");
    flacDirectoryServiceImpl = new DirectoryServiceImpl(FileSystems.getDefault());
    log.info("Using directory " + testDirectory);
    for (Path path : new Path[] {
        Paths.get("dir.flac", "myfile.flac"),
        Paths.get("dir.flac", "myfile.xml"),
        Paths.get("dir.flac", "inner", "myfile.flac"),
        Paths.get("dir.flac", "inner", "myfile.xml"),
        Paths.get("my.flac"),
        Paths.get("my.xml"),
        Paths.get("dir", "your.flac"),
        Paths.get("dir", "your.mp3") }) {
      Path fullPath = testDirectory.resolve(path);
      Files.createDirectories(fullPath.getParent());
      Files.createFile(fullPath);
    }
  }

  @Test
  public void testListFilesSuccess() throws InvalidDirectoriesException, IOException {
    SortedSet<FileLocation> actualFiles =
        flacDirectoryServiceImpl.listFiles(
            testDirectory,
            Lists.newArrayList(testDirectory.resolve("dir.flac"), testDirectory.resolve("dir")));
    Assert.assertThat(
        "The wrong files were found.",
        actualFiles,
        containsInAnyOrder(
            fileLocation("dir.flac", "myfile.flac"),
            fileLocation("dir.flac", "inner", "myfile.flac"),
            fileLocation("dir", "your.flac"),
            fileLocation("dir.flac", "myfile.xml"),
            fileLocation("dir.flac", "inner", "myfile.xml"),
            fileLocation("dir", "your.mp3")
            ));
  }

  protected FileLocation fileLocation(String first, String... more) {
    return new FileLocation(testDirectory, Paths.get(first, more));
  }
  
  @Test
  public void testListFilesFail() throws IOException {
    try {
      flacDirectoryServiceImpl.listFiles(
          testDirectory,
          Lists.newArrayList(
              testDirectory.resolve("dir.flac"),
              testDirectory.getParent(),
              testDirectory.resolve("my.xml")));
      Assert.fail("Invalid directories did not fail.");
    }
    catch (InvalidDirectoriesException e) {
      Assert.assertThat(
          "The wrong files were marked as invalid.",
          e.getInvalidDirectories(),
          containsInAnyOrder(
              testDirectory.getParent(),
              testDirectory.resolve("my.xml")));
    }
  }

  @After
  public void removeTestDirectory() throws IOException {
    if (testDirectory != null) {
      removeRecurisvely(testDirectory.toFile());
    }
  }

  protected void removeRecurisvely(File f) throws IOException {
    f.setWritable(true);
    if (f.isDirectory()) {
      for (File child : f.listFiles()) {
        removeRecurisvely(child);
      }
    }
    f.delete();
  }

}