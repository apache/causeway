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

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * A specification that has an underlying type.
 * 
 * <p>
 * For a property or action parameter, is the type.  For a collection is the element type.
 * For an action it is always <tt>null</tt>.
 * 
 * <p>
 * TODO: replace ObjectAction#getReturnType() with #getSpecification() - ie so doesn't return null.
 */
public interface TypedSpecification extends Specification {

    /**
     * The specification of the underlying type.
     * 
     * <p>
     * For example:
     * <ul>
     * <li>for a {@link OneToOneAssociation property}, will return the {@link ObjectSpecification} 
     *     of the type that the accessor returns.
     * <li>for a {@link OneToManyAssociation collection} it will be the type of element
     *     the collection holds (not the type of collection).
     * <li>for a {@link ObjectAction action}, will always return <tt>null</tt>.  See instead {@link ObjectAction#getReturnType()} and {@link ObjectAction#getParameterTypes()}.
     * <li>for a {@link ObjectActionParameter action}, will return the type of the parameter}.
     * </ul>
     */
    ObjectSpecification getSpecification();
    


}
