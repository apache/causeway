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
package org.apache.isis.applib.services.wrapper.control;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.Bookmark;

import lombok.val;

/**
 * For framework internal use.
 */
@Component
public class AsyncControlService {

    public <R> AsyncControl<R> init(AsyncControl<R> asyncControl, Method method, Bookmark bookmark) {
        asyncControl.setMethod(method);
        asyncControl.setBookmark(bookmark);
        return asyncControl;
    }

    public <R> AsyncControl<R> update(AsyncControl<R> asyncControl, Future<R> future) {
        asyncControl.setFuture(future);
        return asyncControl;
    }

    public <R> boolean shouldCheckRules(AsyncControl<R> asyncControl) {
        val executionModes = asyncControl.getExecutionModes();
        val skipRules = executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION);
        return !skipRules;
    }
}
