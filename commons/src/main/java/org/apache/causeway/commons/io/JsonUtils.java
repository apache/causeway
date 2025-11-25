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
import java.util.function.Consumer;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Generics;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

/**
 * Utilities to convert from and to JSON format.
 *
 * @since 2.0 refined for 4.0 {@index}
 */
@UtilityClass
@Slf4j
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

    @SuppressWarnings("rawtypes")
	@FunctionalInterface
    public interface JacksonCustomizer extends Consumer<MapperBuilder> {
        public static <T> JacksonCustomizer wrapXmlAdapter(final XmlAdapter<String, T> xmlAdapter) {
            @SuppressWarnings("unchecked")
            var type = (Class<T>) _Generics.streamGenericTypeArgumentsOfType(xmlAdapter.getClass(), XmlAdapter.class)
                .skip(1)
                .findFirst()
                .orElseThrow(()->_Exceptions.unsupportedOperation(
                        "Failed to autodetect second generic type argument of class %s. "
                        + "Use variant JacksonCustomizer.wrapXmlAdapter(type, xmlAdapter) "
                        + "to provide the type explicitely.",
                        xmlAdapter.getClass().getName()));
            return wrapXmlAdapter(type, xmlAdapter);
        }
        public static <T> JacksonCustomizer wrapXmlAdapter(final Class<T> type, final XmlAdapter<String, T> xmlAdapter) {
            return builder->
                builder.addModule(new SimpleModule()
                        .addSerializer(new XSerializer<T>(type, xmlAdapter))
                        .addDeserializer(type, new XDeserializer<T>(type, xmlAdapter)));
        }
    }

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
            return Try.call(()->createJacksonReader(customizers).readValue(is, mappedType));
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
                var mapper = createJacksonReader(customizers);
                var collectionType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
                return mapper.readValue(is, collectionType);
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

    /** enable indentation for the underlying generator */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> indentedOutput(final MapperBuilder<M, B> builder) {
        return builder.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** only properties with non-null values are to be included */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> onlyIncludeNonNull(final MapperBuilder<M, B> builder) {
    	return builder.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL));
    }

    /** add support for JAXB annotations */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> jaxbAnnotationSupport(final MapperBuilder<M, B> builder) {
        return builder.addModule(new JakartaXmlBindAnnotationModule());
    }

    /** add support for reading java.time (ISO) */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> readingJavaTimeSupport(final MapperBuilder<M, B> builder) {
        builder.disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        return builder;
    }

    /** add support for writing java.time (ISO) */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> writingJavaTimeSupport(final MapperBuilder<M, B> builder) {
    	builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }

    // -- CAN SUPPORT

    static class CanDeserializer extends ValueDeserializer<Can<?>> {
        private Class<?> elementType;
        public CanDeserializer(final @NonNull Class<?> elementType) {
            this.elementType = elementType;
        }
        @Override
        public CanDeserializer createContextual(DeserializationContext ctxt, BeanProperty beanProperty) {
            var type = ctxt.getContextualType() != null
                ? ctxt.getContextualType()
                : beanProperty!=null
                    ? beanProperty.getMember().getType()
                    : null;
            var elementType = type!=null && type.containedTypeCount()==1
                    ? type.containedType(0).getRawClass()
                    : Object.class;
            return new CanDeserializer(elementType);
        }
        @Override
        public Can<?> deserialize(
                final JsonParser p, final DeserializationContext ctxt) {
            var listType = ctxt.getTypeFactory().constructCollectionType(List.class, elementType);
            var list = ctxt.readValue(p, listType);
            return Can.ofCollection(_Casts.uncheckedCast(list));
        }
    }
    /** add support for reading Can<T> */
	public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> readingCanSupport(final MapperBuilder<M, B> builder) {
    	builder.addModule(new SimpleModule().addDeserializer(Can.class, new CanDeserializer(Object.class)));
        return builder;
    }

    static class CanSerializer extends StdSerializer<Can<?>> {
        protected CanSerializer() { super(Can.class); }
        @Override
        public void serialize(final Can<?> value, final JsonGenerator gen,
                final SerializationContext context) throws JacksonException {
            gen.writePOJO(value.toList());
        }
    }
    /** add support for writing Can<T> */
    public <M extends ObjectMapper, B extends MapperBuilder<M,B>> MapperBuilder<M, B> writingCanSupport(final MapperBuilder<M, B> builder) {
    	builder.addModule(new SimpleModule().addSerializer(new CanSerializer()));
        return builder;
    }

    // -- XML ADAPTER SUPPORT

    static class XSerializer<T> extends StdSerializer<T> {
        private final XmlAdapter<String, T> xmlAdapter;
        protected XSerializer(final Class<T> type, final XmlAdapter<String, T> xmlAdapter) {
            super(type);
            this.xmlAdapter = xmlAdapter;
        }
        @Override
        public void serialize(final T value, final JsonGenerator gen,
                final SerializationContext context) throws JacksonException {
            String stringified;
            try {
                stringified = this.xmlAdapter.marshal(value);
            } catch (Exception e) {
                throw _Exceptions.unrecoverable("Unable to marshal: " + e.getMessage(), e);
            }
            gen.writeString(stringified);
        }
    }

    static class XDeserializer<T> extends StdDeserializer<T> {
        private final XmlAdapter<String, T> xmlAdapter;
        protected XDeserializer(final Class<T> type, final XmlAdapter<String, T> xmlAdapter) {
            super(type);
            this.xmlAdapter = xmlAdapter;
        }
        @Override
        public T deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
            String stringified = ctxt.readValue(p, String.class);
            try {
                return xmlAdapter.unmarshal(stringified);
            } catch (Exception e) {
                throw _Exceptions.unrecoverable("Unable to unmarshal (to type " + _valueType + "): " + e.getMessage(), e);
            }
        }
    }

    // -- MAPPER FACTORY

    private JsonMapper createJacksonReader(
            final JsonUtils.JacksonCustomizer ... customizers) {
		var builder = JsonMapper.builder();
		readingJavaTimeSupport(builder);
		readingCanSupport(builder);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
        	customizer.accept(builder);
        }
        return builder.build();
    }

    private JsonMapper createJacksonWriter(
            final JsonUtils.JacksonCustomizer ... customizers) {
    	var builder = JsonMapper.builder();
    	writingJavaTimeSupport(builder);
    	writingCanSupport(builder);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
        	customizer.accept(builder);
        }
        return builder.build();
    }

}
