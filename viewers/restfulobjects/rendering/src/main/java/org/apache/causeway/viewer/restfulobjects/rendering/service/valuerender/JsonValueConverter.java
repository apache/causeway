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
package org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender;

import java.util.OptionalInt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.HasObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender._JsonValueConverters.DefaultFormat;

import lombok.Getter;
import lombok.NonNull;

public interface JsonValueConverter {

    /**
     * The value recovered from {@link JsonRepresentation} as pojo, otherwise <tt>null</tt>.
     */
    @Nullable
    Object recoverValueAsPojo(JsonRepresentation repr, Context context);

    /**
     * A {@link JsonNode} or otherwise natively supported simple type.
     */
    Object asObject(ManagedObject objectAdapter, Context context);

    /**
     * Appends given value type representing {@link ManagedObject} to given
     * {@link JsonRepresentation}.
     */
    Object appendValueAndFormat(
            final ManagedObject objectAdapter,
            final Context context,
            final JsonRepresentation repr);

    /**
     * {@link Class} this converter is suited for.
     */
    Class<?> getValueClass();

    static interface Context extends HasObjectFeature {

        OptionalInt maxTotalDigits(@Nullable ManagedObject value);
        OptionalInt maxFractionalDigits(@Nullable ManagedObject value);
        boolean isSuppressExtensions();

        public static Context of(
                final @NonNull ObjectFeature objectFeature,
                final boolean suppressExtensions) {
            return new Context.InferredFromFacets(objectFeature, suppressExtensions);
        }

        public static Context forTesting(final Integer maxTotalDigits, final Integer maxFractionalDigits) {
            return new Context() {
                @Override public ObjectFeature getObjectFeature() {
                    throw _Exceptions.notImplemented(); }
                @Override public OptionalInt maxTotalDigits(final ManagedObject value) {
                    return OptionalInt.of(maxTotalDigits); }
                @Override public OptionalInt maxFractionalDigits(final ManagedObject value) {
                    return OptionalInt.of(maxFractionalDigits); }
                @Override public boolean isSuppressExtensions() {
                    return false; }
            };
        }

        @Getter
        @lombok.Value
        static class InferredFromFacets implements Context {
            final @NonNull ObjectFeature objectFeature;
            final boolean suppressExtensions;

            @Override
            public OptionalInt maxTotalDigits(final @Nullable ManagedObject value) {
                return Facets.maxTotalDigits(facetHolders(value));
            }

            @Override
            public OptionalInt maxFractionalDigits(final @Nullable ManagedObject value) {
                return Facets.maxFractionalDigits(facetHolders(value));
            }

            // look for facet on feature, else on the value's spec
            private Can<FacetHolder> facetHolders(final @Nullable ManagedObject value) {
                return ManagedObjects.isNullOrUnspecifiedOrEmpty(value)
                    ? Can.of(objectFeature)
                    : Can.of(objectFeature, value.getSpecification());
            }

        }

    }

    static abstract class Abstract implements JsonValueConverter {
        protected final String format;
        protected final String extendedFormat;

        @Getter private final Class<?> valueClass;

        public Abstract(final DefaultFormat format) {
            this.format = format.format;
            this.extendedFormat = format.extendedFormat;
            this.valueClass = format.valueClass;
        }

        @Override
        public Object appendValueAndFormat(
                final ManagedObject objectAdapter,
                final Context context,
                final JsonRepresentation repr) {

            final Object value = unwrapAsObjectElseNullNode(objectAdapter);
            repr.mapPut("value", value);
            appendFormats(repr, context);
            return value;
        }

        @Override
        public final Object asObject(final ManagedObject objectAdapter, final Context format) {
            return objectAdapter.getPojo();
        }

        protected final String effectiveFormat(final String formatOverride) {
            return formatOverride!=null ? formatOverride : format;
        }

        static Object unwrapAsObjectElseNullNode(final ManagedObject adapter) {
            return adapter != null? adapter.getPojo(): NullNode.getInstance();
        }

        void appendFormats(
                final JsonRepresentation repr,
                final Context context) {
            repr.putFormat(format);
            if(!context.isSuppressExtensions()) {
                repr.putExtendedFormat(extendedFormat);
            }
        }
    }

}
