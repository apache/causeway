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

package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalTime;

import java.util.Locale;

public class ConverterForJodaLocalTime implements IConverter<LocalTime> {
    
    private static final long serialVersionUID = 1L;
    
    public LocalTime convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return LocalTime.parse(value);
        } catch (Exception e) {
            throw new ConversionException("'" + value + "' is not a valid time.");
        }
    }
    
    public String convertToString(LocalTime value, Locale locale) {
        return value.toString();
    }
}
