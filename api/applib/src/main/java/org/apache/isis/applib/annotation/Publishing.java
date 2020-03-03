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

/**
 * The available policies for publishing changes to the properties of the object.
 */
// tag::refguide[]
public enum Publishing {

    // end::refguide[]
    /**
     * The publishing of the object should be as per the default publishing policy configured in <tt>application.properties</tt>.
     *
     * <p>
     *     If no publishing policy is configured, then the publishing is disabled.
     * </p>
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Publish changes to this object.
     */
    // tag::refguide[]
    ENABLED,

    // end::refguide[]
    /**
     * Do not publish changes to this object (even if otherwise configured to enable publishing).
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
