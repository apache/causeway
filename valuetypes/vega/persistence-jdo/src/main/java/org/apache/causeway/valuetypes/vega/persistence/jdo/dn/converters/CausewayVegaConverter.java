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
package org.apache.causeway.valuetypes.vega.persistence.jdo.dn.converters;

import org.datanucleus.store.types.converters.TypeConverter;

import org.apache.causeway.valuetypes.vega.applib.stringify.VegaStringifier;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;

/**
 * @since 2.0 {@index}
 */
public class CausewayVegaConverter implements TypeConverter<Vega, String>{

    private static final long serialVersionUID = 1L;

    @Override
    public String toDatastoreType(final Vega memberValue) {
        return VegaStringifier.enstring(memberValue);
    }

    @Override
    public Vega toMemberType(final String datastoreValue) {
        return VegaStringifier.destring(datastoreValue);
    }

}
