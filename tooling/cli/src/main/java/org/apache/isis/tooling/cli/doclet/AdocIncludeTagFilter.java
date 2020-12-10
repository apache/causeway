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
package org.apache.isis.tooling.cli.doclet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Text;

final class AdocIncludeTagFilter {
    
    public static String read(File source) {
        return _Text.readLinesFromFile(source, StandardCharsets.UTF_8).stream()
        //.filter(line->!containsIncludeTag(line))
        .filter(line->!isAllLineComment(line))
        .map(AdocIncludeTagFilter::removeFootNote)
        .collect(Collectors.joining("\n"));
    }
    
    // -- HELPER

//    private static boolean containsIncludeTag(String line) {
//        line = line.trim();
//        if(!line.startsWith("//")) {
//            return false;
//        }
//        return line.contains(" tag::")
//                || line.contains(" end::");
//    }

    private static boolean isAllLineComment(String line) {
        return line.trim().startsWith("//");
    }
    
    private static String removeFootNote(String line) {
        return line.replace("// <.>", "").stripTrailing();
    }
    
}
