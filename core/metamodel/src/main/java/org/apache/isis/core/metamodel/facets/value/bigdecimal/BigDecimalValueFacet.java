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

import org.apache.isis.core.metamodel.facetapi.Facet;

public interface BigDecimalValueFacet extends Facet {

    /**
     * Maximum length of digits for this decimal (in other words, its precision).
     *
     * <p>
     * For example:
     * <ul>
     * <li><tt>12345.789</tt> has a length of 8 (and a {@link #getScale() scale} of 3)</li>
     * <li><tt>12345</tt> has a length of 5 (and {@link #getScale() scale} of 0)</li>
     * <li><tt>12345.0</tt> has a length of 6 (and {@link #getScale() scale} of 1)</li>
     * </ul>
     */
    int getPrecision();

    /**
     * The number of digits to the right of the decimal place (fractional part) for this decimal.
     *
     * <p>
     * For example:
     * <ul>
     * <li><tt>12345.789</tt> has scale of 3 (and a {@link #getPrecision() precision} of 8)</li>
     * <li><tt>12345</tt> has a scale of 0 (and a {@link #getPrecision() precision} of 5)</li>
     * <li><tt>12345.0</tt> has a scale of 1 (and a {@link #getPrecision() precision} of 6)</li>
     * </ul>
     */
    int getScale();

}
