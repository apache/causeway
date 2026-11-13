/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.testing.unittestsupport.applib.util;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class Files_deleteFileUtils_Test {

    private FileUtils.Deleter deleter = Mockito.mock(FileUtils.Deleter.class);

    @Test
    void test() throws IOException {
        final File cusIdxFile = new File("xml/objects/CUS.xml").getAbsoluteFile();
        final File cus1File = new File("xml/objects/CUS/1.xml").getAbsoluteFile();
        final File cus2File = new File("xml/objects/CUS/2.xml").getAbsoluteFile();

        FileUtils.deleteFiles(
                new File("xml/objects").getAbsoluteFile(),
                FileUtils.filterFileNameExtension(".xml"),
                FileUtils.Recursion.DO_RECURSE,
                deleter);

        verify(deleter, times(1)).deleteFile(Mockito.argThat(equalsFile(cusIdxFile)));
        verify(deleter, times(1)).deleteFile(Mockito.argThat(equalsFile(cus1File)));
        verify(deleter, times(1)).deleteFile(Mockito.argThat(equalsFile(cus2File)));
        verify(deleter, times(3)).deleteFile(Mockito.any(File.class));
    }

    private static ArgumentMatcher<File> equalsFile(final File file) throws IOException {
        return file1 -> {
            try {
                return file1.getCanonicalPath().equals(file.getCanonicalPath());
            } catch (IOException e) {
                return false;
            }
        };
    }

}
