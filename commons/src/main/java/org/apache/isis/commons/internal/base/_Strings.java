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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Bytes.BytesOperator;
import org.apache.isis.commons.internal.functions._Predicates;

import static org.apache.isis.commons.internal.base._NullSafe.size;
import static org.apache.isis.commons.internal.base._Strings_SplitIterator.splitIterator;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;
import static org.apache.isis.commons.internal.base._With.requiresNotEmpty;
import static org.apache.isis.commons.internal.functions._Predicates.not;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides String related algorithms.
 * </p><p>
 * Keep the public methods simple, these are basic building blocks for more complex composites.
 * Composites are provided as static fields.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Strings {

    private _Strings() {}

    // -- CONSTANTS

    /**
     * Convenient e.g. for toArray conversions
     * (a duplicate of in {@link _Constants#emptyStringArray} )
     */
    public static final String[] emptyArray = new String[0];

    // -- PAIR OF STRINGS

    public static interface KeyValuePair extends Map.Entry<String, String> { }

    public static KeyValuePair pair(final String key, final String value){
        return _Strings_KeyValuePair.of(key, value);
    }

    /**
     * Parses a string assumed to be of the form <kbd>key[separator]value</kbd> into its parts.
     * @param keyValueLiteral
     * @param separator
     * @return a non-empty Optional, if (and only if) the {@code keyValueLiteral}
     * does contain at least one {@code separator}
     */
    public static Optional<KeyValuePair> parseKeyValuePair(@Nullable final String keyValueLiteral, final char separator) {
        return _Strings_KeyValuePair.parse(keyValueLiteral, separator);
    }

    // -- FILLING

    public static String of(final int length, final char c) {
        if(length<=0) {
            return "";
        }
        final char[] chars = new char[length];
        Arrays.fill(chars, c);
        return String.valueOf(chars);
    }

    // -- COMPARE

    /**
     * Compares two strings lexicographically (and nulls-frist).
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with
     * {@link Comparator#nullsFirst(Comparator)}.
     * @implNote this utility method does not produce objects on the heap
     * @param a
     * @param b
     * @return {@code -1} if {@code a < b}, {@code 1} if {@code a > b} else {@code 0}
     * @see String#compareTo(String)
     */
    public static int compareNullsFirst(final @Nullable String a, final @Nullable String b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins
        if(a==null) {
            return -1;
        }
        if(b==null) {
            return 1;
        }
        // at this point neither can be null
        return a.compareTo(b);
    }

    /**
     * Compares two strings lexicographically (and nulls-last).
     * @apiNote consider using {@link Comparator#naturalOrder()} combined with
     * {@link Comparator#nullsFirst(Comparator)}.
     * @implNote this utility method does not produce objects on the heap
     * @param a
     * @param b
     * @return {@code -1} if {@code a < b}, {@code 1} if {@code a > b} else {@code 0}
     * @see String#compareTo(String)
     */
    public static int compareNullsLast(final @Nullable String a, final @Nullable String b) {
        if(Objects.equals(a, b)) {
            return 0;
        }
        // at this point not both can be null, so which ever is null wins
        if(a==null) {
            return 1;
        }
        if(b==null) {
            return -1;
        }
        // at this point neither can be null
        return a.compareTo(b);
    }

    // -- BASIC PREDICATES

    /**
     * Same as {@link #isNullOrEmpty(CharSequence)}
     * @param x
     * @return true only if string is of zero length or null.
     */
    public static boolean isEmpty(@Nullable final CharSequence x){
        return x==null || x.length()==0;
    }
    /**
     * Same as {@link #isEmpty(CharSequence)}
     * @param x
     * @return true only if string is of zero length or null.
     */
    public static boolean isNullOrEmpty(@Nullable final CharSequence x){
        return x==null || x.length()==0;
    }


    /**
     *
     * @param x
     * @return inverse of isEmpty(CharSequence).
     */
    public static boolean isNotEmpty(@Nullable final CharSequence x){
        return x!=null && x.length()!=0;
    }

    // -- OPTIONAL

    /**
     * @param x - input string
     * @return optionally the input string based on whether the input is not empty
     */
    public static Optional<String> nonEmpty(@Nullable final CharSequence x) {
        return isEmpty(x) ? Optional.empty() : Optional.of(x.toString());
    }

    // -- BASIC UNARY OPERATORS

    /**
     * @param input
     * @return null if the {@code input} is null or empty, the {@code input} otherwise
     */
    public static @Nullable String emptyToNull(@Nullable final String input) {
        if(isEmpty(input)) {
            return null;
        }
        return input;
    }

    /**
     * @param input
     * @return the empty string if the {@code input} is null, the {@code input} otherwise
     */
    public static String nullToEmpty(@Nullable final String input) {
        if(input==null) {
            return "";
        }
        return input;
    }


    /**
     * Trims the input.
     * @param input
     * @return null if the {@code input} is null
     */
    public static String trim(@Nullable final String input) {
        return mapIfPresentElse(input, String::trim, null);
    }

    /**
     * Converts all of the characters in {@code input} to lower case using the rules of the default locale.
     * @param input
     * @return null if {@code input} is null
     */
    public static String lower(@Nullable final String input) {
        return mapIfPresentElse(input, String::toLowerCase, null);
    }

    /**
     * Converts all of the characters in {@code input} to upper case using the rules of the default locale.
     * @param input
     * @return null if {@code input} is null
     */
    public static String upper(@Nullable final String input) {
        return mapIfPresentElse(input, String::toUpperCase, null);
    }

    /**
     * Converts the first character in {@code input} to upper case using the rules of the default locale.
     * @param input
     * @return null if {@code input} is null
     */
    public static String capitalize(@Nullable final String input) {
        if(input==null) {
            return null;
        }
        if (input.length() == 0) {
            return input;
        }
        if (input.length() == 1) {
            return input.toUpperCase();
        }
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Converts the first character in {@code input} to lower case using the rules of the default locale.
     * @param input
     * @return null if {@code input} is null
     */
    public static String decapitalize(@Nullable final String input) {
        if(input==null) {
            return null;
        }
        if (input.length() == 0) {
            return input;
        }
        if (input.length() == 1) {
            return input.toLowerCase();
        }
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    // -- SPECIAL UNARY OPERATORS

    public static String htmlEscape(final String source) {
        return _Strings_HtmlEscaper.htmlEscape(source);
    }

    // -- PREFIX/SUFFIX

    /**
     * If {@code input} does not start with {@code prefix} prepends {@code prefix} to the input.
     * @param input
     * @param prefix
     * @return null if {@code input} is null
     */
    public static String prefix(@Nullable final String input, final @NonNull String prefix) {
        if(input==null) {
            return null;
        }
        if(input.startsWith(prefix)) {
            return input;
        }
        return prefix + input;
    }

    /**
     * If {@code input} does not end with {@code suffix} appends {@code suffix} to the input.
     * @param input
     * @param suffix
     * @return null if {@code input} is null
     */
    public static String suffix(@Nullable final String input, final @NonNull String suffix) {
        if(input==null) {
            return null;
        }
        if(input.endsWith(suffix)) {
            return input;
        }
        return input + suffix;
    }

    // -- REDUCTION (BINARY OPERATIOR)

    /**
     * Combines 2 strings {@code left} and {@code right} into a single string, such that left
     * and right are delimited by the {@code delimiter} and such that
     * the result does not introduce a sequence of delimiters, like for example when building file-system
     * paths from chunks.
     *
     * @param left
     * @param right
     * @param delimiter
     * @return non-null
     */
    public static String combineWithDelimiter(
            @Nullable final String left, @Nullable final String right, final String delimiter) {

        requiresNotEmpty(delimiter, "pathDelimiter");

        if (isNullOrEmpty(left) && isNullOrEmpty(right)) {
            return "";
        }
        if (isNullOrEmpty(left)) {
            return right;
        }
        if (isNullOrEmpty(right)) {
            return left;
        }
        if (left.endsWith(delimiter) || right.startsWith(delimiter)) {
            return left + right;
        }
        return left + delimiter + right;
    }


    // -- PADDING

    /**
     * Returns a string, of length at least minLength, consisting of string prepended with as many copies
     * of padChar as are necessary to reach that length.
     * @param str
     * @param minLength
     * @param c
     */
    public static String padStart(@Nullable final String str, final int minLength, final char c) {
        if(minLength<=0) {
            return str;
        }
        final int len = size(str);
        if(len>=minLength) {
            return str;
        }

        final int fillCount = minLength - len;

        return of(fillCount, c) + nullToEmpty(str);
    }

    /**
     * Returns a string, of length at least minLength, consisting of string appended with as many copies
     * of padChar as are necessary to reach that length.
     * @param str
     * @param minLength
     * @param c
     */
    public static String padEnd(@Nullable final String str, final int minLength, final char c) {
        if(minLength<=0) {
            return str;
        }
        final int len = size(str);
        if(len>=minLength) {
            return str;
        }

        final int fillCount = minLength - len;

        return nullToEmpty(str) + of(fillCount, c);
    }

    // -- SPLITTING

    /**
     * Splits the {@code input} into chunks separated by {@code separator},
     * then puts all chunks on the returned stream.
     * <p>
     * Corner cases:
     * <ul>
     * <li>{@code input} starts with {@code separator}: an empty string is the first chunk put on the stream</li>
     * <li>{@code input} ends with {@code separator}: an empty string is the last chunk put on the stream</li>
     * <li>a {@code separator} is followed by another: an empty string is put on the stream</li>
     * </ul>
     * @param input
     * @param separator non-empty string
     * @return empty stream if {@code input} is null
     * @throws IllegalArgumentException if {@code separator} is empty
     */
    public static Stream<String> splitThenStream(@Nullable final String input, final String separator) {
        if(isEmpty(separator)) {
            throw new IllegalArgumentException("a non empty separator is required");
        }
        if(isEmpty(input)) {
            return Stream.of();
        }
        if(!input.contains(separator)) {
            return Stream.of(input);
        }

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(splitIterator(input, separator), Spliterator.ORDERED),
                false); // not parallel
    }

    /**
     * Creates a stream from the given input sequence around matches of {@code delimiterPattern}.
     * @param input
     * @param delimiterPattern
     */
    public static Stream<String> splitThenStream(@Nullable final CharSequence input, final @NonNull Pattern delimiterPattern) {
        if(isEmpty(input)) {
            return Stream.of();
        }
        return delimiterPattern.splitAsStream(input);
    }

    /**
     * Optionally applies {@code onNonEmptySplit} function, based on whether both split parts
     * <i>lhs</i> and <i>rhs</i> are non empty Strings.
     * @param <T>
     * @param input
     * @param separator
     * @param onNonEmptySplit
     */
    public static <T> Optional<T> splitThenApplyRequireNonEmpty(
            @Nullable final String input,
            final String separator,
            final BiFunction<String, String, T> onNonEmptySplit) {

        if(isEmpty(input)) {
            return Optional.empty();
        }
        // we have a non-empty string
        final int p = input.indexOf(separator);
        if(p<1){
            // separator not found or
            // empty lhs in string
            return Optional.empty();
        }
        final int q = p + separator.length();
        if(q==input.length()) {
            // empty rhs
            return Optional.empty();
        }
        return Optional.ofNullable(onNonEmptySplit.apply(input.substring(0, p), input.substring(q)));
    }

    public static void splitThenAccept(
            @Nullable final String input,
            final String separator,
            final BiConsumer<String, String> onNonEmptySplit,
            final Consumer<String> onNonEmptyLhs,
            final Consumer<String> onNonEmptyRhs) {

        _Strings_FastSplit.splitThenAccept(input, separator, onNonEmptySplit, onNonEmptyLhs, onNonEmptyRhs);
    }

    public static void splitThenAcceptEmptyAsNull(
            @Nullable final String input,
            final String separator,
            final BiConsumer<String, String> onSplit) {

        _Strings_FastSplit.splitThenAccept(input, separator, onSplit,
                lhs->onSplit.accept(lhs, null),
                rhs->onSplit.accept(null, rhs));
    }


    public static Stream<String> grep(@Nullable final String input, @Nullable Predicate<String> matcher){
        matcher = matcher!=null ? matcher : _Predicates.alwaysTrue();
        return splitThenStream(input, "\n")
                .filter(_Strings::isNotEmpty)
                .filter(matcher)
                .map(s->s.replace("\r", ""));
    }

    public static Stream<String> grep(@Nullable final String input, @Nullable final String contains){
        final Predicate<String> matcher = contains!=null ? line->line.contains(contains) : _Predicates.alwaysTrue();
        return grep(input, matcher);
    }


    // -- REPLACEMENT OPERATORS

    /**
     * Condenses any whitespace to the given {@code replacement}
     *
     * @param input
     * @param replacement
     * @return null if {@code input} is null
     */
    public static String condenseWhitespaces(@Nullable final String input, final @NonNull String replacement) {
        return mapIfPresentElse(input, __->input.replaceAll("\\s+", replacement), null);
    }

    /**
     * ...xyz
     * @param input
     * @param maxLength
     * @param ellipsis
     * @return (non-null), ellipsified version of {@code input}, if {@code input} exceeds length {@code maxLength}
     */
    public static String ellipsifyAtStart(
            @Nullable final CharSequence input,
            final int maxLength,
            final @NonNull CharSequence ellipsis) {

        if(input==null) {
            return "";
        }
        if(input.length()<=maxLength) {
            return input.toString();
        }
        final int trimmedLength = maxLength - ellipsis.length();
        final int end = input.length();
        final int start = end - trimmedLength;
        return String.join("", ellipsis, input.subSequence(start, end));
    }

    /**
     * abc...
     * @param input
     * @param maxLength
     * @param ellipsis
     * @return (non-null), ellipsified version of {@code input}, if {@code input} exceeds length {@code maxLength}
     */
    public static String ellipsifyAtEnd(
            @Nullable final CharSequence input,
            final int maxLength,
            final @NonNull CharSequence ellipsis) {

        if(input==null) {
            return "";
        }
        if(input.length()<=maxLength) {
            return input.toString();
        }
        final int trimmedLength = maxLength - ellipsis.length();
        return String.join("", input.subSequence(0, trimmedLength), ellipsis);
    }

    // -- READ FROM INPUT STREAM

    public static String read(@Nullable final InputStream input, final @NonNull Charset charset) {
        if(input==null) {
            return "";
        }
        // see https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
        try(Scanner scanner = new Scanner(input, charset.name())){
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    @SneakyThrows
    public static String readFromResource(
            final @NonNull Class<?> resourceLocation,
            final @NonNull String resourceName,
            final @NonNull Charset charset) {
        try(lombok.val input = resourceLocation.getResourceAsStream(resourceName)){
            return read(input, charset);
        }
    }

    // -- BYTE ARRAY CONVERSION

    /**
     * Encodes {@code str} into a sequence of bytes using the given {@code charset}.
     * @param str
     * @param charset
     * @return null if {@code str} is null
     */
    public static final byte[] toBytes(@Nullable final String str, final @NonNull Charset charset) {
        return mapIfPresentElse(str, __->str.getBytes(charset), null);
    }

    /**
     * Constructs a new String by decoding the specified array of bytes using the specified {@code charset}.
     * @param bytes
     * @param charset
     * @return null if {@code bytes} is null
     */
    public static final String ofBytes(@Nullable final byte[] bytes, final @NonNull Charset charset) {
        return mapIfPresentElse(bytes, __->new String(bytes, charset), null);
    }

    /**
     * Converts the {@code input} to a byte array using the specified {@code charset},
     * then applies the byte manipulation operator {@code converter},
     * then converts the (manipulated) byte array back to a string, again using the specified {@code charset}.
     * @param input
     * @param converter
     * @param charset
     * @return null if {@code input} is null
     */
    public static final String convert(@Nullable final String input, final @NonNull BytesOperator converter, final @NonNull Charset charset) {
        return mapIfPresentElse(input, __->ofBytes(converter.apply(toBytes(input, charset)), charset), null);
    }

    // -- UNARY OPERATOR COMPOSITION

    /**
     * Monadic StringOperator that allows composition of unary string operators.
     */
    public static final class StringOperator {

        private final UnaryOperator<String> operator;

        private StringOperator(final @NonNull UnaryOperator<String> operator) {
            this.operator = operator;
        }

        public String apply(final String input) {
            return operator.apply(input);
        }

        public StringOperator andThen(final UnaryOperator<String> andThen) {
            return new StringOperator(s->andThen.apply(operator.apply(s)));
        }

    }

    /**
     * Returns a StringOperator that allows composition of unary string operators
     */
    public static StringOperator operator() {
        return new StringOperator(UnaryOperator.identity());
    }

    // -- SPECIAL COMPOSITES

    // using naming convention asXxx...

    public static final StringOperator asLowerDashed = operator()
            .andThen(_Strings::lower)
            .andThen(s->_Strings.condenseWhitespaces(s, "-"));

    public static final StringOperator asNormalized = operator()
            .andThen(s->_Strings.condenseWhitespaces(s, " "));

    public static final StringOperator asNaturalName2 = operator()
            .andThen(s->_Strings_NaturalNames.naturalName2(s, true));


    public static final String asFileNameWithExtension(final @NonNull String fileName, final @NonNull String fileExtension) {
        return suffix(fileName, prefix(fileExtension, "."));
    }

    // -- SHORTCUTS

    /**
     * Like {@link _Strings#splitThenStream(String, String)} but also trimming each junk, then discarding
     * empty chunks.
     * @return empty stream if {@code input} is null
     */
    public static Stream<String> splitThenStreamTrimmed(@Nullable final String input, final String separator) {
        return splitThenStream(input, separator)
                .map(String::trim)
                .filter(not(String::isEmpty));
    }

    /**
     * Like {@link _Strings#splitThenStream(CharSequence, Pattern)} but also trimming each junk,
     * then discarding empty chunks.
     * @return empty stream if {@code input} is null
     */
    public static Stream<String> splitThenStreamTrimmed(@Nullable final CharSequence input, final Pattern delimiterPattern) {
        return splitThenStream(input, delimiterPattern)
                .map(String::trim)
                .filter(not(String::isEmpty));
    }

    public static String base64UrlDecode(final @Nullable String str) {
        return _Strings.convert(str, _Bytes.ofUrlBase64, StandardCharsets.UTF_8);
    }

    public static String base64UrlEncode(final @Nullable String str) {
        return _Strings.convert(str, _Bytes.asUrlBase64, StandardCharsets.UTF_8);
    }


}
