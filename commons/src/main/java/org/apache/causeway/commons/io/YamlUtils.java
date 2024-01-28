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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * Utilities to convert from and to YAML format.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class YamlUtils {

    /**
     * @deprecated We rely on Jackson to parse YAML. Might also replace SnakeYaml with Jackson to write YAML.
     */
    @Deprecated
    @FunctionalInterface
    public interface YamlDumpCustomizer extends UnaryOperator<DumperOptions> {}

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
        mapper = JsonUtils.writingJavaTimeSupport(mapper);
        mapper = JsonUtils.writingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

    @Deprecated
    private Yaml createMapperLegacy(
            final Class<?> mappedType,
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
        presenter.setPropertyUtils(new PropertyUtils2());
        presenter.addClassTag(mappedType, Tag.MAP);

        var loaderOptions = new LoaderOptions();
        var mapper = new Yaml(new Constructor(mappedType, loaderOptions), presenter, dumperOptions, loaderOptions);
        return mapper;
    }

    // -- REPRESENTING RECORD TYPES

    static class PropertyUtils2 extends PropertyUtils {

        @Override
        protected Map<String, Property> getPropertiesMap(final Class<?> type, final BeanAccess bAccess) {
            if(type==Class.class) {
                setAllowReadOnlyProperties(true);
                try {
                    val properties = new LinkedHashMap<String, Property>();
                    val propertyDescriptor = new PropertyDescriptor("name", className(), null);
                    properties.put("name", new MethodProperty(propertyDescriptor));
                    return properties;
                } catch (IntrospectionException e) {
                    throw new YAMLException(e);
                }
            }
            if(type.isRecord()) {
                setAllowReadOnlyProperties(true);
                try {
                    val properties = new LinkedHashMap<String, Property>();
                    for(RecordComponent rc: type.getRecordComponents()) {
                        val propertyDescriptor = new PropertyDescriptor(rc.getName(), rc.getAccessor(), null);
                        properties.put(rc.getName(), new MethodProperty(propertyDescriptor));
                    }
                    return postProcessMap(properties);
                } catch (IntrospectionException e) {
                    throw new YAMLException(e);
                }
            }
            val map = super.getPropertiesMap(type, bAccess);
            return postProcessMap(map);
        }

        private Map<String, Property> postProcessMap(final Map<String, Property> map) {
            //debug
            //System.err.printf("%s map: %s%n", type.getName(), map);
            map.replaceAll((k, v)->
                Can.class.isAssignableFrom(v.getType())
                    && v instanceof MethodProperty // no field support yet
                    ? MethodPropertyFromCanToList.wrap((MethodProperty)v)
                    : v);
            return map;
        }

        @Getter(lazy = true) @Accessors(fluent=true)
        private final static Method className = lookupClassName();
        @SneakyThrows
        private static Method lookupClassName() {
            return Class.class.getMethod("getName");
        }

        @Getter(lazy = true) @Accessors(fluent=true)
        private final static Method canToList = lookupCanToList();
        @SneakyThrows
        private static Method lookupCanToList() {
            return Can.class.getMethod("toList");
        }

    }

    /** Wraps any {@link MethodProperty} that represent a {@link Can} type
     * and acts as a {@link List} representing MethodProperty facade instead. */
    static class MethodPropertyFromCanToList extends MethodProperty {

        @SneakyThrows
        static MethodPropertyFromCanToList wrap(final MethodProperty wrappedMethodProperty) {
            return new MethodPropertyFromCanToList(
                    wrappedMethodProperty,
                    new PropertyDescriptor(wrappedMethodProperty.getName(), null, null));
        }

        final MethodProperty wrappedMethodProperty;

        MethodPropertyFromCanToList(
                final MethodProperty wrappedMethodProperty,
                final PropertyDescriptor property) {
            super(property);
            this.wrappedMethodProperty = wrappedMethodProperty;
        }

        @Override public Object get(final Object object) {
            return ((Can<?>)wrappedMethodProperty.get(object)).toList();
        }
        @Override public Class<?> getType() { return List.class; }
        @Override public boolean isReadable() { return true; }

    }

}
