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
package org.apache.isis.core.metamodel.facets.value.password;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class PasswordValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<Password>
implements PasswordValueFacet {

    private static final Class<? extends Facet> type() {
        return PasswordValueFacet.class;
    }

    private static final Password DEFAULT_VALUE = null; // no default
    private static final int TYPICAL_LENGTH = 12;

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public PasswordValueSemanticsProvider() {
        this(null);
    }

    public PasswordValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Password.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Password doParse(final Object context, final String text) {
        return new Password(text);
    }

    @Override
    public String titleString(final Object object) {
        return object == null ? "" : password(object).toString();
    }

    @Override
    public String titleStringWithMask(final Object object, final String usingMask) {
        return titleString(object);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Password object) {
        return password(object).getPassword();
    }

    @Override
    protected Password doRestore(final String data) {
        return new Password(data);
    }

    // //////////////////////////////////////////////////////////////////
    // PasswordValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean checkPassword(final ManagedObject object, final String password) {
        return password(object.getPojo()).checkPassword(password);
    }

    @Override
    public String getEditText(final ManagedObject object) {
        return object == null ? "" : password(object).getPassword();
    }

    @Override
    public ManagedObject createValue(final String password) {
        return getObjectManager().adapt(new Password(password));
    }

    private Password password(final Object object) {
        if (object instanceof ManagedObject) {
            return (Password) ((ManagedObject) object).getPojo();
        } else {
            return (Password) object;
        }
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "PasswordValueSemanticsProvider";
    }

}
