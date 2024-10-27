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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.approvaltests.reporters.GenericDiffReporter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApprovalUtils {

    /**
     * Enables approvar testing's text compare for given file extension.
     * @param ext - should include the leading dot '.' like in say {@code .yaml}
     */
    public void registerFileExtensionForTextCompare(final String ext) {
        if(GenericDiffReporter.TEXT_FILE_EXTENSIONS.contains(ext)) {
            return; // nothing to do
        }
        final List<String> textFileExtensions = new ArrayList<>(GenericDiffReporter.TEXT_FILE_EXTENSIONS);
        textFileExtensions.add(ext);
        GenericDiffReporter.TEXT_FILE_EXTENSIONS = Collections.unmodifiableList(textFileExtensions);
    }

}
