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
 * The available policies for editing properties and collections.
 */
// tag::refguide[]
public enum Editing {

    // end::refguide[]
    /**
     * The editing of the object should be as per the default editing policy configured in <tt>application.properties</tt>.
     *
     * <p>
     *     If no editing policy is configured, then the editing is enabled.
     * </p>
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Audit changes to this object.
     */
    // tag::refguide[]
    ENABLED,

    // end::refguide[]
    /**
     * Do not allow the properties to be edited, or the collections to be added to/removed from.
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
