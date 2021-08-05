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
package org.apache.isis.testing.unittestsupport.applib.matchers;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest {@link Matcher} implementations for {@link File}s.
 *
 * @since 2.0 {@index}
 */
public final class FileMatchers {

    private FileMatchers() { }

    public static Matcher<File> existsAndNotEmpty() {

        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(final Description arg0) {
                arg0.appendText("exists and is not empty");
            }

            @Override
            public boolean matchesSafely(final File f) {
                return f.exists() && f.length() > 0;
            }
        };
    }

    public static Matcher<File> equalsFile(final File file) throws IOException {
        final String canonicalPath = file.getCanonicalPath();
        return new TypeSafeMatcher<>() {

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
