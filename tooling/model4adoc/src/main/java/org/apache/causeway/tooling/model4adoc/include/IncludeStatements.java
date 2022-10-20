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
package org.apache.causeway.tooling.model4adoc.include;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;

import lombok.NonNull;
import lombok.val;

public final class IncludeStatements {

    // -- UTILITIES

    public static Can<IncludeStatement> find(
            final @NonNull Iterable<String> lines) {

        val matches = _Lists.<IncludeStatement>newArrayList();
        visit(lines, (line, incl)->incl.ifPresent(matches::add));
        return Can.ofCollection(matches);
    }

    /**
     * @param lines input from eg. a file
     * @param rewriter - receives all include statements found,
     *          when returning null, means the current line stays unmodified
     * @return updated lines ready to be eg. written to file
     */
    public static Can<String> rewrite(
            final @NonNull Iterable<String> lines,
            final @NonNull UnaryOperator<IncludeStatement> rewriter) {

        val processedLines = _Lists.<String>newArrayList();
        visit(lines, (originalLine, inclOptional)->{

            val incl = inclOptional.map(rewriter::apply).orElse(null);
            if(incl!=null) {
                processedLines.add(incl.toAdocAsString());
            } else {
                processedLines.add(originalLine);
            }

        });
        return Can.ofCollection(processedLines);
    }

    // -- HELPER

    private static void visit(
            final Iterable<String> lines,
            final BiConsumer<String, Optional<IncludeStatement>> onLine) {

        int zeroBasedLineIndex = 0;

        for(val line : lines) {

            // include::[version@]component:module:page$relative-path

            if(line.startsWith("include::")) {

                val acc = _Refs.stringRef(line);

                acc.cutAtIndex("include::".length());

                val incl = IncludeStatement.builder();
                incl.matchingLine(line);
                incl.zeroBasedLineIndex(zeroBasedLineIndex);

                if(acc.contains("@")) {
                    incl.version(acc.cutAtIndexOfAndDrop("@"));
                }

                incl.component(acc.cutAtIndexOfAndDrop(":"));
                incl.module(acc.cutAtIndexOfAndDrop(":"));
                incl.type(acc.cutAtIndexOfAndDrop("$"));

                final String referencePath;
                if(acc.contains("[")) {
                    referencePath = acc.cutAtIndexOf("[");
                    incl.options(acc.getValue());
                } else {
                    referencePath = acc.getValue();
                }

                acc.setValue(referencePath);

                val namespaceAsString = acc.cutAtLastIndexOfAndDrop("/");

                incl.namespace(Can.ofStream(_Strings.splitThenStream(namespaceAsString, "/")));

                incl.canonicalName(acc.cutAtLastIndexOfAndDrop("."));
                incl.ext(acc.getValue());

                onLine.accept(line, Optional.of(incl.build()));
            } else {
                onLine.accept(line, Optional.empty());
            }

            zeroBasedLineIndex++;
        }

    }


}
