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
package org.apache.isis.core.metamodel.facets.value.bigdecimal;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.commons.internal.base._Strings;

public class BigDecimalValueSemantics
extends AbstractValueSemanticsProvider<BigDecimal>
implements
    DefaultsProvider<BigDecimal>,
    EncoderDecoder<BigDecimal>,
    Parser<BigDecimal> {

    public static final int DEFAULT_LENGTH = 18;
    public static final int DEFAULT_SCALE = 2;

    @Override
    public BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final BigDecimal value) {
        try {
            return value.toPlainString();
        } catch (final Exception e) {
            throw new UnrecoverableException(e);
        }
    }

    @Override
    public BigDecimal fromEncodedString(final String data) {
        return new BigDecimal(data);
    }

    // -- PARSER

    @Override
    public String presentationValue(final Context context, final BigDecimal value) {
        return value==null
            ? ""
            : getNumberFormat(context)
                .format(value);
    }

    @Override
    public String parseableTextRepresentation(final Context context, final BigDecimal value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public BigDecimal parseTextRepresentation(final Context context, final String text) {
        final var input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        final var format = getNumberFormat(context);
        format.setParseBigDecimal(true);
        final var position = new ParsePosition(0);

        try {
            final var number = (BigDecimal)format.parse(input, position);
            if (position.getErrorIndex() != -1) {
                throw new ParseException("could not parse input='" + input + "'", position.getErrorIndex());
            } else if (position.getIndex() < input.length()) {
                throw new ParseException("input='" + input + "' wasnt processed completely", position.getIndex());
            }
            return number;
        } catch (final NumberFormatException | ParseException e) {
            throw new TextEntryParseException("Not a decimal " + input, e);
        }
    }

    @Override
    public int typicalLength() {
        return 10;
    }

}
