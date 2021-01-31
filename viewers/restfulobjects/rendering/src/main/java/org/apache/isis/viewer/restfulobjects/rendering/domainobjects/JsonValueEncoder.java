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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.NonNull;
import lombok.val;


/**
 * Similar to Isis' value encoding, but with additional support for JSON
 * primitives.
 */
@Service
@Named("isis.viewer.ro.JsonValueEncoder")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("Default")
@Singleton
public class JsonValueEncoder {

    @Inject private SpecificationLoader specificationLoader;
    
    @PostConstruct
    public void init() {
        
        //XXX no lombok val here
        Function<Object, ManagedObject> pojoToAdapter = pojo ->
            ManagedObject.of(specificationLoader::loadSpecification, pojo);
        
        new JsonValueEncoder_Converters().asList(pojoToAdapter)
            .forEach(this::registerConverter);
    }
    
    private Map<ObjectSpecId, JsonValueConverter> converterBySpecId = _Maps.newLinkedHashMap();

    private void registerConverter(JsonValueConverter jvc) {
        for (val specId : jvc.getSpecIds()) {
            converterBySpecId.put(specId, jvc);
        }
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

        final ObjectSpecId specId = objectSpec.getSpecId();
        final JsonValueConverter jvc = converterBySpecId.get(specId);
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
            ManagedObject objectAdapter,
            ObjectSpecification objectSpecification,
            JsonRepresentation repr,
            String format,
            boolean suppressExtensions) {

        val specId = objectSpecification.getSpecId();
        val jsonValueConverter = converterBySpecId.get(specId);
        if(jsonValueConverter != null) {
            return jsonValueConverter.appendValueAndFormat(objectAdapter, format, repr, suppressExtensions);
        } else {
            val encodableFacet = objectSpecification.getFacet(EncodableFacet.class);
            if (encodableFacet == null) {
                throw _Exceptions.illegalArgument(
                        "objectSpec '%s' expected to have EncodableFacet "
                        + "or a registered JsonValueConverter", 
                        specId);
            }
            Object value = objectAdapter != null
                    ? encodableFacet.toEncodedString(objectAdapter)
                            : NullNode.getInstance();
            repr.mapPut("value", value);
            appendFormats(repr, "string", "string", suppressExtensions);
            return value;
        }
    }

    @Nullable
    public Object asObject(final @NonNull ManagedObject adapter, final String format) {
        
        requires(adapter, "adapter");
        
        val objectSpec = adapter.getSpecification();
        
        val jsonValueConverter = converterBySpecId.get(objectSpec.getSpecId());
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


    static void appendFormats(JsonRepresentation repr, String format, String xIsisFormat, boolean suppressExtensions) {
        if(format != null) {
            repr.mapPut("format", format);
        }
        if(!suppressExtensions && xIsisFormat != null) {
            repr.mapPut("extensions.x-isis-format", xIsisFormat);
        }
    }

    static Object unwrapAsObjectElseNullNode(ManagedObject adapter) {
        return adapter != null? adapter.getPojo(): NullNode.getInstance();
    }

//    ManagedObject adapterFor(Object pojo) {
//        return objectAdapterProvider.adapterFor(pojo);
//    }
    
    // -- NESTED TYPE DECLARATIONS
    
    public static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class JsonValueConverter {

        protected final String format;
        protected final String xIsisFormat;
        private final Class<?>[] classes;

        public JsonValueConverter(String format, String xIsisFormat, Class<?>... classes) {
            this.format = format;
            this.xIsisFormat = xIsisFormat;
            this.classes = classes;
        }

        public List<ObjectSpecId> getSpecIds() {
            return _NullSafe.stream(classes)
                    .map((Class<?> cls) ->ObjectSpecId.of(cls.getName()))
                    .collect(Collectors.toList());
        }

        /**
         * The value, otherwise <tt>null</tt>.
         */
        public abstract ManagedObject asAdapter(JsonRepresentation repr, String format);

        public Object appendValueAndFormat(
                ManagedObject objectAdapter, 
                String format, 
                JsonRepresentation repr, 
                boolean suppressExtensions) {
            
            final Object value = unwrapAsObjectElseNullNode(objectAdapter);
            repr.mapPut("value", value);
            appendFormats(repr, this.format, this.xIsisFormat, suppressExtensions);
            return value;
        }

        public Object asObject(ManagedObject objectAdapter, String format) {
            return objectAdapter.getPojo();
        }
    }
    
    
    /**
     * JUnit support
     */
    public static JsonValueEncoder forTesting(SpecificationLoader specificationLoader) {
        val jsonValueEncoder = new JsonValueEncoder();
        jsonValueEncoder.specificationLoader = specificationLoader;
        jsonValueEncoder.init();
        return jsonValueEncoder;
    }

}
