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
package org.apache.causeway.viewer.commons.model.error;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface ErrorFormatter {

    default String formatClass(Class<? extends Throwable> errorClass) {
        return errorClass.getName();
    }

    default String formatMessage(String errorMessage) {
        return errorMessage;
    }

    default String formatElement(StackTraceElement el) {
        return new StringBuilder()
            .append(el.getClassName())
            .append("#")
            .append(el.getMethodName())
            .append("(")
            .append(el.getFileName())
            .append(":")
            .append(el.getLineNumber())
            .append(")\n")
            .toString();
    }

    default List<String> chainJoiningLines() {
        return List.of("", "caused By:", "");
    }

    default List<String> toLines(Throwable cause) {
        var lines = new ArrayList<String>();
        lines.add(formatClass(cause.getClass()));
        lines.add(formatMessage(cause.getMessage()));
        Stream.of(cause.getStackTrace())
            .map(this::formatElement)
            .forEach(lines::add);
        return lines;
    }

}
