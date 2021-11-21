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
package org.apache.isis.core.metamodel.facets.object.encodeable;

import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Indicates that this class can be encoded/decoded as a string.
 */
public interface EncodableFacet extends Facet {

    /**
     * Equivalent to {@link EncoderDecoder#fromEncodedString(String)}, though
     * may be implemented through some other equivalent mechanism.
     */
    ManagedObject fromEncodedString(String encodedData);

    /**
     * Equivalent to {@link EncoderDecoder#toEncodedString(Object)}, though may
     * be implemented through some other equivalent mechanism.
     */
    String toEncodedString(ManagedObject object);
}
