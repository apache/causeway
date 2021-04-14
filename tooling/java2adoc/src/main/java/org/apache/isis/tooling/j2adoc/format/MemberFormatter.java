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
package org.apache.isis.tooling.j2adoc.format;

public interface MemberFormatter {

    public default String getEnumConstantFormat() {
        return "`%s`";
    }

    public default String getAnnotationMemberFormat() {
        return "`%2$s` : `%1$s`";
    }

    public default String getFieldFormat() {
        return "`%2$s` : `%1$s`";
    }

    public default String getConstructorFormat() {
        return "`%1$s(%2$s)`";
    }

    public default String getGenericConstructorFormat() {
        return "`%2$s%1$s(%3$s)`";
    }

    public default String getMethodFormat() {
        return "`%2$s(%3$s)` : `%1$s`";
    }

    public default String getGenericMethodFormat() {
        return "`%3$s%1$s(%4$s)` : `%2$s`";
    }

}
