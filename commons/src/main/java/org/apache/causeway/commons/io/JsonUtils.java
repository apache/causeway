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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Generics;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Utilities to convert from and to JSON format.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class JsonUtils {

    @FunctionalInterface
    public interface JacksonCustomizer extends UnaryOperator<ObjectMapper> {
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
            return mapper->
                mapper.registerModule(new SimpleModule()
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
    public ObjectMapper indentedOutput(final ObjectMapper mapper) {
        return mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** only properties with non-null values are to be included */
    public ObjectMapper onlyIncludeNonNull(final ObjectMapper mapper) {
        return mapper.setSerializationInclusion(Include.NON_NULL);
    }

    /** add support for JDK 8, e.g. {@link Optional} */
    public ObjectMapper jdk8Support(final ObjectMapper mapper) {
        return mapper.registerModule(new Jdk8Module());
    }

    /** add support for JAXB annotations */
    public ObjectMapper jaxbAnnotationSupport(final ObjectMapper mapper) {
        return mapper.registerModule(new JaxbAnnotationModule());
    }

    /** add support for reading java.time (ISO) */
    public ObjectMapper readingJavaTimeSupport(final ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        return mapper;
    }

    /** add support for writing java.time (ISO) */
    public ObjectMapper writingJavaTimeSupport(final ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // -- CAN SUPPORT

    static class CanDeserializer extends JsonDeserializer<Can<?>> implements ContextualDeserializer {
        private Class<?> elementType;
        public CanDeserializer(final @NonNull Class<?> elementType) {
            this.elementType = elementType;
        }
        @Override
        public JsonDeserializer<?> createContextual(final DeserializationContext ctxt, final BeanProperty beanProperty) throws JsonMappingException {
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
                final JsonParser p, final DeserializationContext ctxt) throws IOException {
            var listType = ctxt.getTypeFactory().constructCollectionType(List.class, elementType);
            var list = ctxt.readValue(p, listType);
            return Can.ofCollection(_Casts.uncheckedCast(list));
        }
    }
    /** add support for reading Can<T> */
    public ObjectMapper readingCanSupport(final ObjectMapper mapper) {
        mapper.registerModule(new SimpleModule().addDeserializer(Can.class, new CanDeserializer(Object.class)));
        return mapper;
    }

    static class CanSerializer extends StdSerializer<Can<?>> {
        private static final long serialVersionUID = 1L;
        protected CanSerializer() { super(Can.class, false); }
        @Override
        public void serialize(final Can<?> value, final JsonGenerator gen,
                final SerializerProvider provider) throws IOException {
            gen.writeObject(value.toList());
        }
    }
    /** add support for writing Can<T> */
    public ObjectMapper writingCanSupport(final ObjectMapper mapper) {
        mapper.registerModule(new SimpleModule().addSerializer(new CanSerializer()));
        return mapper;
    }

    // -- XML ADAPTER SUPPORT

    static class XSerializer<T> extends StdSerializer<T> {
        private static final long serialVersionUID = 1L;
        private final XmlAdapter<String, T> xmlAdapter;
        protected XSerializer(final Class<T> type, final XmlAdapter<String, T> xmlAdapter) {
            super(type, false);
            this.xmlAdapter = xmlAdapter;
        }
        @Override
        public void serialize(final T value, final JsonGenerator gen,
                final SerializerProvider provider) throws IOException {
            String stringified;
            try {
                stringified = this.xmlAdapter.marshal(value);
            } catch (Exception e) {
                throw new JsonMappingException(gen, "Unable to marshal: " + e.getMessage(), e);
            }
            gen.writeObject(stringified);
        }
    }

    static class XDeserializer<T> extends StdDeserializer<T> {
        private static final long serialVersionUID = 1L;
        private final XmlAdapter<String, T> xmlAdapter;
        protected XDeserializer(final Class<T> type, final XmlAdapter<String, T> xmlAdapter) {
            super(type);
            this.xmlAdapter = xmlAdapter;
        }
        @Override
        public T deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JacksonException {
            String stringified = ctxt.readValue(p, String.class);
            try {
                return xmlAdapter.unmarshal(stringified);
            } catch (Exception e) {
                throw new JsonMappingException(p, "Unable to unmarshal (to type " + _valueType + "): " + e.getMessage(), e);
            }
        }
    }

    // -- MAPPER FACTORY

    private ObjectMapper createJacksonReader(
            final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper();
        mapper = jdk8Support(mapper);
        mapper = readingJavaTimeSupport(mapper);
        mapper = readingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

    private ObjectMapper createJacksonWriter(
            final JsonUtils.JacksonCustomizer ... customizers) {
        var mapper = new ObjectMapper();
        mapper = jdk8Support(mapper);
        mapper = writingJavaTimeSupport(mapper);
        mapper = writingCanSupport(mapper);
        for(JsonUtils.JacksonCustomizer customizer : customizers) {
            mapper = Optional.ofNullable(customizer.apply(mapper))
                    .orElse(mapper);
        }
        return mapper;
    }

}
