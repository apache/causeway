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

package org.apache.isis.core.runtime.persistence.query;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * Corresponds to {@link QueryFindByPattern}.
 */
public class PersistenceQueryFindByPattern extends PersistenceQueryBuiltInAbstract {
    private final ObjectAdapter pattern;

    public ObjectAdapter getPattern() {
        return pattern;
    }

    public PersistenceQueryFindByPattern(
            final ObjectSpecification specification,
            final ObjectAdapter pattern,
            final SpecificationLoader specificationLoader,
            final long... range) {
        super(specification, specificationLoader, range);
        this.pattern = pattern;
    }

    @Override
    public boolean matches(final ObjectAdapter object) {
        final ObjectSpecification requiredSpec = pattern.getSpecification();
        final ObjectSpecification objectSpec = object.getSpecification();
        return objectSpec.equals(requiredSpec) && matchesPattern(pattern, object);
    }

    private boolean matchesPattern(final ObjectAdapter pattern, final ObjectAdapter instance) {
        final ObjectAdapter object = instance;
        final ObjectSpecification nc = object.getSpecification();
        final List<ObjectAssociation> fields = nc.getAssociations(Contributed.EXCLUDED);

        for (int f = 0; f < fields.size(); f++) {
            final ObjectAssociation fld = fields.get(f);

            // are ignoring internal collections - these probably should be
            // considered
            
            // ignore non-persistent fields - there is no persisted field to
            // compare against
            if (fld.isNotPersisted()) {
                continue;
            }
            if (!fld.isOneToOneAssociation()) {
                continue;
            } 

            // if pattern contains empty value then it matches anything
            if (fld.isEmpty(pattern, InteractionInitiatedBy.FRAMEWORK)) {
                continue;
            }

            // find the object to match against, if any
            final ObjectAdapter reqd = fld.get(pattern, InteractionInitiatedBy.FRAMEWORK);
            if (reqd == null) {
                continue;
            }

            // find the object; it's a bust if nothing
            final ObjectAdapter search = fld.get(object, InteractionInitiatedBy.FRAMEWORK);
            if (search == null) {
                return false;
            }

            if (fld.getSpecification().isValue()) {
                // compare values directly
                if (!reqd.getObject().equals(search.getObject())) {
                    return false;
                }
                
            } else {

                // compare the titles
                final String r = reqd.titleString(null).toLowerCase();
                final String s = search.titleString(null).toLowerCase();

                // if the pattern does not occur in the object, then it's a bust
                if (s.indexOf(r) == -1) {
                    return false;
                }
            }
        }

        return matchesRange(true);
    }

}
