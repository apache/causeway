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

package org.apache.isis.applib.services.publish;

import org.apache.isis.applib.annotation.Programmatic;




/**
 * The payload of an event, representing the information to be published 
 * in some canonicalized form.
 * 
 * <p>
 * This should be prepared in a way that can be processed by the {@link EventSerializer}.  For example:
 * <ul>
 * <li>The {@link EventSerializer.Simple simple event serializer} simply invokes
 * {@link Object#toString() toString()} on the payload.  Use the {@link Default simple} implementation
 * which simply wraps a string.
 * </li>
 * <li>The <tt>RestfulObjectsSpecEventSerializer</tt> event serializer expects a pojo domain object
 * and serializes it out according to the <a href="http://restfulobject.org">Restful Objects spec</a>.
 * Use the {@link EventPayloadForActionInvocation object payload} implementation if you simply wish to reference some
 * persistent domain object.
 * </li>
 * </ul>
 * 
 * @see EventMetadata
 */
public interface EventPayload {

    /**
     * Injected by Isis runtime immediately after instantiation.
     */
    @Programmatic
    void withStringifier(ObjectStringifier stringifier);

}
