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

package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of a Wicket {@link IConverter} for
 * {@link ObjectAdapterMemento}s, converting to-and-from their {@link Oid}'s
 * string representation.
 */
@RequiredArgsConstructor
public class ConverterForObjectAdapterMemento implements IConverter<ObjectAdapterMemento> {

    private static final long serialVersionUID = 1L;
    
    private final transient IsisWebAppCommonContext commonContext;

    /**
     * Converts string representation of {@link Oid} to
     * {@link ObjectAdapterMemento}.
     */
    @Override
    public ObjectAdapterMemento convertToObject(final String value, final Locale locale) {
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        val rootOid = RootOid.deStringEncoded(value);
        return commonContext.mementoFor(rootOid);
    }

    /**
     * Converts {@link ObjectAdapterMemento} to string representation of
     * {@link RootOid}.
     */
    @Override
    public String convertToString(final ObjectAdapterMemento memento, final Locale locale) {
        if (memento == null) {
            return null;
        }
        val adapter = commonContext.reconstructObject(memento);
        val spec = adapter.getSpecification();
        if(spec!=null && spec.isValue()) {
            return memento.toString();
        }
        val oid = ManagedObject._identify(adapter);
        return oid.enString();
    }

}
