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
package org.apache.causeway.commons.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities for text processing and text I/O.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class TextUtils {

    // -- LINE READING

    /**
     * Converts given {@code text} into a {@link Stream} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     * @apiNote Java 11+ provides {@code String.lines()}
     */
    public Stream<String> streamLines(final @Nullable String text){
        return _Strings.splitThenStream(text, "\n")
                .map(s->s.replace("\r", ""));
    }

    /**
     * Converts given {@code text} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     */
    public Can<String> readLines(final @Nullable String text){
        return Can.ofStream(streamLines(text));
    }

    /**
     * Reads content from given {@code input} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r}
     * and BOM file header (if any) in the process.
     * @param input - nullable
     * @return non-null
     */
    public Can<String> readLinesFromInputStream(
            final @Nullable InputStream input,
            final @NonNull  Charset charset){
        if(input==null) {
            return Can.empty();
        }
        var lines = new ArrayList<String>();
        try(Scanner scanner = new Scanner(input, charset.name())){
            scanner.useDelimiter("\\n");
            while(scanner.hasNext()) {
                var line = scanner.next()
                        .replace("\r", "");
                if(lines.size()==0) {
                    line = stripBom(line); // special handling of first line
                }
                lines.add(line);
            }
        }
        return Can.ofCollection(lines);
    }

    /**
     * Reads content from given resource into a {@link Can} of lines,
     * removing new line characters {@code \n,\r}
     * and BOM file header (if any) in the process.
     * @return non-null
     * @see #readLinesFromInputStream(InputStream, Charset)
     */
    @SneakyThrows
    public Can<String> readLinesFromResource(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName,
            final @NonNull Charset charset) {
        try(var input = resourceLocation.getResourceAsStream(resourceName)){
            return readLinesFromInputStream(input, charset);
        }
    }

    /**
     * Reads content from given {@link URL} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r}
     * and BOM file header (if any) in the process.
     * @return non-null
     * @see #readLinesFromInputStream(InputStream, Charset)
     */
    @SneakyThrows
    public Can<String> readLinesFromUrl(
            final @NonNull URL url,
            final @NonNull Charset charset) {
        try(var input = url.openStream()){
            return readLinesFromInputStream(input, charset);
        }
    }

    /**
     * Reads content from given {@link File} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r}
     * and BOM file header (if any) in the process.
     * @return non-null
     * @see #readLinesFromInputStream(InputStream, Charset)
     */
    @SneakyThrows
    public Can<String> readLinesFromFile(
            final @NonNull File file,
            final @NonNull Charset charset) {
        try(var input = new FileInputStream(file)){
            return readLinesFromInputStream(input, charset);
        }
    }

    /**
     * Reads content from given {@link DataSource} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r}
     * and BOM file header (if any) in the process.
     * @return non-null
     * @see #readLinesFromInputStream(InputStream, Charset)
     */
    public Can<String> readLinesFromDataSource(
            final @NonNull DataSource dataSource,
            final @NonNull Charset charset) {
        return dataSource.tryReadAsLines(charset)
                .valueAsNonNullElseFail();
    }

    // -- WRITING

    /**
     * Writes given lines to given {@link File},
     * using new line character {@code \n}.
     */
    @SneakyThrows
    public void writeLinesToFile(
            final @NonNull Iterable<String> lines,
            final @NonNull File file,
            final @NonNull Charset charset) {

        try(var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset))) {
            for(var line : lines) {
                bw.append(line).append("\n");
            }
        }
    }

    /**
     * Writes given lines to given {@link DataSink},
     * using new line character {@code \n}.
     */
    public void writeLinesToDataSink(
            final @NonNull Iterable<String> lines,
            final @NonNull DataSink dataSink,
            final @NonNull Charset charset) {

        dataSink.writeAll(os->{
            try(var bw = new BufferedWriter(new OutputStreamWriter(os, charset))) {
                for(var line : lines) {
                    bw.append(line).append("\n");
                }
            }
        });
    }

    // -- STRING DELIMITER

    public StringDelimiter delimiter(final @NonNull String delimiter) {
        return StringDelimiter.of(delimiter, new String[0]);
    }

    /**
     * Holder of immutable {@link String}[] elements, that provides
     * 'path like' composition and decomposition utilities.
     * <p>
     * Null or empty delimited elements are ignored.
     */
    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public final static class StringDelimiter {

        @Getter
        private final @NonNull String delimiter;
        private final @NonNull String[] elements;

        /**
         * Returns a new {@link StringDelimiter} instance splitting given {@code string}
         * into elements using {@link String} {@link #getDelimiter()} as delimiter.
         * <p>
         * Null or empty delimited elements are ignored.
         * @param string - null-able
         */
        public StringDelimiter parse(final @Nullable String string) {
            return new StringDelimiter(delimiter,
                _Strings.splitThenStream(string, delimiter)
                    .filter(_Strings::isNotEmpty)
                    .collect(_Arrays.toArray(String.class)));
        }

        /**
         * Streams the delimited {@link String} elements, this {@link StringDelimiter} is holding.
         */
        public Stream<String> stream() {
            return elementCount()>0
                    ? Stream.of(elements)
                    : Stream.empty();
        }

        /**
         * Returns a new {@link StringDelimiter} instance that has all the
         * delimited {@link String} elements of this and given {@code other}
         * {@link StringDelimiter} (in sequence).
         * @param other - null-able
         */
        public StringDelimiter join(final @Nullable StringDelimiter other) {
            return other!=null
                    ? new StringDelimiter(delimiter,
                            _Arrays.combine(this.elements, other.elements))
                    : this;
        }

        /**
         * Number of delimited {@link String} elements this {@link StringDelimiter} is holding.
         */
        public int elementCount() {
            return elements.length;
        }

        public String asDelimitedString() {
            return stream().collect(Collectors.joining(delimiter));
        }

        @Override
        public String toString() {
            return String.format("StringDelimiter[delimiter=%s,elements=%s]",
                    delimiter,
                    Arrays.asList(elements));
        }

        /**
         * Returns a new {@link StringDelimiter} instance that has all the
         * delimited {@link String} elements of this but instead uses given {@code newDelimiter}.
         */
        public StringDelimiter withDelimiter(final @NonNull String newDelimiter) {
            return new StringDelimiter(newDelimiter, elements);
        }

    }

    // -- STRING CUTTER

    public StringCutter cutter(final @NonNull String value) {
        return StringCutter.of(value);
    }

    /**
     * Holder of immutable {@link String} value, that provides
     * dropping of characters before or after search and match for
     * a specific character sequence.
     */
    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public final static class StringCutter {

        @Getter
        private final @NonNull String value;

        /**
         * Returns a new {@link StringCutter} holding a the string value as returned by given {@code mapper}.
         */
        public StringCutter map(final @NonNull UnaryOperator<String> mapper) {
            return StringCutter.of(mapper.apply(value));
        }

        /**
         * Whether the held string value contains the specified
         * sequence of char values.
         *
         * @param str the sequence to search for
         * @return true if the held string value contains {@code str},
         *  false otherwise (as well as when null or empty)
         */
        public boolean contains(final @Nullable CharSequence str) {
            return _Strings.isNullOrEmpty(str)
                    ? false
                    : value.contains(str);
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value and drops any characters that come before
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropBefore(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value and drops any characters that come after
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropAfter(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index + str.length()));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value and drops any characters that come before
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropBeforeLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value and drops any characters that come after
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropAfterLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index + str.length()));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value, keeps any characters that come before
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepBefore(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value, keeps any characters that come after
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepAfter(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index + str.length()));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value, keeps any characters that come before
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepBeforeLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value, keeps any characters that come after
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepAfterLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index + str.length()));
            }
            return this;
        }

        // -- HELPER

        private String matcherNotEmptyMsg() {
            return "can only match search and match for non-empty string";
        }

    }

    // -- HELPER

    /**
     * If line has a BOM 65279 (0xFEFF) leading character, strip it.
     * <p>
     * Some UTF-8 formatted files may have a BOM signature at their start.
     */
    private String stripBom(final String line) {
        if(line.length()>0
                && line.charAt(0)==65279) {
            return line.substring(1);
        }
        return line;
    }

}
