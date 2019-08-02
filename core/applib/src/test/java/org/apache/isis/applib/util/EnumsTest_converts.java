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
package org.apache.isis.applib.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EnumsTest_converts {

    private static enum MyEnum {
        CONTENT_TYPE, LAST_MODIFIED, WARNING, X_REPRESENTATION_TYPE, OBJECT_ACTION
    }

    @Test
    public void converts() {
        assertConverts(MyEnum.CONTENT_TYPE, "Content-Type", "contentType");
        assertConverts(MyEnum.LAST_MODIFIED, "Last-Modified", "lastModified");
        assertConverts(MyEnum.WARNING, "Warning", "warning");
        assertConverts(MyEnum.X_REPRESENTATION_TYPE, "X-Representation-Type", "xRepresentationType");
    }

    protected void assertConverts(final Enum<?> someEnum, final String httpHeader, final String camelCase) {
        assertThat(Enums.enumToHttpHeader(someEnum), is(httpHeader));
        assertThat(Enums.enumToCamelCase(someEnum), is(camelCase));
    }

}
