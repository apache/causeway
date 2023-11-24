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
package org.apache.isis.core.metamodel.facets.objectvalue.digits;

import org.apache.isis.applib.annotation.ValueSemantics;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * The minimum required number of digits to the left of the decimal place
 * (integer/integral part) for this number.
 *
 * <p>
 * For example:
 * <ul>
 * <li><tt>12345.789</tt> has 5 integer/integral digits</li>
 * <li><tt>0.123</tt> has 1 integer/integral digit</li>
 * </ul>
 */
public interface MinIntegerDigitsFacet
extends Facet {

    /**
     * eg. as provided by {@link ValueSemantics#minIntegerDigits()}
     */
    int getMinIntegerDigits();

}
