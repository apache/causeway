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
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.DumpSettingsBuilder;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder;
import org.yaml.snakeyaml.DumperOptions;

import org.apache.causeway.commons.functional.Try;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import tools.jackson.dataformat.yaml.YAMLFactory;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;

/**
 * Utilities to convert from and to YAML format.
 *
 * @since 2.0 refined for 4.0 {@index}
 */
@UtilityClass
public class YamlUtils {

    @FunctionalInterface
    public interface YamlDumpCustomizer extends Consumer<DumpSettingsBuilder> {}
    @FunctionalInterface
    public interface YamlLoadCustomizer extends Consumer<LoadSettingsBuilder> {}

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
    private YAMLMapper createJacksonReader(
            final Optional<YamlLoadCustomizer> loadCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        var yamlFactory = YAMLFactory.builder()
                .loadSettings(loadCustomizer
                        .map(YamlUtils::createLoadSettings)
                        .orElseGet(YamlUtils::createLoadSettings))
                .build();
        
        var builder = YAMLMapper.builder(yamlFactory);
        JsonUtils.readingJavaTimeSupport(builder);
        JsonUtils.readingCanSupport(builder);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
        	customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * Use Jackson to write YAML.
     */
    private YAMLMapper createJacksonWriter(
            final Optional<YamlDumpCustomizer> dumpCustomizer,
            final JsonUtils.JacksonCustomizer ... customizers) {
        var yamlFactory = YAMLFactory.builder()
        		.disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
                .dumperOptions(dumpCustomizer
                        .map(YamlUtils::createDumpSettings)
                        .orElseGet(YamlUtils::createDumpSettings))
                .build();
        
        var builder = YAMLMapper.builder(yamlFactory);
        JsonUtils.writingJavaTimeSupport(builder);
        JsonUtils.writingCanSupport(builder);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
        	customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * @see YAMLFactoryBuilder#dumperOptions(DumpSettings)
     */
    private DumpSettings createDumpSettings(final YamlDumpCustomizer ... dumpCustomizers) {
    	var builder = DumpSettings.builder()
			.setIndent(2)
			.setBestLineBreak("\n"); // fixated for consistency
        for(YamlUtils.YamlDumpCustomizer customizer : dumpCustomizers) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    private LoadSettings createLoadSettings(final YamlLoadCustomizer ... loadCustomizers) {
        var builder = LoadSettings.builder();
        for(YamlUtils.YamlLoadCustomizer customizer : loadCustomizers) {
            customizer.accept(builder);
        }
        return builder.build();
    }

}
