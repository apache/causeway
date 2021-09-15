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
package org.apache.isis.core.metamodel.facets.objectvalue.maxlen;

import javax.validation.constraints.Digits;

import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * Maximum length of digits for this decimal.
 *
 * <p>
 * For example:
 * <ul>
 * <li><tt>12345.789</tt> has a total of 8</li>
 * <li><tt>12345</tt> has a total of 5</li>
 * <li><tt>12345.0</tt> has a total of 6</li>
 * </ul>
 */
public interface MaxTotalDigitsFacet
extends Facet {

    /**
     * eg. as provided by {@link Digits#fraction()}
     */
    int maxTotalDigits();

}
