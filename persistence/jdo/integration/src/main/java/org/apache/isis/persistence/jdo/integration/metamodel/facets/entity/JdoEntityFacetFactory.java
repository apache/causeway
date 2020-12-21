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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.entity;

import javax.inject.Inject;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.persistence.jdo.applib.integration.JdoSupportService;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class JdoEntityFacetFactory extends FacetFactoryAbstract {

    @Inject private JdoFacetContext jdoFacetContext;
    @Inject private JdoSupportService jdoSupportService;
    
    public JdoEntityFacetFactory() {
        super(ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val entityAnnotation = Annotations.getAnnotation(cls, PersistenceCapable.class);
        if (entityAnnotation == null) {
            return;
        }
        
        val facetHolder = processClassContext.getFacetHolder();
        val mmc = facetHolder.getMetaModelContext();
        val jdoEntityFacet = new JdoEntityFacet(facetHolder, cls, 
                mmc, jdoFacetContext, jdoSupportService);
            
        addFacet(jdoEntityFacet);
    }
    
    
    // -- HELPER - OBJECT ID SERIALIZATION
    

    @SuppressWarnings("rawtypes")
    static JdoObjectIdSerializer createJdoObjectIdSerializer(
            final @NonNull Class<?> primaryKeyType,
            final @NonNull ServiceRegistry serviceRegistry) {
        
        
        // not strictly required, but to have simpler entity URLs for simple primary-key types
        {
            if(primaryKeyType.equals(Long.class)
                    || primaryKeyType.equals(long.class)) {
                return new LongIdSerializer();
            }
            if(primaryKeyType.equals(Integer.class)
                    || primaryKeyType.equals(int.class)) {
                return new IntegerIdSerializer();
            }
            if(primaryKeyType.equals(Short.class)
                    || primaryKeyType.equals(short.class)) {
                return new ShortIdSerializer();
            }
            if(primaryKeyType.equals(Byte.class)
                    || primaryKeyType.equals(byte.class)) {
                return new ByteIdSerializer();
            }
        }
        
        val codec = serviceRegistry.lookupServiceElseFail(UrlEncodingService.class);
        val serializer = serviceRegistry.lookupServiceElseFail(SerializingAdapter.class);
        return new JdoObjectIdSerializerUsingMementos<>(primaryKeyType, codec, serializer);
    }
    
    
    @RequiredArgsConstructor
    static abstract class JdoObjectIdSerializer<T> {
        @SuppressWarnings("unused")
        final Class<T> primaryKeyType;
        abstract String stringify(T id);
        abstract T parse(String stringifiedPrimaryKey);
    }
    
    private static class LongIdSerializer extends JdoObjectIdSerializer<Long> {
        public LongIdSerializer() { super(Long.class); }
        @Override String stringify(Long id) { return id.toString(); }
        @Override Long parse(String stringifiedPrimaryKey) { return Long.parseLong(stringifiedPrimaryKey); }
    }
    private static class IntegerIdSerializer extends JdoObjectIdSerializer<Integer> {
        public IntegerIdSerializer() { super(Integer.class); }
        @Override String stringify(Integer id) { return id.toString(); }
        @Override Integer parse(String stringifiedPrimaryKey) { return Integer.parseInt(stringifiedPrimaryKey); }
    }
    private static class ShortIdSerializer extends JdoObjectIdSerializer<Short> {
        public ShortIdSerializer() { super(Short.class); }
        @Override String stringify(Short id) { return id.toString(); }
        @Override Short parse(String stringifiedPrimaryKey) { return Short.parseShort(stringifiedPrimaryKey); }
    }
    private static class ByteIdSerializer extends JdoObjectIdSerializer<Byte> {
        public ByteIdSerializer() { super(Byte.class); }
        @Override String stringify(Byte id) { return id.toString(); }
        @Override Byte parse(String stringifiedPrimaryKey) { return Byte.parseByte(stringifiedPrimaryKey); }
    }
    
    private static class JdoObjectIdSerializerUsingMementos<T> extends JdoObjectIdSerializer<T> {
        private final UrlEncodingService codec;
        private final SerializingAdapter serializer;
        
        public JdoObjectIdSerializerUsingMementos(
                final @NonNull Class<T> primaryKeyType, 
                final @NonNull UrlEncodingService codec,
                final @NonNull SerializingAdapter serializer) {
            super(primaryKeyType);
            this.codec = codec;
            this.serializer = serializer;
        }
       
        public String stringify(Object id) {
            return newMemento().put("id", id).asString();
        }
        
        public T parse(final String stringifiedPrimaryKey) {
            if(_Strings.isEmpty(stringifiedPrimaryKey)) {
                return null;
            }
            return _Casts.uncheckedCast(parseMemento(stringifiedPrimaryKey).get("id", Object.class));
        }
       
        // -- HELPER

        private _Mementos.Memento newMemento(){
            return _Mementos.create(codec, serializer);
        }

        private _Mementos.Memento parseMemento(String input){
            return _Mementos.parse(codec, serializer, input);
        }
        
    }
    


}
