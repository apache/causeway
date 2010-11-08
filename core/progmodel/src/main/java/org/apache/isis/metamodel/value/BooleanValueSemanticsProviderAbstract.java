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


package org.apache.isis.metamodel.value;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.TextEntryParseException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.IsisUtils;
import org.apache.isis.metamodel.facets.value.BooleanValueFacet;


public abstract class BooleanValueSemanticsProviderAbstract extends ValueSemanticsProviderAbstract implements BooleanValueFacet {

    private static Class<? extends Facet> type() {
        return BooleanValueFacet.class;
    }

    private static final int TYPICAL_LENGTH = 5;
    private static final boolean IMMUTABLE = true;
    private static final boolean EQUAL_BY_CONTENT = true;

    public BooleanValueSemanticsProviderAbstract(
    		final FacetHolder holder, 
    		final Class<?> adaptedClass, 
    		final Object defaultValue,
            final IsisConfiguration configuration, 
            final SpecificationLoader specificationLoader, 
            final RuntimeContext runtimeContext) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, defaultValue, configuration, specificationLoader, runtimeContext);
    }

    // //////////////////////////////////////////////////////////////////
    // Parsing
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Object doParse(final Object original, final String entry) {
        String compareTo = entry.trim().toLowerCase();
        if ("true".equals(compareTo)) {
            return Boolean.TRUE;
        } else if ("false".startsWith(compareTo)) {
            return Boolean.FALSE;
        } else {
            throw new TextEntryParseException("Not a logical value " + entry);
        }
    }

    @Override
    public String titleString(final Object value) {
        return value == null ? "" : isSet(value) ? "True" : "False";
    }
    
    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }

    // //////////////////////////////////////////////////////////////////
    // Encode, Decode
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        return isSet(object) ? "T" : "F";
    }

    @Override
    protected Object doRestore(final String data) {
        if (data.length() != 1) {
            throw new IsisException("Invalid data for logical, expected 1 byte, got " + data.length());
        }
        switch (data.charAt(0)) {
        case 'T':
            return Boolean.TRUE;
        case 'F':
            return Boolean.FALSE;
        default:
            throw new IsisException("Invalid data for logical, expected 'T', 'F' or 'N, but got " + data.charAt(0));
        }
    }

    private boolean isSet(final Object value) {
        return ((Boolean) value).booleanValue();
    }

    // //////////////////////////////////////////////////////////////////
    // BooleanValueFacet
    // //////////////////////////////////////////////////////////////////

    public boolean isSet(final ObjectAdapter adapter) {
        if (!IsisUtils.exists(adapter)) {
            return false;
        }
        final Object object = adapter.getObject();
        final Boolean objectAsBoolean = (Boolean) object;
        return objectAsBoolean.booleanValue();
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "BooleanValueSemanticsProvider";
    }

}
