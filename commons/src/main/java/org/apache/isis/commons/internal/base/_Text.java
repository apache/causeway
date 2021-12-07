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
package org.apache.isis.commons.internal.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides common text processing algorithms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Text {

    private _Text() {}

    /**
     * Converts given {@code text} into a {@link Stream} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     * @apiNote Java 11+ provides {@code String.lines()}
     */
    public static Stream<String> streamLines(final @Nullable String text){
        return _Strings.splitThenStream(text, "\n")
                .map(s->s.replace("\r", ""));
    }

    /**
     * Converts given {@code text} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     */
    public static Can<String> getLines(final @Nullable String text){
        return Can.ofStream(streamLines(text));
    }

    public static Can<String> breakLines(final Can<String> lines, final int maxChars) {
        if(lines.isEmpty()) {
            return lines;
        }
        return lines.stream()
        .flatMap(line->breakLine(line, maxChars))
        .collect(Can.toCan());
    }

    /**
     * Reads content from given {@code input} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param input - nullable
     * @return non-null
     */
    public static Can<String> readLines(
            final @Nullable InputStream input,
            final @NonNull  Charset charset){
        if(input==null) {
            return Can.empty();
        }
        val lines = new ArrayList<String>();
        try(Scanner scanner = new Scanner(input, charset.name())){
            scanner.useDelimiter("\\n");
            while(scanner.hasNext()) {
                val line = scanner.next();
                lines.add(line.replace("\r", ""));
            }
        }
        return Can.ofCollection(lines);
    }

    @SneakyThrows
    public static Can<String> readLinesFromResource(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName,
            final @NonNull Charset charset) {
        try(val input = resourceLocation.getResourceAsStream(resourceName)){
            return readLines(input, charset);
        }
    }

    @SneakyThrows
    public static Can<String> readLinesFromUrl(
            final @NonNull URL url,
            final @NonNull Charset charset) {
        try(val input = url.openStream()){
            return readLines(input, charset);
        }
    }

    @SneakyThrows
    public static Can<String> readLinesFromFile(
            final @NonNull File file,
            final @NonNull Charset charset) {
        try(val input = new FileInputStream(file)){
            return readLines(input, charset);
        }
    }

    // -- WRITING

    @SneakyThrows
    public static void writeLinesToFile(
            final @NonNull Iterable<String> lines,
            final @NonNull File file,
            final @NonNull Charset charset) {

        try(val bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset))) {
            for(val line : lines) {
                bw.append(line).append("\n");
            }
        }
    }

    // -- NORMALIZING

    public static String normalize(final @Nullable String text) {
        if(text==null) {
            return "";
        }
        return normalize(getLines(text)).stream().collect(Collectors.joining("\n"));
    }

    /**
     * Converts given {@code lines} into a {@link Can} of lines,
     * with any empty lines removed that appear
     * <ul>
     * <li>before the first non-empty line</li>
     * <li>immediately after an empty line</li>
     * <li>after the last non-empty line</li>
     * </ul>
     * A line is considered non-empty,
     * if it contains non-whitespace characters.
     *
     * @param lines
     * @return non-null
     */
    public static Can<String> normalize(final @NonNull Can<String> lines) {
        return removeRepeatedEmptyLines(removeTrailingEmptyLines(removeLeadingEmptyLines(lines)));
    }

    /**
     * Converts given {@code lines} into a {@link Can} of lines,
     * with any empty lines removed that appear before the first non-empty line.
     * A line is considered non-empty,
     * if it contains non-whitespace characters.
     *
     * @param lines
     * @return non-null
     */
    public static Can<String> removeLeadingEmptyLines(final @NonNull Can<String> lines) {

        if(lines.isEmpty()) {
            return lines;
        }

        final int[] nonEmptyLineCount = {0};

        return lines.stream()
                // peek with side-effect
                .peek(line->{
                    if(hasNonWhiteSpaceChars(line)) nonEmptyLineCount[0]++;
                })
                .filter(line->nonEmptyLineCount[0]>0)
                .collect(Can.toCan());
    }

    /**
     * Converts given {@code lines} into a {@link Can} of lines,
     * with any empty lines removed that appear after the last non-empty line.
     * A line is considered non-empty,
     * if it contains non-whitespace characters.
     *
     * @param lines
     * @return non-null
     */
    public static Can<String> removeTrailingEmptyLines(final @NonNull Can<String> lines) {

        if(lines.isEmpty()) {
            return lines;
        }

        final int lastLineIndex = lines.size()-1;

        final int lastNonEmptyLineIndex = lines.stream()
        .mapToInt(indexAndlineToIntFunction((index, line)->hasNonWhiteSpaceChars(line) ? index : -1))
        .max()
        .orElse(-1);

        if(lastLineIndex == lastNonEmptyLineIndex) {
            return lines; // reuse immutable object
        }

        return lines.stream().limit(1L + lastNonEmptyLineIndex).collect(Can.toCan());
    }

    /**
     * Converts given {@code lines} into a {@link Can} of lines,
     * with any empty lines removed that appear immediately after an empty line.
     * A line is considered non-empty,
     * if it contains non-whitespace characters.
     *
     * @param lines
     * @return non-null
     */
    public static Can<String> removeRepeatedEmptyLines(final @NonNull Can<String> lines) {

        // we need at least 2 lines
        if(lines.size()<2) {
            return lines;
        }

        final int[] latestEmptyLineIndex = {-2};

        return streamLineObjects(lines)
        .peek(line->{
            if(!line.isEmpty()) {
                return; // ignore
            }
            if(latestEmptyLineIndex[0] == line.getIndex()-1) {
                line.setMarkedForRemoval(true);
            }
            latestEmptyLineIndex[0] = line.getIndex();
        })
        .filter(line->!line.isMarkedForRemoval())
        .map(Line::getString)
        .collect(Can.toCan());

    }

    // -- LOGGING SUPPORT

    public static String abbreviate(final @Nullable String input) {
        if(input==null) {
            return input;
        }
        var s = input;
        s = s.replace("org.apache.isis.", "..isis.");
        s = s.replace(".viewer.wicket.", "..wkt.");
        return s;
    }

    public static String abbreviate(final @Nullable Class<?> cls) {
        if(cls==null) {
            return "[none]";
        }
        return abbreviate(cls.getCanonicalName());
    }

    public static String abbreviateClassOf(final @Nullable Object obj) {
        if(obj==null) {
            return "[none]";
        }
        return abbreviate(obj.getClass());
    }

    // -- TESTING SUPPORT

    public static void assertTextEquals(final @Nullable String a, final @Nullable String b) {
        assertTextEquals(getLines(a), getLines(b));
    }

    public static void assertTextEquals(final @NonNull Can<String> a, final @Nullable String b) {
        assertTextEquals(a, getLines(b));
    }

    public static void assertTextEquals(final @Nullable String a, final @NonNull Can<String> b) {
        assertTextEquals(getLines(a), b);
    }

    public static void assertTextEquals(final @NonNull Can<String> a, final @NonNull Can<String> b) {

        val na = normalize(a);
        val nb = normalize(b);

        val lineNrRef = _Refs.intRef(0);

        if(na.size()<=nb.size()) {
            na.zip(nb, (left, right)->{
                final int lineNr = lineNrRef.incAndGet();
                _Assert.assertEquals(left, right, ()->String.format("first non matching lineNr %d", lineNr));
            });
        } else {
            nb.zip(na, (right, left)->{
                final int lineNr = lineNrRef.incAndGet();
                _Assert.assertEquals(left, right, ()->String.format("first non matching lineNr %d", lineNr));
            });
        }

        _Assert.assertEquals(na.size(), nb.size(), ()->String.format("normalized texts differ in number of lines"));
    }

    // -- HELPER

    private static boolean hasNonWhiteSpaceChars(final String s) {
        if(s==null) {
            return false;
        }
        return !s.trim().isEmpty();
    }

    @Getter
    private static class Line {
        private final int index; // zero based
        private final String string;
        private final boolean empty; // whether has no non-whitespace characters
        @Setter private boolean markedForRemoval;

        public Line(final int index, final String string) {
            this.index = index;
            this.string = string;
            this.empty = !hasNonWhiteSpaceChars(string);
        }

    }

    private static Stream<Line> streamLineObjects(final @NonNull Can<String> lines) {
        final int[] indexRef = {0};
        return lines.stream().map(line->new Line(indexRef[0]++, line));
    }

    private static interface IndexAwareLineToIntFunction {
        public int apply(int lineIndex, String line);
    }

    private static ToIntFunction<String> indexAndlineToIntFunction(final IndexAwareLineToIntFunction mapper) {
        final int[] indexRef = {0};
        return line->mapper.apply(indexRef[0]++, line);
    }

    private static Stream<String> breakLine(String line, final int maxChars) {
        line = line.trim();
        if(line.length()<=maxChars) {
            return Stream.of(line);
        }
        val tokens = Can.ofEnumeration(new StringTokenizer(line, " .-:/_", true))
                .map(String.class::cast);

        val constraintLines = _Lists.<String>newArrayList();
        val partialSum = _Refs.intRef(0);
        val partialCount = _Refs.intRef(0);

        val tokenIterator = tokens.iterator();

        tokens.stream()
        .mapToInt(String::length)
        .forEach(tokenLen->{

            final int nextLen = partialSum.getValue() + tokenLen;
            if(nextLen <= maxChars) {
                partialSum.update(x->nextLen);
                partialCount.incAndGet();
            } else {

                constraintLines.add(
                        IntStream.range(0, partialCount.getValue())
                            .mapToObj(__->tokenIterator.next())
                            .collect(Collectors.joining()));

                partialSum.update(x->tokenLen);
                partialCount.setValue(1);
            }
        });

        // add remaining
        constraintLines.add(
                IntStream.range(0, partialCount.getValue())
                    .mapToObj(__->tokenIterator.next())
                    .collect(Collectors.joining()));

        return constraintLines.stream();
    }

}
