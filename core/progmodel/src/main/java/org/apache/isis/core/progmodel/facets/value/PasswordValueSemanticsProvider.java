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


package org.apache.isis.core.progmodel.facets.value;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


public class PasswordValueSemanticsProvider extends ValueSemanticsProviderAbstract implements PasswordValueFacet {

    public static Class<? extends Facet> type() {
        return PasswordValueFacet.class;
    }

    private static final int TYPICAL_LENGTH = 12;
    private static final boolean IMMUTABLE = true;
    private static final boolean EQUAL_BY_CONTENT = true;
    private static final Object DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public PasswordValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public PasswordValueSemanticsProvider(
    		final FacetHolder holder,
    		final IsisConfiguration configuration,
    		final SpecificationLoader specificationLoader,
    		final RuntimeContext runtimeContext) {
        super(type(), holder, Password.class, TYPICAL_LENGTH, IMMUTABLE, EQUAL_BY_CONTENT, DEFAULT_VALUE, configuration, specificationLoader, runtimeContext);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Object doParse(final Object original, final String text) {
        return new Password(text);
    }

    @Override
    public String titleString(final Object object) {
        return object == null ? "" : password(object).toString();
    }

    public String titleStringWithMask(final Object object, final String usingMask) {
        return titleString(object);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        return password(object).getPassword();
    }

    @Override
    protected Object doRestore(final String data) {
        return new Password(data);
    }

    // //////////////////////////////////////////////////////////////////
    // PasswordValueFacet
    // //////////////////////////////////////////////////////////////////

    public boolean checkPassword(final ObjectAdapter object, final String password) {
        return password(object.getObject()).checkPassword(password);
    }

    public String getEditText(final ObjectAdapter object) {
        return object == null ? "" : password(object).getPassword();
    }

    public ObjectAdapter createValue(final String password) {
        return getRuntimeContext().adapterFor(new Password(password));
    }

    private Password password(final Object object) {
        return (Password) object;
    }


    // /////// toString ///////

    @Override
    public String toString() {
        return "PasswordValueSemanticsProvider";
    }


}

