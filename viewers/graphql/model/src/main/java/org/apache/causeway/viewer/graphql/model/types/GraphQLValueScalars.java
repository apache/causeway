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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.model.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.applib.value.Password;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GraphQLValueScalars {

    public static final GraphQLScalarType LOCAL_DATE_TIME = stringScalar(
            "LocalDateTime",
            "An ISO-8601 local date-time without an offset or zone; fractional seconds are preserved.",
            LocalDateTime.class,
            value -> LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            value -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value));

    public static final GraphQLScalarType OFFSET_DATE_TIME = stringScalar(
            "DateTime",
            "An ISO-8601 date-time with an explicit offset; fractional seconds are preserved.",
            OffsetDateTime.class,
            value -> OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            value -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value));

    public static final GraphQLScalarType OFFSET_TIME = stringScalar(
            "Time",
            "An ISO-8601 time with an explicit offset; fractional seconds are preserved.",
            OffsetTime.class,
            value -> OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME),
            value -> DateTimeFormatter.ISO_OFFSET_TIME.format(value));

    public static final GraphQLScalarType URL_VALUE = stringScalar(
            "Url",
            "An absolute URL represented by its normalized external form.",
            URL.class,
            GraphQLValueScalars::parseUrl,
            URL::toExternalForm);

    public static final GraphQLScalarType LEGACY_DATE_TIME = stringScalar(
            "LegacyDateTime",
            "An ISO-8601 UTC instant used for java.util.Date.",
            Date.class,
            value -> Date.from(Instant.parse(value)),
            value -> DateTimeFormatter.ISO_INSTANT.format(value.toInstant()));

    public static final GraphQLScalarType SQL_DATE = stringScalar(
            "SqlDate",
            "An ISO-8601 date-only value used for java.sql.Date.",
            java.sql.Date.class,
            value -> java.sql.Date.valueOf(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)),
            value -> DateTimeFormatter.ISO_LOCAL_DATE.format(value.toLocalDate()));

    public static final GraphQLScalarType SQL_TIMESTAMP = stringScalar(
            "SqlTimestamp",
            "An ISO-8601 local date-time used for java.sql.Timestamp; fractional seconds are preserved.",
            Timestamp.class,
            value -> Timestamp.valueOf(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            value -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value.toLocalDateTime()));

    public static final GraphQLScalarType SQL_TIME = stringScalar(
            "SqlTime",
            "An ISO-8601 time-only value at second precision used for java.sql.Time.",
            Time.class,
            GraphQLValueScalars::parseSqlTime,
            value -> DateTimeFormatter.ISO_LOCAL_TIME.format(value.toLocalTime()));

    public static final GraphQLScalarType LOCALE = stringScalar(
            "Locale",
            "An IETF BCP 47 language tag used for java.util.Locale.",
            Locale.class,
            GraphQLValueScalars::parseLocale,
            Locale::toLanguageTag);

    public static final GraphQLScalarType BOOKMARK = stringScalar(
            "Bookmark",
            "A Causeway bookmark in logical-type:identifier form.",
            Bookmark.class,
            Bookmark::parseElseFail,
            Bookmark::stringify);

    public static final GraphQLScalarType APPLICATION_FEATURE_ID = stringScalar(
            "ApplicationFeatureId",
            "A URL-safe encoded Causeway application feature identifier.",
            ApplicationFeatureId.class,
            ApplicationFeatureId::parseEncoded,
            ApplicationFeatureId::asEncodedString);

    public static final GraphQLScalarType PASSWORD = stringScalar(
            "Password",
            "A protected Causeway password value. Input constructs the value; output is always suppressed.",
            Password.class,
            Password::of,
            Password::toString);

    public static final GraphQLScalarType MARKUP = outputOnlyStringScalar(
            "Markup",
            "Causeway markup HTML. This scalar is output-only and clients must treat the content as markup.",
            Markup.class,
            Markup::html);

    public static final GraphQLScalarType UNSUPPORTED_VALUE = GraphQLScalarType.newScalar()
            .name("UnsupportedValue")
            .description("Unsupported Causeway value. Strict output is redacted and input requires an explicit reversible ScalarMarshaller or TypeMapper.")
            .coercing(new Coercing<Object, String>() {
                @Override
                public String serialize(
                        final Object dataFetcherResult,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    return "[unsupported]";
                }

                @Override
                public Object parseValue(
                        final Object input,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    throw new CoercingParseValueException(
                            "This Causeway value has no reversible GraphQL input strategy; register a ScalarMarshaller or TypeMapper");
                }

                @Override
                public Object parseLiteral(
                        final Value<?> input,
                        final CoercedVariables variables,
                        final GraphQLContext graphQLContext,
                        final Locale locale) {
                    throw new CoercingParseLiteralException(
                            "This Causeway value has no reversible GraphQL input strategy; register a ScalarMarshaller or TypeMapper");
                }
            })
            .build();

    /**
     * Compatibility alias used where the unsupported scalar is selected specifically for input.
     */
    public static final GraphQLScalarType UNSUPPORTED_INPUT = UNSUPPORTED_VALUE;

    private static URL parseUrl(final String value) {
        try {
            var url = new URL(value);
            if (url.getProtocol() == null || url.getHost() == null || url.getHost().isBlank()) {
                throw new IllegalArgumentException("URL must be absolute");
            }
            return url;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid URL", ex);
        }
    }

    private static Time parseSqlTime(final String value) {
        var localTime = java.time.LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
        if (localTime.getNano() != 0) {
            throw new IllegalArgumentException("SqlTime supports second precision");
        }
        return Time.valueOf(localTime);
    }

    private static Locale parseLocale(final String value) {
        return new Locale.Builder().setLanguageTag(value).build();
    }

    private static <T> GraphQLScalarType stringScalar(
            final String name,
            final String description,
            final Class<T> javaType,
            final Function<String, T> parser,
            final Function<T, String> formatter) {
        return GraphQLScalarType.newScalar()
                .name(name)
                .description(description)
                .coercing(new Coercing<T, String>() {
                    @Override
                    public String serialize(
                            final Object dataFetcherResult,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        if (!javaType.isInstance(dataFetcherResult)) {
                            throw new CoercingSerializeException("Expected a " + name + " compatible Java value");
                        }
                        try {
                            return formatter.apply(javaType.cast(dataFetcherResult));
                        } catch (RuntimeException ex) {
                            throw new CoercingSerializeException("Unable to serialize " + name, ex);
                        }
                    }

                    @Override
                    public T parseValue(
                            final Object input,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        if (!(input instanceof String text)) {
                            throw new CoercingParseValueException("Expected " + name + " input as a string");
                        }
                        return parseString(text, parser, name, CoercingParseValueException::new);
                    }

                    @Override
                    public T parseLiteral(
                            final Value<?> input,
                            final CoercedVariables variables,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        if (!(input instanceof StringValue stringValue)) {
                            throw new CoercingParseLiteralException("Expected " + name + " input as a string literal");
                        }
                        return parseString(stringValue.getValue(), parser, name, CoercingParseLiteralException::new);
                    }
                })
                .build();
    }

    private static <T> GraphQLScalarType outputOnlyStringScalar(
            final String name,
            final String description,
            final Class<T> javaType,
            final Function<T, String> formatter) {
        return GraphQLScalarType.newScalar()
                .name(name)
                .description(description)
                .coercing(new Coercing<T, String>() {
                    @Override
                    public String serialize(
                            final Object dataFetcherResult,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        if (!javaType.isInstance(dataFetcherResult)) {
                            throw new CoercingSerializeException("Expected a " + name + " compatible Java value");
                        }
                        return formatter.apply(javaType.cast(dataFetcherResult));
                    }

                    @Override
                    public T parseValue(
                            final Object input,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        throw new CoercingParseValueException(name + " is output-only");
                    }

                    @Override
                    public T parseLiteral(
                            final Value<?> input,
                            final CoercedVariables variables,
                            final GraphQLContext graphQLContext,
                            final Locale locale) {
                        throw new CoercingParseLiteralException(name + " is output-only");
                    }
                })
                .build();
    }

    private static <T, E extends RuntimeException> T parseString(
            final String value,
            final Function<String, T> parser,
            final String scalarName,
            final Function<String, E> exceptionFactory) {
        try {
            return parser.apply(value);
        } catch (RuntimeException ex) {
            throw exceptionFactory.apply("Invalid " + scalarName + " value");
        }
    }
}
