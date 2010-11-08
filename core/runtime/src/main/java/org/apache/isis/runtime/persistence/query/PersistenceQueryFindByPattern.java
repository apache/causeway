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

package org.apache.isis.runtime.persistence.query;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

/**
 * Corresponds to {@link QueryFindByPattern}.
 */
public class PersistenceQueryFindByPattern extends PersistenceQueryBuiltInAbstract {
    private final ObjectAdapter pattern;

    public ObjectAdapter getPattern() {
        return pattern;
    }

    public PersistenceQueryFindByPattern(final ObjectSpecification specification, final ObjectAdapter pattern) {
        super(specification);
        this.pattern = pattern;
    }

    @Override
    public boolean matches(final ObjectAdapter object) {
        final ObjectSpecification requiredSpec = pattern.getSpecification();
        ObjectSpecification objectSpec = object.getSpecification();
        return objectSpec.equals(requiredSpec) && matchesPattern(pattern, object);
    }

    private boolean matchesPattern(final ObjectAdapter pattern, final ObjectAdapter instance) {
        final ObjectAdapter object = instance;
        final ObjectSpecification nc = object.getSpecification();
        final ObjectAssociation[] fields = nc.getAssociations();

        for (int f = 0; f < fields.length; f++) {
            final ObjectAssociation fld = fields[f];

            // are ignoring internal collections - these probably should be considered
            // ignore non-persistent fields - there is no persisted field to compare against
            if (fld.isNotPersisted()) {
                continue;
            }
            if (fld.isOneToOneAssociation()) {
                if (fld.getSpecification().isCollectionOrIsAggregated()) {
                    // if pattern contains empty value then it matches anything
                    if (fld.isEmpty(pattern)) {
                        continue;
                    }

                    // find the objects
                    final ObjectAdapter reqd = fld.get(pattern);
                    final ObjectAdapter search = fld.get(object);

                    // compare the titles
                    final String r = reqd.titleString().toLowerCase();
                    final String s = search.titleString().toLowerCase();

                    // if the pattern occurs in the object
                    if (s.indexOf(r) == -1) {
                        return false;
                    }
                } else if (fld.isOneToOneAssociation()) {
                    // find the objects
                    final ObjectAdapter reqd = fld.get(pattern);

                    // if pattern contains null reference then it matches anything
                    if (reqd == null) {
                        continue;
                    }

                    final ObjectAdapter search = fld.get(object);

                    // otherwise there must be a reference, else they can never
                    // match
                    if (search == null) {
                        return false;
                    }

                    if (reqd != search) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
