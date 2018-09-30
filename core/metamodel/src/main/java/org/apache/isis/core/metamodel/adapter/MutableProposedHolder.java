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

package org.apache.isis.core.metamodel.adapter;

import javax.enterprise.inject.Instance;

import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.spec.Specification;

/**
 * Mix-in for {@link ManagedObject} implementations, where can hold a proposed new
 * value different from the underlying value.
 *
 * <p>
 * TODO: same concept as {@link ProposedHolder}, so should try to combine.
 */
public interface MutableProposedHolder {

    /**
     * The proposed (pending) value, if applicable.
     *
     * <p>
     * See {@link Specification#createInstanceProposalEvent(Instance)} for an
     * indication as to whether the state will be populated, and what its type
     * will be.
     *
     * @return
     */
    Object getProposed();

    /**
     * Sets the proposed (pending) value, if applicable.
     *
     * <p>
     * <p>
     * See {@link Specification#createInstanceProposalEvent(Instance)} for an
     * indication as to whether the proposed state should be populated, and what
     * its type will be.
     *
     * @return
     */
    public void setProposed(Object value);

}
