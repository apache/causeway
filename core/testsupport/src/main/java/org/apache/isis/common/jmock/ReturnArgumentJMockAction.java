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


package org.apache.isis.common.jmock;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public final class ReturnArgumentJMockAction implements Action {
    private final int i;

    public ReturnArgumentJMockAction(int i) {
        this.i = i;
    }

    public void describeTo(Description description) {
        description.appendText("parameter #" + i + " ");
    }

    public Object invoke(Invocation invocation) throws Throwable {
        return invocation.getParameter(i);
    }

    /**
     * Factory
     */
    public static Action returnArgument(final int i) {
    	return new ReturnArgumentJMockAction(i);
    }

}

