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

package org.apache.isis.viewer.dnd.view.field;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

final class ObjectField {
    private final ObjectAssociation field;
    private final ObjectAdapter parent;

    ObjectField(final ObjectAdapter parent, final ObjectAssociation field) {
        this.parent = parent;
        this.field = field;
    }

    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("field", getObjectAssociation());
        debug.appendln("name", getName());
        debug.appendln("specification", getSpecification());
        debug.appendln("parent", parent);
    }

    public String getDescription() {
        return field.getDescription();
    }

    public String getHelp() {
        return field.getHelp();
    }

    public ObjectAssociation getObjectAssociation() {
        return field;
    }

    public final String getName() {
        return field.getName();
    }

    public ObjectAdapter getParent() {
        return parent;
    }

    public ObjectSpecification getSpecification() {
        return field.getSpecification();
    }
}
