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
package org.apache.causeway.extensions.docgen.topics.domainobjects;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _DiagramUtils {

    String plantumlBlock(final String diagramSource) {
        return adocBlockHeader("plantuml")
                + adocDelimited("----", diagramSource);
    }

    String plantumlSourceBlock(final String diagramSource) {
        return adocLabel("Diagram Source")
            + adocCollapsibeBlock(
                    adocBlockHeader("source")
                    + adocLabel("Plantuml Syntax")
                    + adocDelimited("----", diagramSource));
    }

    String adocCollapsibeBlock(final String content) {
        return "[%collapsible]\n"
                + adocDelimited("====", content);
    }
    String adocBlockHeader(final String entry) {
        return "[" + entry + "]\n";
    }
    String adocDelimited(final String delimiter, final String content) {
        return delimiter + "\n"
            + content
            + "\n" + delimiter + "\n";
    }
    String adocLabel(final String label) {
        return "." + label + "\n";
    }

    String multilineLabel(final String...lines) {
        return Stream.of(lines).collect(Collectors.joining("\\n"));
    }

    String doubleQuoted(final String string) {
        return "\"" + string + "\"";
    }

    String objectId(final ObjectSpecification objSpec) {
        return objSpec.getLogicalType().getLogicalTypeName();
    }

    String objectName(final ObjectSpecification objSpec) {
        return multilineLabel(
                objectShortName(objSpec),
                "<" + objSpec.getLogicalType().getNamespace() + ">");
    }

    String objectShortName(final ObjectSpecification objSpec) {
        val simpleName = objSpec.getLogicalType().getLogicalTypeSimpleName();
        return simpleName;
    }

}
