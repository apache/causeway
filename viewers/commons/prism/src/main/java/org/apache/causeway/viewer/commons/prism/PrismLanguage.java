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
package org.apache.causeway.viewer.commons.prism;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PrismLanguage {
    MARKUP("markup"),
    CSS("css"),
    CLIKE("clike"),
    JAVA("java"),
    JAVASCRIPT("javascript"),
    ASCIIDOC("asciidoc"),
    JAVADOCLIKE("javadoclike"),
    JAVADOC("javadoc"),
    JSON("json"),
    PROPERTIES("properties"),
    XML_DOC("xml-doc"),
    YAML("yaml"),
    ;
    final String languageSuffix;
    public String jsFile() {
        return "prism/components/prism-" + languageSuffix + ".min.js";
    }

    public static List<PrismLanguage> mostCommon() {
        //XXX order matters, eg. JAVADOCLIKE must come before JAVADOC
        //XXX future extensions might want to make that a config option
        return Arrays.asList(PrismLanguage.values());
    }

}
