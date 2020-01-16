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

package org.apache.isis.core.metamodel.spec;

import java.util.stream.Stream;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.ToString;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A list returned from an action, ie not associated or owned by any entity.
 */
@RequiredArgsConstructor(staticName = "of")
public class FreeStandingList implements Container {

    @Getter(onMethod = @__(@Override))
    @NonNull private final ObjectSpecification elementSpecification;
    @NonNull private final Can<ManagedObject> elements;
    
    public Stream<ManagedObject> stream() {
        return elements.stream();
    }

    public int size() {
        return elements.size();
    }

    @Override
    public String titleString() {
        switch(elements.getCardinality()) {
        case ONE:
            return getElementSpecification().getSingularName();
        default:
            return getElementSpecification().getPluralName() + ", " + size();
        }
    }

    @Override
    public String toString() {
        final ToString s = new ToString(this);
        s.append("elements", getElementSpecification().getFullIdentifier());

        // title
        String title;
        try {
            title = "'" + this.titleString() + "'";
        } catch (final NullPointerException e) {
            title = "none";
        }
        s.append("title", title);

        s.append("vector", elements);

        return s.toString();
    }



}
