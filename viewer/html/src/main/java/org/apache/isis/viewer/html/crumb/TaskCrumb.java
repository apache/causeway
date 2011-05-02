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

package org.apache.isis.viewer.html.crumb;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.viewer.html.request.ForwardRequest;
import org.apache.isis.viewer.html.request.Request;
import org.apache.isis.viewer.html.task.Task;

public class TaskCrumb implements Crumb {
    private final Task task;

    public TaskCrumb(final Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public void debug(final DebugBuilder string) {
        string.appendln("Task Crumb");
        string.appendln("task", task);

        task.debug(string);
    }

    @Override
    public String title() {
        return task.getName();
    }

    @Override
    public String toString() {
        return new ToString(this).append(title()).toString();
    }

    @Override
    public Request changeContext() {
        return ForwardRequest.task(task);
    }

}
