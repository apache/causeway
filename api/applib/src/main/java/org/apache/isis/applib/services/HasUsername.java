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
package org.apache.isis.applib.services;

/**
 * Mix-in interface for objects (usually created by service implementations) that are be persistable,
 * and so can be associated with a username, usually of the user that has performed some operation.
 *
 * <p>
 * Other services can then use this username as a means to contributed actions/collections to render such additional
 * information relating to the activities of the user.
 */
// tag::refguide[]
public interface HasUsername {

    /**
     * The user that created this object.
     * @return
     */
    String getUsername();

}
// end::refguide[]
