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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;

public interface DateConverter<T> extends IConverter<T> {
    Class<T> getConvertableClass();

    /**
     * The date pattern, without a time component.
     *
     * <p>
     * For example, <tt>dd-MM-yyyy</tt> for <tt>13-05-2013</tt> for the 13 May 2013.
     */
    String getDatePattern(Locale locale);

    /**
     * The date pattern, with a time component.
     *
     * <p>
     * For example, <tt>dd-MM-yyyy HH:mm</tt> for <tt>13-05-2013 09:15</tt> for the 13 May 2013 at 9:15am.
     */
    String getDateTimePattern(Locale locale);

}