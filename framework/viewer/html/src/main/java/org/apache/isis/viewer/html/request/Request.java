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

package org.apache.isis.viewer.html.request;

/**
 * The Request object represents all the information collected from the user
 * when requesting the server do something.
 */
public interface Request {
    public static final String EDIT_COMMAND = "edit";
    public static final String COLLECTION_COMMAND = "collection";
    public static final String FIELD_COLLECTION_COMMAND = "fieldCollection";
    public static final String OBJECT_COMMAND = "object";
    public static final String SERVICE_COMMAND = "serviceOption";
    public static final String TASK_COMMAND = "task";
    public static final String LOGON_COMMAND = "task";

    void forward(Request object);

    String getActionId();

    /**
     * Name of a property within an object.
     */
    String getProperty();

    /**
     * The element within a collection.
     */
    String getElementId();

    /**
     * The users entry into a field on the form.
     */
    String getFieldEntry(int i);

    Request getForward();

    String getObjectId();

    /**
     * Name of the request. See the constants defined in this class.
     */
    String getRequestType();

    String getTaskId();

    /**
     * Name of the button pressed on the form.
     */
    String getButtonName();

    String getName();

}
