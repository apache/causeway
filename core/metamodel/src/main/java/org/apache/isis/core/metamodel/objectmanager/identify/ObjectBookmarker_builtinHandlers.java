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
package org.apache.isis.core.metamodel.objectmanager.identify;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectBookmarker.Handler;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

class ObjectBookmarker_builtinHandlers {

    public static final String SERVICE_IDENTIFIER = "1";

    static class GuardAgainstOid implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getPojo() instanceof Oid;
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            throw new IllegalArgumentException("Cannot create a Bookmark for pojo, "
                    + "when pojo is instance of Bookmark. You might want to ask "
                    + "ObjectAdapterByIdProvider for an ObjectAdapter instead.");
        }

    }

    static class BookmarkForServices implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecification().isManagedBean();
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            final String identifier = SERVICE_IDENTIFIER;
            return Bookmark.forLogicalTypeAndIdentifier(
                    managedObject.getSpecification().getLogicalType(),
                    identifier);
        }

    }

    @Log4j2
    static class BookmarkForEntities implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecification().isEntity();
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val entityPojo = managedObject.getPojo();
            if(entityPojo==null) {
                val msg = String.format("entity '%s' is null, cannot identify", managedObject);
                throw _Exceptions.unrecoverable(msg);
            }
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                val msg = String.format("entity '%s' has no EntityFacet associated", managedObject);
                throw _Exceptions.unrecoverable(msg);
            }

            // fail early when detached entities are detected
            // should have been re-fetched at start of this request-cycle
            if(!managedObject.isBookmarkMemoized()
//                    && EntityUtil.getPersistenceStandard(managedObject)
//                        .map(PersistenceStandard::isJdo)
//                        .orElse(false)
                    && !entityFacet.getEntityState(entityPojo).isAttached()) {

                _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
                    _Debug.log("detached entity detected %s", entityPojo);
                });

                val msg = String.format(
                        "The persistence layer does not recognize given object of type %s, "
                        + "meaning the object has no identifier that associates it with the persistence layer. "
                        + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)",
                        entityPojo.getClass().getName());

                // in case of the exception getting swallowed, also write a log
                log.error(msg);

                throw _Exceptions.illegalArgument(msg);
            }

            val identifier = entityFacet.identifierFor(spec, entityPojo);
            return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
        }

    }

    static class BookmarkForValues implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ValueFacet.class);
        }

        @SneakyThrows
        @Override
        public Bookmark handle(final ManagedObject managedObject) {
//            throw _Exceptions.illegalArgument("cannot 'identify' the value type %s, "
//                    + "as values have no identifier",
//                    managedObject.getSpecification().getCorrespondingClass().getName());

            val spec = managedObject.getSpecification();

            if(java.io.Serializable.class.isAssignableFrom(spec.getCorrespondingClass())) {
                return new BookmarkForSerializable().handle(managedObject);
            }

            val valueFacet = spec.getFacet(ValueFacet.class);
            EncoderDecoder<Object> codec = (EncoderDecoder) valueFacet.selectDefaultEncoderDecoder()
                    .orElseThrow(()->_Exceptions.illegalArgument(
                            "Cannot create a bookmark for the value type %s, "
                          + "as no appropriate EncoderDecoder could be found.",
                          managedObject.getSpecification().getCorrespondingClass().getName()));

            val encoded = codec.toEncodedString(managedObject.getPojo()).getBytes();

            val identifier = _Strings.ofBytes(
                    _Bytes.asUrlBase64.apply(encoded),
                    StandardCharsets.UTF_8);

            return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
        }

    }

    static class BookmarkForSerializable implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            return spec.isViewModel()
                    && java.io.Serializable.class.isAssignableFrom(spec.getCorrespondingClass());
        }

        @SneakyThrows
        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val baos = new ByteArrayOutputStream();
            try(val oos = new ObjectOutputStream(baos)) {
                oos.writeObject(managedObject.getPojo());
                val identifier = _Strings.ofBytes(
                        _Bytes.asUrlBase64.apply(baos.toByteArray()),
                        StandardCharsets.UTF_8);
                return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
            }
        }

    }

    static class BookmarkForViewModels implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return managedObject.getSpecification().containsFacet(ViewModelFacet.class);
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {

            if(managedObject.isBookmarkMemoized()) {
                return managedObject.getBookmark().get();
            }

            val spec = managedObject.getSpecification();
            val recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
            return recreatableObjectFacet.serializeToBookmark(managedObject);
        }

    }

    static class BookmarkForOthers implements Handler {

        @Override
        public boolean isHandling(final ManagedObject managedObject) {
            return true; // try to handle anything
        }

        @Override
        public Bookmark handle(final ManagedObject managedObject) {
            val spec = managedObject.getSpecification();
            val identifier = UUID.randomUUID().toString();
            return Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), identifier);
        }
    }

}
