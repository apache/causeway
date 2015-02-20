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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigInteger;
import java.util.Locale;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.AbstractIntegerConverter;
import org.apache.wicket.util.string.Strings;

public final class BigIntegerConverter extends AbstractIntegerConverter<BigInteger> {
    private static final long serialVersionUID = 1L;

    /**
     * The singleton instance for a big integer converter
     * (cf the Wicket subclasses)
     */
    public static final IConverter<BigInteger> INSTANCE = new BigIntegerConverter();

    @Override
    public BigInteger convertToObject(String value, Locale locale) throws ConversionException {
        if (Strings.isEmpty(value))
        {
            return null;
        }

        final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);

        if (number instanceof BigInteger)
        {
            return (BigInteger)number;
        }
        else if (number instanceof Long)
        {
            return BigInteger.valueOf(number.longValue());
        }
        else if (number instanceof Integer)
        {
            return BigInteger.valueOf(number.intValue());
        }
        else
        {
            return new BigInteger(value);
        }
    }

    @Override
    protected Class<BigInteger> getTargetType() {
        return BigInteger.class;
    }
}
