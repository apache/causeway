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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.approvaltests.core.Options;
import org.approvaltests.core.Scrubber;
import org.approvaltests.reporters.GenericDiffReporter;

import org.apache.causeway.commons.io.TextUtils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApprovalUtils {

    /**
     * Enables approval testing's text compare for given file extension.
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

    /**
     * Creates new approval test options with a scrubber, to normalize line ending characters
     * <p>
     * Usage:<br>
     * {@code Approvals.verify(source, ApprovalUtils.ignoreLineEndings());}
     * <p>
     * Usage when appending to existing options:<br>
     * {@code Approvals.verify(source, options.withScrubber(ApprovalUtils.ignoreLineEndings()::scrub));}
     */
    public Options ignoreLineEndings() {
        return new Options().withScrubber(s -> TextUtils.readLines(s).join("\n"));
    }

    public Scrubber jsonPropertiesSortedWhenParentedBy(final Predicate<String> applicableForParentKey) {
        return new JsonPropertiesSortedWhenParentedByKey(applicableForParentKey);
    }

    // -- HELPER

    record JsonPropertiesSortedWhenParentedByKey(Predicate<String> applicableForParentKey) implements Scrubber {

        @SneakyThrows
        @Override
        public String scrub(final String input) {

            var rootNode = new ObjectMapper().readTree(input);

            traverse(rootNode, "");

            String prettyJson = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(rootNode);

            return prettyJson;
        }

        // -- INTERNALS

        /**
         * Recursively traverses all nodes of the given tree and conditionally sorts the properties.
         */
        private void traverse(final JsonNode node, final String parentKey) {

            switch (node.getNodeType()) {
                case OBJECT -> {

                    // conditionally apply property sorting
                    if(applicableForParentKey.test(parentKey)) {

                        var objectNode = (ObjectNode)node;

                        var sortedPropertyEntries = node.properties().stream()
                            .sorted((a, b)->a.getKey().compareTo(b.getKey()))
                            .collect(Collectors.toList());

                        objectNode.removeAll();

                        sortedPropertyEntries.forEach(entry ->
                            objectNode.set(entry.getKey(), entry.getValue()));
                    }

                    node.properties().forEach(entry -> {
                        traverse(entry.getValue(), entry.getKey());
                    });
                }
                case ARRAY -> {
                    for (JsonNode element : node) {
                        traverse(element, parentKey);
                    }
                }
                default -> {}
            }

        }

    }

}
