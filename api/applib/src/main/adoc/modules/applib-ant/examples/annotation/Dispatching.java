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
 * The available policies as to whether data should be dispatched to  
 * corresponding listeners. The framework supports several kinds of data 
 * that are available for dispatching:
 * <ul>
 * <li><b>EntityAudit</b> ... dispatched via EntityAuditDispatcher and subscribed to via EntityAuditListener (SPI)</li>
 * <li><b>ChangingEntities</b> ... dispatched via ChangingEntitiesDispatcher and subscribed to via ChangingEntitiesListener (SPI)</li>
 * <li><b>Execution</b> ... dispatched via ExecutionDispatcher and subscribed to via ExecutionListener (SPI)</li>
 * <li><b>Command</b> ... dispatched via CommandDispatcher and subscribed to via CommandListener (SPI)</li>
 * </ul>
 */
// tag::refguide[]
public enum Dispatching {

    // end::refguide[]
    /**
     * Dispatching of data triggered by interaction with this object 
     * should be handled as per the default dispatching policy 
     * configured in <tt>application.properties</tt>.
     * <p>
     * If no dispatching policy is configured, then dispatching is disabled.
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Do dispatch data triggered by interaction with this object.
     */
    // tag::refguide[]
    ENABLED,

    // end::refguide[]
    /**
     * Do <b>not</b> dispatch data triggered by interaction with this object
     * (even if otherwise configured to enable dispatching).
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
