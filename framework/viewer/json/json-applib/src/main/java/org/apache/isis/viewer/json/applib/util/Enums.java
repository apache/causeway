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
package org.apache.isis.viewer.json.applib.util;

public final class Enums {
    
    private Enums() {}

    public static String enumToHttpHeader(Enum<?> anEnum) {
        return enumNameToHttpHeader(anEnum.name());
    }

    public static String enumNameToHttpHeader(String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = true;
        for(char c: name.toCharArray()) {
            if(c == '_') {
                nextUpper = true;
                builder.append("-");
            } else {
                builder.append(nextUpper?c:Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }

    public static String enumToCamelCase(Enum<?> anEnum) {
        return enumNameToCamelCase(anEnum.name());
    }

    private static String enumNameToCamelCase(String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = false;
        for(char c: name.toCharArray()) {
            if(c == '_') {
                nextUpper = true;
            } else {
                builder.append(nextUpper?c:Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }


}
