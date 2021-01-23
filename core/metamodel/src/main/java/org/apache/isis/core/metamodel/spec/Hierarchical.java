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

import java.util.Collection;

public interface Hierarchical {

    /**
     * Returns true if the <tt>subclasses()</tt> method will return an array of
     * one or more elements (ie, not an empty array).
     */
    boolean hasSubclasses();

    /**
     * Get the set of specifications for all the interfaces that the class
     * represented by this specification implements.
     */
    Collection<ObjectSpecification> interfaces();

    /**
     * Determines if this specification represents the same specification, or a
     * subclass, of the specified specification.
     *
     * <p>
     * <tt>subSpec.isOfType(superSpec)</tt> is equivalent to
     * {@link Class#isAssignableFrom(Class) Java's}
     * <tt>superType.isAssignableFrom(subType)</tt>.
     */
    boolean isOfType(ObjectSpecification specification);

    public static enum Depth {
        DIRECT,
        TRANSITIVE
    }

    /**
     * Get the set of specifications for the subclasses of the class
     * represented by this specification
     */
    Collection<ObjectSpecification> subclasses(Depth depth);

    /**
     * Get the specification for this specification's class's superclass.
     */
    ObjectSpecification superclass();
    
    default boolean isTypeHierarchyRoot() {
        return superclass()==null;
    }

}
