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
package org.apache.causeway.applib.services.wrapper.control;

import java.lang.reflect.Method;

import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * used for exception logging
 */
public record AsyncLogger(ExceptionHandler rootExceptionHandler, Method method, Bookmark bookmark) implements ExceptionHandler {

    static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncControl.class);

    @Override
    public Object handle(Exception ex) throws Exception {
        log(ex);
        return rootExceptionHandler.handle(ex);
    }

    void log(Exception ex) {
        var buf = new StringBuilder("Failed to execute ");
        if(method() != null) {
            buf.append(" ").append(method().getName()).append(" ");
            if(bookmark() != null) {
                buf.append(" on '")
                        .append(bookmark().logicalTypeName())
                        .append(":")
                        .append(bookmark().identifier())
                        .append("'");
            }
        }
        log.error(buf.toString(), ex);
    }

}
