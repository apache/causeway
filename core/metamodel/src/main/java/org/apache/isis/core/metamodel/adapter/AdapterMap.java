/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.feature.IdentifiedHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface AdapterMap extends Injectable {


    /**
     * Gets the {@link ObjectAdapter adapter} for the specified domain object if it exists in the identity map.
     * 
     * <p>
     * Provided by the <tt>AdapterManager</tt> when used by framework.
     * 
     * @param pojo - must not be <tt>null</tt>
     * @return adapter, or <tt>null</tt> if doesn't exist.
     */
    ObjectAdapter getAdapterFor(Object pojo);

    /**
     * Either returns an existing adapter (as per {@link #getAdapterFor(Object)}), otherwise creates either a
     * transient root or a standalone {@link ObjectAdapter adapter} for the supplied domain object, depending on
     * its {@link ObjectSpecification}.
     * 
     * <p>
     * The rules for creating a {@link ResolveState#VALUE standalone} vs {@link ResolveState#TRANSIENT
     * transient} root {@link ObjectAdapter adapter} are as for
     * {@link #adapterFor(Object, ObjectAdapter, IdentifiedHolder)}.
     * 
     * <p>
     * Historical notes: previously called <tt>createAdapterForTransient</tt>, though this name wasn't quite
     * right.
     * 
     * <p>
     * Provided by the <tt>AdapterManager</tt> when used by framework.
     */
    ObjectAdapter adapterFor(Object domainObject);


    /**
     * Either returns an existing adapter (as per {@link #getAdapterFor(Object)}), otherwise creates either a
     * transient, standalone or aggregated {@link ObjectAdapter adapter} for the supplied domain object,
     * depending on its {@link ObjectSpecification} and the context arguments provided.
     * 
     * <p>
     * If no adapter is found for the provided pojo, then the rules for creating the {@link ObjectAdapter
     * adapter} are as follows:
     * <ul>
     * <li>if the pojo's {@link ObjectSpecification specification} indicates that this is an immutable
     * value, then a {@link ResolveState#VALUE} {@link ObjectAdapter adapter} is created
     * <li>otherwise, if context <tt>ownerAdapter</tt> and <tt>identified</tt> arguments have both been
     * provided and also either the {@link IdentifiedHolder} argument indicates that for this particular
     * property/collection the object is aggregated <i>or</i> that the pojo's own
     * {@link ObjectSpecification specification} indicates that the pojo is intrinsically aggregated,
     * then an {@link ObjectAdapter#isAggregated() aggregated} adapter is created. Note that the
     * {@link ResolveState} of such {@link ObjectAdapter's} is independent of its <tt>ownerAdapter</tt>, but it
     * has the same {@link ObjectAdapter#setOptimisticLock(Version) optimistic locking version}.
     * <li>otherwise, a {@link ResolveState#TRANSIENT} {@link ObjectAdapter adapter} is created.
     * </ul>
     * 
     * <p>
     * Provided by the <tt>AdapterManager</tt> when used by framework.
     * 
     * @param pojo
     *            - pojo to adapt
     * @param ownerAdapter
     *            - only used if aggregated
     * @param identifier
     *            - only used if aggregated
     */
    public ObjectAdapter adapterFor(final Object pojo, final ObjectAdapter ownerAdapter, IdentifiedHolder identifiedHolder);

}
