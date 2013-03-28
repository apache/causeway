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

import org.apache.isis.viewer.html.task.Task;

public class ForwardRequest implements Request {

    public static Request editObject(final String objectId) {
        return new ForwardRequest(EDIT_COMMAND, objectId);
    }

    public static Request listCollection(final String collectionId) {
        return new ForwardRequest(COLLECTION_COMMAND, collectionId);
    }

    public static ForwardRequest viewObject(final String objectId) {
        return new ForwardRequest(OBJECT_COMMAND, objectId);
    }

    public static Request viewObject(final String objectId, final String collectionField) {
        return new ForwardRequest(OBJECT_COMMAND, objectId, collectionField);
    }

    public static Request viewService(final String objectId) {
        return new ForwardRequest(SERVICE_COMMAND, objectId);
    }

    public static Request task(final Task task) {
        final ForwardRequest forwardRequest = new ForwardRequest(TASK_COMMAND, null);
        forwardRequest.taskId = task.getId();
        return forwardRequest;
    }

    public static Request taskComplete() {
        final ForwardRequest forwardRequest = new ForwardRequest(TASK_COMMAND, null);
        forwardRequest.submitName = "Ok";
        return forwardRequest;
    }

    private final String actionName;
    private Request forwardedRequest;
    private final String objectId;
    private final String fieldName;
    private String submitName;
    private String taskId;

    private ForwardRequest(final String actionName, final String id) {
        this(actionName, id, null);
    }

    private ForwardRequest(final String actionName, final String objectId, final String fieldName) {
        this.actionName = actionName;
        this.objectId = objectId;
        this.fieldName = fieldName;
    }

    @Override
    public void forward(final Request forwardedRequest) {
        this.forwardedRequest = forwardedRequest;
    }

    @Override
    public String getActionId() {
        return null;
    }

    @Override
    public String getElementId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getRequestType() {
        return actionName;
    }

    @Override
    public String getButtonName() {
        return submitName;
    }

    @Override
    public String getProperty() {
        return fieldName;
    }

    @Override
    public String getFieldEntry(final int i) {
        return null;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public Request getForward() {
        return forwardedRequest;
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public String toString() {
        return "ForwardRequest " + actionName + " " + forwardedRequest;
    }

    public static Request logon() {
        return new ForwardRequest("logon", null);
    }
}
