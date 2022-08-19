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

import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.Getter;

public interface JsonValueConverter {

    /**
     * The value as pojo, otherwise <tt>null</tt>.
     */
    @Nullable
    Object recoverValueAsPojo(JsonRepresentation repr, String format);

    Object asObject(ManagedObject objectAdapter, String format);

    Object appendValueAndFormat(
            final ManagedObject objectAdapter,
            final String formatOverride,
            final JsonRepresentation repr,
            final boolean suppressExtensions);

    Can<Class<?>> getClasses();

    static abstract class Abstract implements JsonValueConverter {
        protected final String format;
        protected final String xIsisFormat;

        @Getter private final Can<Class<?>> classes;

        public Abstract(final String format, final String xIsisFormat, final Class<?>... classes) {
            this.format = format;
            this.xIsisFormat = xIsisFormat;
            this.classes = Can.ofArray(classes);
        }

        @Override
        public Object appendValueAndFormat(
                final ManagedObject objectAdapter,
                final String formatOverride,
                final JsonRepresentation repr,
                final boolean suppressExtensions) {

            final Object value = unwrapAsObjectElseNullNode(objectAdapter);
            repr.mapPut("value", value);
            appendFormats(repr, effectiveFormat(formatOverride), this.xIsisFormat, suppressExtensions);
            return value;
        }

        @Override
        public final Object asObject(final ManagedObject objectAdapter, final String format) {
            return objectAdapter.getPojo();
        }

        protected final String effectiveFormat(final String formatOverride) {
            return formatOverride!=null ? formatOverride : this.format;
        }

        static Object unwrapAsObjectElseNullNode(final ManagedObject adapter) {
            return adapter != null? adapter.getPojo(): NullNode.getInstance();
        }

        static void appendFormats(
                final JsonRepresentation repr,
                final String format,
                final String xIsisFormat,
                final boolean suppressExtensions) {
            if(format != null) {
                repr.mapPutString("format", format);
            }
            if(!suppressExtensions && xIsisFormat != null) {
                repr.mapPutString("extensions.x-isis-format", xIsisFormat);
            }
        }
    }

}
