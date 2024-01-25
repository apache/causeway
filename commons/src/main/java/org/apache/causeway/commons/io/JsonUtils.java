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
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.context._Context;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * Utilities to convert from and to JSON format.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
@Log4j2
public class JsonUtils {

    /**
     * Consumers of the framework may choose to use a different provider.
     */
    public Optional<Class<?>> getPlatformDefaultJsonProviderForJaxb() {
        return Try.call(()->_Context.loadClass("org.eclipse.persistence.jaxb.rs.MOXyJsonProvider"))
                .ifFailure(cause->
                      log.warn("This implementation of RestfulClient does require the class 'MOXyJsonProvider'"
                          + " on the class-path."
                          + " Are you missing a maven dependency?")
                )
                .getValue()
                .map(x->x);
    }

    @FunctionalInterface
    public interface JacksonCustomizer extends UnaryOperator<ObjectMapper> {}

    // -- READING

    /**
     * Tries to deserialize JSON content from given UTF8 encoded {@link String}
     * into an instance of given {@code mappedType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @Nullable String stringUtf8,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return tryRead(mappedType, DataSource.ofStringUtf8(stringUtf8), customizers);
    }

    /**
     * Tries to deserialize JSON content from given {@link DataSource} into an instance of
     * given {@code mappedType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @NonNull DataSource source,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            return Try.call(()->createMapper(customizers).readValue(is, mappedType));
        });
    }

    /**
     * Tries to deserialize JSON content from given {@link DataSource} into a {@link List}
     * with given {@code elementType}.
     */
    public <T> Try<List<T>> tryReadAsList(
            final @NonNull Class<T> elementType,
            final @NonNull DataSource source,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            return Try.call(()->{
                val mapper = createMapper(customizers);
                val listFactory = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
                return mapper.readValue(is, listFactory);
            });
        });
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
            Try.run(()->createMapper(customizers).writeValue(os, pojo)));
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
                ? createMapper(customizers).writeValueAsString(pojo)
                : null;
    }

    // -- CUSTOMIZERS

    /** enable indentation for the underlying generator */
    public ObjectMapper indentedOutput(final ObjectMapper mapper) {
        return mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** only properties with non-null values are to be included */
    public ObjectMapper onlyIncludeNonNull(final ObjectMapper mapper) {
        return mapper.setSerializationInclusion(Include.NON_NULL);
    }

    /** add support for JAXB annotations */
    public ObjectMapper jaxbAnnotationSupport(final ObjectMapper mapper) {
        return mapper.registerModule(new JakartaXmlBindAnnotationModule());
    }

    // -- MAPPER FACTORY

    private ObjectMapper createMapper(
            final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper();
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

}
