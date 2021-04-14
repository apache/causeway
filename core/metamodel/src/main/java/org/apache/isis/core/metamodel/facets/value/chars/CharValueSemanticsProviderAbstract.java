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

package org.apache.isis.core.metamodel.facets.value.chars;

import java.text.DecimalFormat;

import org.apache.isis.applib.exceptions.recoverable.InvalidEntryException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public abstract class CharValueSemanticsProviderAbstract extends ValueSemanticsProviderAndFacetAbstract<Character> implements CharValueFacet {

    private static Class<? extends Facet> type() {
        return CharValueFacet.class;
    }

    private static final Character DEFAULT_VALUE = Character.valueOf((char) 0);
    private static final int MAX_LENGTH = 1;
    private static final int TYPICAL_LENGTH = MAX_LENGTH;

    public CharValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Character> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    public Character doParse(final Object context, final String entry) {
        if (entry.length() > 1) {
            throw new InvalidEntryException("Only a single character is required");
        } else {
            return Character.valueOf(entry.charAt(0));
        }
    }

    @Override
    public String titleString(final Object value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(new DecimalFormat(usingMask), value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Character object) {
        return object.toString();
    }

    @Override
    protected Character doRestore(final String data) {
        return Character.valueOf(data.charAt(0));
    }

    // //////////////////////////////////////////////////////////////////
    // CharValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Character charValue(final ManagedObject object) {
        return object == null ? null : (Character) object.getPojo();
    }

    @Override
    public ManagedObject createValue(final Character value) {
        return getObjectManager().adapt(value);
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "CharacterValueSemanticsProvider";
    }

}
