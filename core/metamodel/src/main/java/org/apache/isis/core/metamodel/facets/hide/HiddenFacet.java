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

package org.apache.isis.core.metamodel.facets.hide;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.WhenAndWhereValueFacet;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;

/**
 * Hide a property, collection or action.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * member with <tt>@Hidden</tt>.
 */
public interface HiddenFacet extends WhenAndWhereValueFacet, HidingInteractionAdvisor {

    /**
     * The reason why the (feature of the) target object is currently hidden, or
     * <tt>null</tt> if visible.
     */
    public String hiddenReason(ObjectAdapter target, Where whereContext);

}
