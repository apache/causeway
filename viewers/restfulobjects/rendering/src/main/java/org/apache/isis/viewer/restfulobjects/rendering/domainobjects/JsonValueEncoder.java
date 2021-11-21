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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Similar to Isis' value encoding, but with additional support for JSON
 * primitives.
 */
@Service
@Named("isis.viewer.ro.JsonValueEncoder")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@Log4j2
public class JsonValueEncoder {

    @Inject private SpecificationLoader specificationLoader;

    @PostConstruct
    public void init() {

        //XXX no lombok val here
        Function<Object, ManagedObject> pojoToAdapter = pojo ->
            ManagedObject.lazy(specificationLoader, pojo);

        new JsonValueEncoder_Converters().asList(pojoToAdapter)
            .forEach(this::registerConverter);
    }

    private Map<Class<?>, JsonValueConverter> converterByClass = _Maps.newLinkedHashMap();

    private void registerConverter(final JsonValueConverter jvc) {
        jvc.getClasses().forEach(cls->converterByClass.put(cls, jvc));
    }

    public ManagedObject asAdapter(
            final ObjectSpecification objectSpec,
            final JsonRepresentation argValueRepr,
            final String format) {

        if(argValueRepr == null) {
            return null;
        }
        if (objectSpec == null) {
            throw new IllegalArgumentException("ObjectSpecification is required");
        }
        if (!argValueRepr.isValue()) {
            throw new IllegalArgumentException("Representation must be of a value");
        }
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            String reason = "ObjectSpec expected to have an EncodableFacet";
            throw new IllegalArgumentException(reason);
        }

        val cls = objectSpec.getCorrespondingClass();
        final JsonValueConverter jvc = converterByClass.get(cls);
        if(jvc == null) {
            // best effort
            if (argValueRepr.isString()) {
                final String argStr = argValueRepr.asString();
                return encodableFacet.fromEncodedString(argStr);
            }

            throw new IllegalArgumentException("Unable to parse value");
        }

        final ManagedObject asAdapter = jvc.asAdapter(argValueRepr, format);
        if(asAdapter != null) {
            return asAdapter;
        }

        // last attempt
        if (argValueRepr.isString()) {
            final String argStr = argValueRepr.asString();
            try {
                return encodableFacet.fromEncodedString(argStr);
            } catch(TextEntryParseException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        throw new IllegalArgumentException("Could not parse value '" + argValueRepr.asString() + "' as a " + objectSpec.getFullIdentifier());
    }

    public Object appendValueAndFormat(
            final ManagedObject objectAdapter,
            final ObjectSpecification objectSpecification,
            final JsonRepresentation repr,
            final String format,
            final boolean suppressExtensions) {

        val cls = objectSpecification.getCorrespondingClass();
        val jsonValueConverter = converterByClass.get(cls);
        if(jsonValueConverter != null) {
            return jsonValueConverter.appendValueAndFormat(objectAdapter, format, repr, suppressExtensions);
        } else {

            final Object value = ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)
                    ? NullNode.getInstance()
                    : toEncodedString(objectSpecification, objectAdapter.getPojo())
                        .map(Object.class::cast)
                        .orElseGet(()->{
                            log.warn("{Could not resolve an EncoderDecoder for {}, "
                                    + "falling back to rendering as 'null'. "
                                    + "Make sure the framework has access to a ValueSemanticsProvider<{}> "
                                    + "that implements EncoderDecoder<{}>}",
                                    objectSpecification.getLogicalTypeName(),
                                    objectSpecification.getCorrespondingClass().getSimpleName(),
                                    objectSpecification.getCorrespondingClass().getSimpleName());
                            return NullNode.getInstance();
                        });

            repr.mapPut("value", value);
            appendFormats(repr, "string", "string", suppressExtensions);
            return value;
        }
    }

    private static <T> Optional<String> toEncodedString(final ObjectSpecification spec, final T pojo) {
        return spec.lookupFacet(ValueFacet.class)
            .flatMap(ValueFacet::selectDefaultEncoderDecoder)
            .map(EncoderDecoder.class::cast)
            .map(codec->((EncoderDecoder<T>)codec).toEncodedString(pojo));
    }

    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final String format) {

        val objectSpec = adapter.getSpecification();
        val cls = objectSpec.getCorrespondingClass();

        val jsonValueConverter = converterByClass.get(cls);
        if(jsonValueConverter != null) {
            return jsonValueConverter.asObject(adapter, format);
        }

        // else
        val encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        return encodableFacet.toEncodedString(adapter);
    }


    static void appendFormats(final JsonRepresentation repr, final String format, final String xIsisFormat, final boolean suppressExtensions) {
        if(format != null) {
            repr.mapPut("format", format);
        }
        if(!suppressExtensions && xIsisFormat != null) {
            repr.mapPut("extensions.x-isis-format", xIsisFormat);
        }
    }

    static Object unwrapAsObjectElseNullNode(final ManagedObject adapter) {
        return adapter != null? adapter.getPojo(): NullNode.getInstance();
    }

    // -- NESTED TYPE DECLARATIONS

    public static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class JsonValueConverter {

        protected final String format;
        protected final String xIsisFormat;

        @Getter private final Can<Class<?>> classes;

        public JsonValueConverter(final String format, final String xIsisFormat, final Class<?>... classes) {
            this.format = format;
            this.xIsisFormat = xIsisFormat;
            this.classes = Can.ofArray(classes);
        }

        /**
         * The value, otherwise <tt>null</tt>.
         */
        public abstract ManagedObject asAdapter(JsonRepresentation repr, String format);

        public Object appendValueAndFormat(
                final ManagedObject objectAdapter,
                final String format,
                final JsonRepresentation repr,
                final boolean suppressExtensions) {

            final Object value = unwrapAsObjectElseNullNode(objectAdapter);
            repr.mapPut("value", value);
            appendFormats(repr, this.format, this.xIsisFormat, suppressExtensions);
            return value;
        }

        public Object asObject(final ManagedObject objectAdapter, final String format) {
            return objectAdapter.getPojo();
        }
    }


    /**
     * JUnit support
     */
    public static JsonValueEncoder forTesting(final SpecificationLoader specificationLoader) {
        val jsonValueEncoder = new JsonValueEncoder();
        jsonValueEncoder.specificationLoader = specificationLoader;
        jsonValueEncoder.init();
        return jsonValueEncoder;
    }

}
