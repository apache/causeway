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
package org.apache.isis.core.metamodel.facets.objectvalue.temporalformat;

import org.apache.isis.applib.annotations.TimePrecision;
import org.apache.isis.applib.annotations.ValueSemantics;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * If associated with a temporal time value, the time of day precision,
 * used for editing a time field in the UI.
 *
 * @see ValueSemantics#timePrecision()
 */
public interface TimeFormatPrecisionFacet
extends Facet {

    /**
     * As provided by {@link ValueSemantics#timePrecision()}.
     */
    TimePrecision getTimePrecision();

}
