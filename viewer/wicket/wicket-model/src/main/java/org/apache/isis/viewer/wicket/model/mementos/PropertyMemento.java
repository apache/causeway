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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class PropertyMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private static ObjectSpecification owningSpecFor(final OneToOneAssociation association) {
        return IsisContext.getSpecificationLoader().loadSpecification(association.getIdentifier().toClassIdentityString());
    }

    private final SpecMemento owningType;
    private final String identifier;

    /**
     * Lazily loaded as required.
     */
    private SpecMemento type;

    private transient OneToOneAssociation property;

    public PropertyMemento(final SpecMemento owningType, final String name) {
        this(owningType, name, null);
    }

    public PropertyMemento(final SpecMemento owningType, final String name, final SpecMemento type) {
        this.owningType = owningType;
        this.identifier = name;
        this.type = type;
    }

    public PropertyMemento(final OneToOneAssociation property) {
        this(new SpecMemento(owningSpecFor(property)), property.getIdentifier().toNameIdentityString(), new SpecMemento(property.getSpecification()));
        this.property = property;
    }

    public SpecMemento getOwningType() {
        return owningType;
    }

    public SpecMemento getType() {
        if (type == null) {
            // lazy load if need be
            type = new SpecMemento(getProperty().getSpecification());
        }
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public OneToOneAssociation getProperty() {
        if (property == null) {
            property = (OneToOneAssociation) owningType.getSpecification().getAssociation(identifier);
        }
        return property;
    }

    /**
     * Value semantics so can use as a key in {@link EntityModel} hash.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    /**
     * Value semantics so can use as a key in {@link EntityModel} hash.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyMemento other = (PropertyMemento) obj;
        if (identifier == null) {
            if (other.identifier != null) {
                return false;
            }
        } else if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

}
