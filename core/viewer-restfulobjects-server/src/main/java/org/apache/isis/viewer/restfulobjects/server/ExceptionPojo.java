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
package org.apache.isis.viewer.restfulobjects.server;

import org.apache.isis.viewer.restfulobjects.rendering.HasHttpStatusCode;

class ExceptionPojo {

    static int getHttpStatusCodeIfAny(final Throwable ex) {
        if (!(ex instanceof HasHttpStatusCode)) {
            return 0;
        }
        final HasHttpStatusCode hasHttpStatusCode = (HasHttpStatusCode) ex;
        return hasHttpStatusCode.getHttpStatusCode().getStatusCode();
    }


    private final int httpStatusCode;
    private final String message;

    public ExceptionPojo(final Throwable ex) {
        this.httpStatusCode = getHttpStatusCodeIfAny(ex);
        this.message = ex.getMessage();
    }

    @SuppressWarnings("unused")
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }

}
