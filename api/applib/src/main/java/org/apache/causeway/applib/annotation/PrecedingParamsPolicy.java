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
package org.apache.causeway.applib.annotation;

/**
 * The policies for calculating the defaults of parameters if it has preceding parameters (isn't the first parameter,
 * in other words).
 *
 * @since 2.x {@index}
 */
public enum PrecedingParamsPolicy {

    /**
     * The policy to use should be as per the preceding defaults policy configured in <tt>application.properties</tt>.
     * <p>
     * If no defaults policy is configured, then default to the {@link PrecedingParamsPolicy#RESET RESET} policy.
     */
    AS_CONFIGURED,

    /**
     * If an end-user has changed this parameter's value, then do not overwrite the value when an earlier parameter
     * changes.
     * <p>
     * <b>WARNING</b>: If the parameter is constrained by dependent choices, then these will <i>not</i> be
     * re-evaluated.  The validation for the action should make sure that the parameter argument is validated
     * correctly.
     */
    PRESERVE_CHANGES,

    /**
     * If a previous parameter is changed, then reset this parameter to its default,
     * <i>even if</i> the end-user has changed the value of this parameter previously.
     */
    RESET,

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    NOT_SPECIFIED

}
