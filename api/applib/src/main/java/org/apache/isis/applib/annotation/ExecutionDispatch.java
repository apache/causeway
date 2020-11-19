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
package org.apache.isis.applib.annotation;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.ExecutionListener;

/**
 * The available policies as to whether {@link Interaction.Execution}s
 * (triggered either by action invocations or property edits), should
 * be dispatched to {@link ExecutionListener}s.
 */
// tag::refguide[]
public enum ExecutionDispatch {

    // end::refguide[]
    /**
     * Dispatching of 
     * {@link Interaction.Execution}s (triggered either by action invocations or property edits) 
     * for this object should be as per the default dispatching policy 
     * configured in <tt>application.properties</tt>.
     *
     * <p>
     *     If no dispatching policy is configured, then dispatching is disabled.
     * </p>
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Do dispatch {@link Interaction.Execution}s (triggered either by action invocations or property edits) 
     * for this object.
     */
    // tag::refguide[]
    ENABLED,

    // end::refguide[]
    /**
     * Do <b>not</b> dispatch {@link Interaction.Execution}s 
     * (triggered either by action invocations or property edits)
     * for this object (even if otherwise configured to enable dispatching).
     */
    // tag::refguide[]
    DISABLED,

    // end::refguide[]
    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    // tag::refguide[]
    NOT_SPECIFIED

}
// end::refguide[]
