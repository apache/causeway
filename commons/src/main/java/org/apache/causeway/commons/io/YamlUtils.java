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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.commons.functional.Try;
import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.DumperOptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities to convert from and to YAML format.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class YamlUtils {

    public enum Marshalling {
        YAML_LIST,
        MULTI_DOC
    }

    // -- READING

    /**
     * Tries to deserialize YAML content from given UTF8 encoded {@link String}
     * into an instance of given {@code mappedType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @Nullable String stringUtf8,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return tryRead(mappedType, DataSource.ofStringUtf8(stringUtf8), customizers);
    }

    /**
     * Tries to deserialize YAML content from given {@link DataSource} into an instance of
     * given {@code requiredType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @NonNull DataSource source,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            return Try.call(()->createJacksonReader(customizers)
                    .readValue(is, mappedType));
        });
    }
    
    /**
     * Tries to deserialize YAML content from given {@link DataSource} into a {@link List}
     * with given {@code elementType}.
     */
    public <T> Try<List<T>> tryReadAsList(
            final @NonNull Class<T> elementType,
            final @NonNull DataSource source,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is) -> Try.call(()->{
            var mapper = createJacksonReader(customizers);
            var collectionType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
            return mapper.readValue(is, collectionType);
        }));
    }

    // -- WRITING

    /**
     * Writes given {@code pojo} to given {@link DataSink}.
     */
    public void write(
            final @Nullable Object pojo,
            final @NonNull DataSink sink,
            final JsonUtils.JacksonCustomizer ... customizers) {
        if(pojo==null) return;
        sink.writeAll(os->
            Try.run(()->createJacksonWriter(customizers).writeValue(os, pojo)));
    }

    /**
     * Converts given {@code pojo} to an UTF8 encoded {@link String}.
     * @return <code>null</code> if pojo is <code>null</code>
     */
    @SneakyThrows
    @Nullable
    public static String toStringUtf8(
            final @Nullable Object pojo,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return pojo!=null
                ? createJacksonWriter(customizers).writeValueAsString(pojo)
                : null;
    }

    /**
     * Converts given list to UTF8 encoded YAML using the requested marshalling mode.
     * @return <code>null</code> if list is <code>null</code>
     */
    @SneakyThrows
    @Nullable
    public static String toStringUtf8ForList(
            final @Nullable List<?> list,
            final @NonNull Marshalling marshalling,
            final JsonUtils.JacksonCustomizer ... customizers) {
        if (list == null) {
            return null;
        }
        if (marshalling == Marshalling.YAML_LIST) {
            return toStringUtf8(list, customizers);
        }
        return toStringUtf8AsMultiDocument(list, customizers);
    }

    @SneakyThrows
    private static String toStringUtf8AsMultiDocument(
            final List<?> list,
            final JsonUtils.JacksonCustomizer ... customizers) {
        if (list.isEmpty()) {
            return "";
        }
        final var mapper = createJacksonWriter(customizers);
        final List<String> serializedDocuments = new ArrayList<>();
        for (Object element : list) {
            serializedDocuments.add(mapper.writeValueAsString(element));
        }
        return String.join("---\n", serializedDocuments);
    }
    // -- CUSTOMIZERS

    /**
     * Include read-only JavaBean properties (the ones without setters) in the YAML document.
     * <p>
     * By default these properties are not included to be able to parse later the same JavaBean. */
    public DumperOptions allowReadOnlyProperties(final DumperOptions opts) {
        opts.setAllowReadOnlyProperties(true);
        return opts;
    }

    // -- MAPPER FACTORIES

    /**
     * SnakeYaml as of 2.2 does not support Java records. So we use Jackson instead.
     */
    private ObjectMapper createJacksonReader(
            final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper = JsonUtils.jdk8Support(mapper);
        mapper = JsonUtils.readingJavaTimeSupport(mapper);
        mapper = JsonUtils.readingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

    /**
     * Use Jackson to write YAML.
     */
    private ObjectMapper createJacksonWriter(
            final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper = JsonUtils.jdk8Support(mapper);
        mapper = JsonUtils.writingJavaTimeSupport(mapper);
        mapper = JsonUtils.writingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

}
