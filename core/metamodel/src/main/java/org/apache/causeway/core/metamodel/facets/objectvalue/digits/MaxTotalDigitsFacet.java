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
package org.apache.causeway.core.metamodel.facets.objectvalue.digits;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Digits;

import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.core.metamodel.facetapi.Facet;

/**
 * Maximum length of digits for this decimal.
 *
 * <p>
 * For example:
 * <ul>
 * <li><tt>12345.789</tt> has a total of 8 digits</li>
 * <li><tt>12345</tt> has a total of 5 digits</li>
 * <li><tt>12345.0</tt> has a total of 6 digits</li>
 * </ul>
 * </p>
 *
 * <p>
 * In JPA's {@link Column}, this corresponds to {@link Column#precision()}.
 * </p>
 *
 * <p>
 * For {@link Digits}, corresponds to sum of {@link Digits#integer()} and {@link Digits#fraction()}.
 * </p>
 */
public interface MaxTotalDigitsFacet
extends Facet {

    /**
     * eg. as provided by {@link Digits#fraction()}
     * and {@link ValueSemantics#maxTotalDigits()}
     */
    int getMaxTotalDigits();

}
