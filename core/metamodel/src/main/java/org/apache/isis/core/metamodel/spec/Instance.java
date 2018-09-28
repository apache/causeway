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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * Represents an instance of some element of the meta-model.
 *
 * <p>
 * Currently the only sub-interface is {@link ObjectAdapter}. However, the
 * intention is for associations, actions and action parameters to also inherit
 * from this interface.
 */
public interface Instance {

    /**
     * Returns the specification that details the structure of this instance.
     */
    Specification getSpecification();

}
