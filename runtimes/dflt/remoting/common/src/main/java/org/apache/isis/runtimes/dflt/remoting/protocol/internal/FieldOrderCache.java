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

package org.apache.isis.runtimes.dflt.remoting.protocol.internal;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

/**
 * Caches a sorted version of the fields for a specified ObjectSpecification. This is used to counteract any differences
 * in field ordering that the specification might have across different tiers
 * 
 * <p>
 * TODO: shouldn't this responsibility simply move onto {@link ObjectSpecification} ?
 */
public class FieldOrderCache {

    private final Hashtable cache = new Hashtable();

    public ObjectAssociation[] getFields(final ObjectSpecification specification) {
        ObjectAssociation[] fields = (ObjectAssociation[]) cache.get(specification);
        if (fields == null) {
            fields = loadFields(specification);
            cache.put(specification, fields);
        }
        return fields;
    }

    private ObjectAssociation[] loadFields(final ObjectSpecification specification) {
        final List<ObjectAssociation> originalFields = specification.getAssociations();
        final Vector sorted = new Vector(originalFields.size());
        outer: for (int i = 0; i < originalFields.size(); i++) {
            final String fieldId = originalFields.get(i).getId();

            for (int j = 0; j < sorted.size(); j++) {
                final ObjectAssociation sortedElement = (ObjectAssociation) sorted.elementAt(j);
                final String sortedFieldId = sortedElement.getId();
                if (sortedFieldId.compareTo(fieldId) > 0) {
                    sorted.insertElementAt(originalFields.get(i), j);
                    continue outer;
                }
            }
            sorted.addElement(originalFields.get(i));
        }

        final ObjectAssociation[] fields = new ObjectAssociation[originalFields.size()];
        sorted.copyInto(fields);

        return fields;
    }

}
