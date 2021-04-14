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

package org.apache.isis.core.metamodel.facets.object.parseable;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * Indicates that this class can parse an entry string.
 */
public interface ParseableFacet extends Facet {

    /**
     * Parses a text entry made by a user and sets the domain object's value.
     *
     * <p>
     * Equivalent to <tt>Parser#parseTextEntry(Object, String)</tt>, though may
     * be implemented through some other mechanism.
     */
    ManagedObject parseTextEntry(
            final ManagedObject original,
            final String text,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * A title for the object that is valid but which may be easier to edit than
     * the title provided by a {@link TitleFacet}.
     *
     * <p>
     * The idea here is that the viewer can display a parseable title for an
     * existing object when, for example, the user initially clicks in the
     * field. So, a date might be rendered via a {@link TitleFacet} as
     * <tt>May 2, 2007</tt>, but its parseable form might be <tt>20070502</tt>.
     */
    String parseableTitle(ManagedObject obj);
}
