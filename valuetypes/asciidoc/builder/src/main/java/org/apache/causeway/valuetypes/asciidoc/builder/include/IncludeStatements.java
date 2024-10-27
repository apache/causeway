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
package org.apache.causeway.valuetypes.asciidoc.builder.include;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.io.TextUtils;

import lombok.NonNull;

public final class IncludeStatements {

    // -- UTILITIES

    public static Can<IncludeStatement> find(
            final @NonNull Iterable<String> lines) {

        var matches = _Lists.<IncludeStatement>newArrayList();
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

        var processedLines = _Lists.<String>newArrayList();
        visit(lines, (originalLine, inclOptional)->{

            var incl = inclOptional.map(rewriter::apply).orElse(null);
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

        for(var line : lines) {

            // include::[version@]component:module:page$relative-path

            if(line.startsWith("include::")) {

                var cutter = TextUtils.cutter(line)
                        .keepAfter("include::");

                var incl = IncludeStatement.builder();
                incl.matchingLine(line);
                incl.zeroBasedLineIndex(zeroBasedLineIndex);

                if(cutter.contains("@")) {
                    incl.version(cutter.keepBefore("@").getValue());
                    cutter = cutter.keepAfter("@");
                }

                incl.component(cutter.keepBefore(":").getValue());
                cutter = cutter.keepAfter(":");

                incl.module(cutter.keepBefore(":").getValue());
                cutter = cutter.keepAfter(":");

                incl.type(cutter.keepBefore("$").getValue());
                cutter = cutter.keepAfter(":");

                final String referencePath;
                if(cutter.contains("[")) {
                    referencePath = cutter.keepBefore("[").getValue();
                    cutter = cutter.keepAfter("[");
                    incl.options(cutter.getValue());
                } else {
                    referencePath = cutter.getValue();
                }

                cutter = TextUtils.cutter(referencePath);

                var namespaceAsString = cutter.keepBeforeLast("/").getValue();

                incl.namespace(Can.ofStream(_Strings.splitThenStream(namespaceAsString, "/")));

                incl.canonicalName(cutter.keepBeforeLast(".").getValue());
                incl.ext(cutter.keepAfter(".").getValue());

                onLine.accept(line, Optional.of(incl.build()));
            } else {
                onLine.accept(line, Optional.empty());
            }

            zeroBasedLineIndex++;
        }

    }

}
