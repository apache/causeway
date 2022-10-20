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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.Oid;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of a Wicket {@link IConverter} for
 * {@link ObjectMemento}s, converting to-and-from their stringified {@link Bookmark}s.
 */
@RequiredArgsConstructor
public class ConverterForObjectAdapterMemento implements IConverter<ObjectMemento> {

    private static final long serialVersionUID = 1L;

    /**
     * Converts string representation of {@link Oid} to
     * {@link ObjectMemento}.
     */
    @Override
    public ObjectMemento convertToObject(
            final String base64UrlEncodedMemento, final Locale locale) {
        val obj = ObjectMemento.destringFromUrlBase64(base64UrlEncodedMemento);

        //XXX ever used ?
        System.err.printf("ConverterForObjectAdapterMemento: convertTo ObjectMemento %s->%s%n", base64UrlEncodedMemento, obj);

        return obj;
    }

    /**
     * Converts {@link ObjectMemento} to string representation of
     * {@link Bookmark}.
     */
    @Override
    public String convertToString(final ObjectMemento memento, final Locale locale) {
        val string = ObjectMemento.enstringToUrlBase64(memento);

        //XXX ever used ?
        System.err.printf("ConverterForObjectAdapterMemento: convertFrom ObjectMemento %s->%s%n", memento.getBookmark(), string);

        return string;
    }

}
