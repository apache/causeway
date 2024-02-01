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
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.LoaderOptions;

import org.apache.causeway.commons.functional.Try;

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

    @FunctionalInterface
    public interface YamlDumpCustomizer extends UnaryOperator<DumperOptions> {}
    @FunctionalInterface
    public interface YamlLoadCustomizer extends UnaryOperator<LoaderOptions> {}

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
            return Try.call(()->createJacksonReader(Optional.empty(), customizers)
                    .readValue(is, mappedType));
        });
    }

    /**
     * Tries to deserialize YAML content from given UTF8 encoded {@link String}
     * into an instance of given {@code mappedType}.
     */
    public <T> Try<T> tryReadCustomized(
            final @NonNull Class<T> mappedType,
            final @Nullable String stringUtf8,
            final @NonNull YamlLoadCustomizer loadCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return tryReadCustomized(mappedType, DataSource.ofStringUtf8(stringUtf8), loadCustomizer, customizers);
    }

    /**
     * Tries to deserialize YAML content from given {@link DataSource} into an instance of
     * given {@code requiredType}.
     */
    public <T> Try<T> tryReadCustomized(
            final @NonNull Class<T> mappedType,
            final @NonNull DataSource source,
            final @NonNull YamlLoadCustomizer loadCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            return Try.call(()->createJacksonReader(Optional.of(loadCustomizer), customizers)
                    .readValue(is, mappedType));
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
            Try.run(()->createJacksonWriter(Optional.empty(), customizers).writeValue(os, pojo)));
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
                ? createJacksonWriter(Optional.empty(), customizers).writeValueAsString(pojo)
                : null;
    }

    /**
     * Writes given {@code pojo} to given {@link DataSink}.
     */
    public void writeCustomized(
            final @Nullable Object pojo,
            final @NonNull DataSink sink,
            final @NonNull YamlDumpCustomizer dumpCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        if(pojo==null) return;
        sink.writeAll(os->
            Try.run(()->createJacksonWriter(Optional.of(dumpCustomizer), customizers).writeValue(os, pojo)));
    }

    /**
     * Converts given {@code pojo} to an UTF8 encoded {@link String}.
     * @return <code>null</code> if pojo is <code>null</code>
     */
    @SneakyThrows
    @Nullable
    public static String toStringUtf8Customized(
            final @Nullable Object pojo,
            final @NonNull YamlDumpCustomizer dumpCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        return pojo!=null
                ? createJacksonWriter(Optional.of(dumpCustomizer), customizers).writeValueAsString(pojo)
                : null;
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
     * @param loadCustomizer
     */
    private ObjectMapper createJacksonReader(
            final Optional<YamlLoadCustomizer> loadCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        var yamlFactory = YAMLFactory.builder()
                .loaderOptions(loadCustomizer
                        .map(YamlUtils::createLoaderOptions)
                        .orElseGet(YamlUtils::createLoaderOptions))
                .build();
        var mapper = new ObjectMapper(yamlFactory);
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
            final Optional<YamlDumpCustomizer> dumpCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        var yamlFactory = YAMLFactory.builder()
                .dumperOptions(dumpCustomizer
                        .map(YamlUtils::createDumperOptions)
                        .orElseGet(YamlUtils::createDumperOptions))
                .build()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        var mapper = new ObjectMapper(yamlFactory);
        mapper = JsonUtils.writingJavaTimeSupport(mapper);
        mapper = JsonUtils.writingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

    private DumperOptions createDumperOptions(final YamlDumpCustomizer ... dumpCustomizers) {
        var dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setLineBreak(LineBreak.UNIX); // fixated for consistency
        //options.setPrettyFlow(true);
        //options.setDefaultFlowStyle(FlowStyle.BLOCK);
        for(YamlUtils.YamlDumpCustomizer customizer : dumpCustomizers) {
            dumperOptions = Optional.ofNullable(customizer.apply(dumperOptions))
                    .orElse(dumperOptions);
        }
        return dumperOptions;
    }

    private LoaderOptions createLoaderOptions(final YamlLoadCustomizer ... loadCustomizers) {
        var loaderOptions = new LoaderOptions();
        for(YamlUtils.YamlLoadCustomizer customizer : loadCustomizers) {
            loaderOptions = Optional.ofNullable(customizer.apply(loaderOptions))
                    .orElse(loaderOptions);
        }
        return loaderOptions;
    }

}
