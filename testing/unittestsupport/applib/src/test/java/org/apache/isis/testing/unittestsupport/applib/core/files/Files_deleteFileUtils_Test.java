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
package org.apache.isis.testing.unittestsupport.applib.core.files;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2;
import org.apache.isis.testing.unittestsupport.applib.util.FileUtils;


public class Files_deleteFileUtils_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private FileUtils.Deleter deleter;

    @Test
    public void test() throws IOException {
        final File cusIdxFile = new File("xml/objects/CUS.xml").getAbsoluteFile();
        final File cus1File = new File("xml/objects/CUS/1.xml").getAbsoluteFile();
        final File cus2File = new File("xml/objects/CUS/2.xml").getAbsoluteFile();
        context.checking(new Expectations() {
            {
                oneOf(deleter).deleteFile(with(equalsFile(cusIdxFile)));
                oneOf(deleter).deleteFile(with(equalsFile(cus1File)));
                oneOf(deleter).deleteFile(with(equalsFile(cus2File)));
            }
        });

        FileUtils.deleteFiles(
                new File("xml/objects").getAbsoluteFile(), 
                FileUtils.filterFileNameExtension(".xml"),
                FileUtils.Recursion.DO_RECURSE,
                deleter);
    }


    private static Matcher<File> equalsFile(final File file) throws IOException {
        final String canonicalPath = file.getCanonicalPath();
        return new TypeSafeMatcher<File>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("file '" + canonicalPath + "'");
            }

            @Override
            public boolean matchesSafely(File arg0) {
                try {
                    return arg0.getCanonicalPath().equals(canonicalPath);
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }


}
