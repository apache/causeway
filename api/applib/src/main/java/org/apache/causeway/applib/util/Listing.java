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
package org.apache.causeway.applib.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.io.TextUtils;

/**
 * Represents a list of lines (of text),
 * where each {@link Line} has semantics, such as being able to be commented out.
 *
 * <p> Each non-comment line can be mapped to a Java class of type {@code T}.
 *
 * <p> Typically not used as domain value, as we provide no {@link ValueSemantics},
 * but rather as a (multi-line) {@link String} support class.
 *
 * @param <T> type each non-comment line can be mapped to (successful parsing)
 * @since 4.0
 */
@Programmatic
public record Listing<T>(
        @NonNull LineAdapter<T> lineAdapter,
        @NonNull Can<Line> lines) {

	public interface LineAdapter<T> {
		Class<T> objectType();
		String stringify(T t);
        T destringify(String literal);
        Object extractKey(T t);

        // -- LISTING FACTORIES

		default Listing<T> emptyListing() {
            return createListing(Can.empty());
        }

		default Listing<T> createListing(@Nullable final Iterable<T> enabledElements) {
            return createListing(_NullSafe.stream(enabledElements));
        }
		default Listing<T> createListing(@Nullable final Stream<T> enabledElementStream) {
            if(enabledElementStream==null)
            	return emptyListing();
            return new Listing<>(
            		this,
            		enabledElementStream
	                    .map(t->new LineEnabled<>(t, stringify(t)))
	                    .collect(Can.toCan()));
        }

		default Listing<T> parseListing(@Nullable final String wholeText) {
            if(wholeText==null) return emptyListing();
            return parseListing(TextUtils.readLines(wholeText));
        }
		default Listing<T> parseListing(@Nullable final Can<String> textLines) {
            if(textLines==null) return emptyListing();
            return new Listing<>(this, textLines.map(this::parseLine));
        }

        // -- PARSING

        /**
         * Parses the whole line as is, including comments.
         *
         * <p>If a line is not a comment, but can also not be mapped to {@code T},
         * then it is commented out.
         */
		default Line parseLine(final String wholeLine) {
            final var line = wholeLine.trim();
            if(line.isBlank())
                return new LineComment(line);
            if(line.startsWith("#")) {
                var trimmed = line;
                while(trimmed.startsWith("#")) {
                    trimmed = trimmed.substring(1).trim();
                }
                T object = asT(trimmed).getValue().orElse(null);
                return object==null
                        ? new LineComment(wholeLine)
                        : (line.startsWith("REMOVED "))
                                ? new LineRemoved<>(object, trimmed.substring(8))
                                : new LineDisabled<>(object, trimmed);
            }
            Try<T> asT = asT(line);
            return asT
                    .mapSuccessWhenPresent(object->(Line)new LineEnabled<T>(object, line))
                    .mapEmptyToFailure()
                    .mapFailureToSuccess(e->new LineComment(String.format("#ERROR cannot parse ‹%s› as %s (%s)",
                            line, objectType().getSimpleName(), e)))
                    .valueAsNonNullElseFail();
        }

        // -- HELPER

        private Try<T> asT(final String stringified) {
            return Try.call(()->destringify(stringified));
        }

	}

    // -- ADAPTERS

	public static <T> LineAdapter<T> lineAdapter(
            @NonNull final Class<T> objectType,
            @NonNull final Function<T, String> stringifier,
            @NonNull final Function<String, T> destringifier,
            @NonNull final Function<T, ?> keyExtractor) {
		return new SimpleLineAdapter<>(objectType, stringifier, destringifier, keyExtractor);
	}

    private record SimpleLineAdapter<T> (
            @NonNull Class<T> objectType,
            @NonNull Function<T, String> stringifier,
            @NonNull Function<String, T> destringifier,
            @NonNull Function<T, ?> keyExtractor) implements LineAdapter<T> {
		@Override public String stringify(final T t) {
			return stringifier.apply(t);
		}
	    @Override public T destringify(final String literal) {
			return destringifier.apply(literal);
		}
	    @Override public Object extractKey(final T t) {
			return keyExtractor.apply(t);
		}
    }

    public sealed interface Line
    permits MappedLine, LineComment{
        String format();
    }
    public sealed interface MappedLine<T>
    extends Line
    permits LineEnabled, LineDisabled, LineRemoved {
        T object();
    }

    /**
     * Can be mapped to {@code T}. Not commented out.
     */
    public record LineEnabled<T>(
            @NonNull T object,
            @NonNull String objectStringified)
            implements MappedLine<T> {
        @Override public String format() {
            return objectStringified;
        }
        public LineDisabled<T> toDisabled() {
            return new LineDisabled<>(object, objectStringified);
        }
    }
    /**
     * Can be mapped to {@code T}, but commented out.
     */
    public record LineDisabled<T>(
            @NonNull T object,
            @NonNull String objectStringified)
            implements MappedLine<T> {
        @Override public String format() {
            return "#" + objectStringified;
        }
        public LineEnabled<T> toEnabled() {
            return new LineEnabled<>(object, objectStringified);
        }
        public LineRemoved<T> toRemoved() {
            return new LineRemoved<>(object, objectStringified);
        }
    }
    /**
     * Can be mapped to {@code T}, but was removed, hence commented out.
     */
    public record LineRemoved<T>(
            @NonNull T object,
            @NonNull String objectStringified)
            implements MappedLine<T> {
        @Override public String format() {
            return "#REMOVED " + objectStringified;
        }
        public LineDisabled<T> toDisabled() {
            return new LineDisabled<>(object, objectStringified);
        }
    }
    /**
     * Blank line or arbitrary comment, cannot be mapped to {@code T}.
     */
    public record LineComment(
            @NonNull String comment)
            implements Line {
        @Override public String format() {
            return comment;
        }
    }

    // -- STREAMS

    public Stream<LineEnabled<T>> streamEnabledLines() {
        return lines().stream().filter(LineEnabled.class::isInstance).map(LineEnabled.class::cast);
    }
    public Stream<LineDisabled<T>> streamDisabledLines() {
        return lines().stream().filter(LineDisabled.class::isInstance).map(LineDisabled.class::cast);
    }
    public Stream<T> streamEnabled() {
        return streamEnabledLines().map(LineEnabled::object);
    }
    public Stream<T> streamDisabled() {
        return streamDisabledLines().map(LineDisabled::object);
    }
    public Stream<LineComment> streamComments() {
        return lines().stream().filter(LineComment.class::isInstance).map(LineComment.class::cast);
    }

    // -- FORMAT

    @Override
    public final String toString() {
        return lines().map(Line::format).join("\n");
    }

    // -- MERGE

    public enum MergePolicy {
        ADD_NEW_AS_ENABLED,
        ADD_NEW_AS_DISABLED
    }

    /**
     * Say this listing was edited by a human,
     * but needs to be synchronized with a newer version originating from some system process,
     * then we'd like to merge in this new information,
     * without loosing any information that is already present in this listing such as:
     * <ul>
     * <li>comments</li>
     * <li>line ordering</li>
     * </ul>
     *
     * <p> We do this by adding a comment line {@code mergeHeader} followed by any lines that are new.
     *
     * <p> Any lines already existing are kept as they are,
     * unless the {@code newerVersion} no longer contains the referenced object of type {@code T},
     * in which case, the {@link Line} will be commented out (if not already) with a marker
     * {@code #REMOVED}.
     *
     * <p> This requires the merge algorithm to evaluate whether 2 referenced objects are equal,
     * which it does by checking object keys as given by {@link ListingHandler#keyExtractor}
     * for equality. We could have done the same by directly checking referenced objects for equality,
     * but - worst case - that would involve entire objects graphs to be assembled,
     * while for our use case its convenient to work with simple object facades.
     *
     * @param mergeHeader comment line automatically added on top of merged lines; if null or empty, then is omitted
     */
    public <K> Listing<T> merge(@NonNull final MergePolicy policy,
    		@Nullable final Listing<T> newerVersion,
    		@Nullable final String mergeHeader) {
        if(newerVersion==null) return this;
        final Map<Object, LineEnabled<T>> incomingByKey = newerVersion.streamEnabledLines()
            .collect(Collectors.toMap(
                line->lineAdapter().extractKey(line.object()),
                UnaryOperator.identity(),
                (a, b)->a,
                LinkedHashMap::new));
        if(incomingByKey.isEmpty()) return this;

        var mergedLines = new ArrayList<Line>();

        // if in <incoming> but NOT in <self> -> add to merged (honor LineMergePolicy)
        // if NOT in <incoming> but in <self> -> mark #REMOVED if not already
        // if in both all AND b -> add to merged (keep enabled-state as defined by b)
        lines().stream()
        .forEach((final Line line)->{
            @SuppressWarnings("unchecked")
            var key = (line instanceof MappedLine mappedLine)
                    ? lineAdapter().extractKey((T) mappedLine.object())
                    : null;
            final boolean incomingContainsKey = key!=null
                    ? incomingByKey.remove(key)!=null
                    : false;

            if(line instanceof LineComment comment) {
                mergedLines.add(comment); // identity operation
            } else if(line instanceof LineEnabled<?> lineEnabled) {
                if(incomingContainsKey) {
                    mergedLines.add(lineEnabled); // identity operation
                } else {
                    mergedLines.add(lineEnabled.toDisabled().toRemoved()); // mark REMOVED
                }
                incomingByKey.remove(key);
            } else if(line instanceof LineDisabled<?> lineDisabled) {
                if(incomingContainsKey) {
                    mergedLines.add(lineDisabled); // identity operation
                } else {
                    mergedLines.add(lineDisabled.toRemoved()); // mark REMOVED
                }
            } else if(line instanceof LineRemoved<?> lineRemoved) {
                if(incomingContainsKey) {
                    mergedLines.add(lineRemoved); // identity operation
                } else {
                    mergedLines.add(lineRemoved.toDisabled()); // unmark REMOVED
                }
            }
        });

        // process the remaining incoming lines
        if(!incomingByKey.isEmpty()) {
            if(!mergedLines.isEmpty()
            		&& StringUtils.hasLength(mergeHeader)) {
                mergedLines.add(new LineComment(mergeHeader)); // skip if we are filling an empty listing
            }
            if(MergePolicy.ADD_NEW_AS_ENABLED==policy) {
                incomingByKey.values()
                    .forEach(mergedLines::add);
            } else {
                incomingByKey.values()
                    .forEach(lineEnabled->mergedLines.add(lineEnabled.toDisabled()));
            }
        }

        return new Listing<>(lineAdapter, Can.ofCollection(mergedLines));
    }

}
