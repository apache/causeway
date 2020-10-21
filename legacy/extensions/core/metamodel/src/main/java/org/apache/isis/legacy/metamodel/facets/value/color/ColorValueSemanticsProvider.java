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

package org.apache.isis.legacy.metamodel.facets.value.color;

import java.text.DecimalFormat;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.legacy.applib.value.Color;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class ColorValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<Color> 
implements ColorValueFacet {

    public static Class<? extends Facet> type() {
        return ColorValueFacet.class;
    }

    private static final Color DEFAULT_VALUE = Color.BLACK;
    private static final int TYPICAL_LENGTH = 4;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public ColorValueSemanticsProvider() {
        this(null);
    }

    public ColorValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Color.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Color doParse(final Object context, final String text) {
        try {
            if (text.startsWith("0x")) {
                return new Color(Integer.parseInt(text.substring(2), 16));
            } else if (text.startsWith("#")) {
                return new Color(Integer.parseInt(text.substring(1), 16));
            } else {
                return new Color(Integer.parseInt(text));
            }
        } catch (final NumberFormatException e) {
            throw new TextEntryParseException("Not a number " + text, e);
        }
    }

    @Override
    public String titleString(final Object object) {
        final Color color = (Color) object;
        return color.title();
    }

    @Override
    public String titleStringWithMask(final Object object, final String usingMask) {
        final Color color = (Color) object;
        return titleString(new DecimalFormat(usingMask), color.intValue());
    }

    // //////////////////////////////////////////////////////////////////
    // Encode, Decode
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Color color) {
        return Integer.toHexString(color.intValue());
    }

    @Override
    protected Color doRestore(final String data) {
        return new Color(Integer.parseInt(data, 16));
    }

    // //////////////////////////////////////////////////////////////////
    // ColorValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public int colorValue(final ManagedObject object) {
        if (object == null) {
            return 0;
        }
        final Color color = (Color) object.getPojo();
        return color.intValue();
    }

    @Override
    public ManagedObject createValue(final ManagedObject object, final int colorAsInt) {
        final Color color = new Color(colorAsInt);
        return getObjectManager().adapt(color);
    }

    @Override
    public String toString() {
        return "ColorValueSemanticsProvider";
    }

}
