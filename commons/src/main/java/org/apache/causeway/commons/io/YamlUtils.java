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
import java.io.OutputStreamWriter;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import org.apache.causeway.commons.collections.Can;
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
            final YamlUtils.YamlLoadCustomizer ... customizers) {
        return tryRead(mappedType, DataSource.ofStringUtf8(stringUtf8), customizers);
    }

    /**
     * Tries to deserialize YAML content from given {@link DataSource} into an instance of
     * given {@code requiredType}.
     */
    public <T> Try<T> tryRead(
            final @NonNull Class<T> mappedType,
            final @NonNull DataSource source,
            final YamlUtils.YamlLoadCustomizer ... customizers) {
        return source.tryReadAll((final InputStream is)->{
            return Try.call(()->createMapper(mappedType, Can.ofArray(customizers), Can.empty())
                    .load(is));
        });
    }

    // -- WRITING

    /**
     * Writes given {@code pojo} to given {@link DataSink}.
     */
    public void write(
            final @Nullable Object pojo,
            final @NonNull DataSink sink,
            final YamlUtils.YamlDumpCustomizer ... customizers) {
        if(pojo==null) return;
        sink.writeAll(os->
            Try.run(()->createMapper(pojo.getClass(), Can.empty(), Can.ofArray(customizers)).dump(pojo, new OutputStreamWriter(os))));
    }

    /**
     * Converts given {@code pojo} to an UTF8 encoded {@link String}.
     * @return <code>null</code> if pojo is <code>null</code>
     */
    @SneakyThrows
    @Nullable
    public static String toStringUtf8(
            final @Nullable Object pojo,
            final YamlUtils.YamlDumpCustomizer ... customizers) {
        return pojo!=null
                ? createMapper(pojo.getClass(), Can.empty(), Can.ofArray(customizers)).dump(pojo)
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

    // -- MAPPER FACTORY

    private Yaml createMapper(
            final Class<?> mappedType,
            final Can<YamlUtils.YamlLoadCustomizer> loadCustomizers,
            final Can<YamlUtils.YamlDumpCustomizer> dumpCustomizers) {
        var dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setLineBreak(LineBreak.UNIX); // fixated for consistency
        //options.setPrettyFlow(true);
        //options.setDefaultFlowStyle(FlowStyle.BLOCK);
        for(YamlUtils.YamlDumpCustomizer customizer : dumpCustomizers) {
            dumperOptions = Optional.ofNullable(customizer.apply(dumperOptions))
                    .orElse(dumperOptions);
        }
        
        var presenter = new Representer(dumperOptions);
        presenter.addClassTag(mappedType, Tag.MAP);

        var loaderOptions = new LoaderOptions();
        for(YamlUtils.YamlLoadCustomizer customizer : loadCustomizers) {
            loaderOptions = Optional.ofNullable(customizer.apply(loaderOptions))
                    .orElse(loaderOptions);
        }
        var mapper = new Yaml(new Constructor(mappedType, loaderOptions), presenter, dumperOptions, loaderOptions);
        return mapper;
    }

}
