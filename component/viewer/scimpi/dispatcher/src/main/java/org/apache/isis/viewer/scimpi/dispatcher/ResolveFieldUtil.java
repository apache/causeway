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
package org.apache.isis.viewer.scimpi.dispatcher;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public final class ResolveFieldUtil {

    private ResolveFieldUtil(){}

    /**
     * Walking the graph.
     *
     * <p>
     *     This combines the implementations of both the DN Objectstore
     *     and also the in-memory objectstore.
     * </p>
     */
    public static void resolveField(final ObjectAdapter object, final ObjectAssociation association) {


        // DN impl.
        {
            final ObjectAdapter referencedCollectionAdapter = association.get(object);

            // this code originally brought in from the JPA impl, but seems reasonable.
            if (association.isOneToManyAssociation()) {
                ensureThatState(referencedCollectionAdapter, is(notNullValue()));

                final Object referencedCollection = referencedCollectionAdapter.getObject();
                ensureThatState(referencedCollection, is(notNullValue()));

                // if a proxy collection, then force it to initialize.  just 'touching' the object is sufficient.
                // REVIEW: I wonder if this is actually needed; does JDO use proxy collections?
                referencedCollection.hashCode();
            }

            // the JPA impl used to also call its lifecycle listener on the referenced collection object, eg List,
            // itself.  I don't think this makes sense to do for JDO (the collection is not a PersistenceCapable).
        }

        // In-memory objectstore impl
        {
            final ObjectAdapter referenceAdapter = association.get(object);
            referenceAdapter.markAsResolvedIfPossible();
        }

    }

}
