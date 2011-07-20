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
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.util.ClassLoaders;

/**
 * A {@link Serializable} wrapper for {@link ObjectSpecification}
 */
public class SpecMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private transient ObjectSpecification specification;

    /**
     * Factory method.
     * 
     * @param className
     * @return may return null if className is null
     */
    public static SpecMemento representing(final String className) {
        if (className == null) {
            return null;
        }
        return new SpecMemento(ClassLoaders.forName(className));
    }

    public static SpecMemento representing(final Class<?> type) {
        if (type == null) {
            return null;
        }
        return new SpecMemento(type);
    }

    public static SpecMemento representing(final ObjectSpecification specification) {
        if (specification == null) {
            return null;
        }
        return new SpecMemento(specification);
    }

    public SpecMemento(final ObjectSpecification specification) {
        this(ClassLoaders.forName(specification));
        this.specification = specification;
    }

    private SpecMemento(final Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    /**
     * Lazy loaded from {@link #getType()}.
     */
    public ObjectSpecification getSpecification() {
        if (specification == null) {
            specification = IsisContext.getSpecificationLoader().loadSpecification(type);
        }
        return specification;
    }

}
