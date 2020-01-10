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

package org.apache.isis.core.metamodel.commons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaClassUtilsTest_getBuiltIn {

    @Test
    public void voidBuiltIns() throws ClassNotFoundException {
        assertEquals(ClassUtil.getBuiltIn("void"), void.class);
        assertEquals(ClassUtil.getBuiltIn("boolean"), boolean.class);
        assertEquals(ClassUtil.getBuiltIn("byte"), byte.class);
        assertEquals(ClassUtil.getBuiltIn("short"), short.class);
        assertEquals(ClassUtil.getBuiltIn("int"), int.class);
        assertEquals(ClassUtil.getBuiltIn("long"), long.class);
        assertEquals(ClassUtil.getBuiltIn("char"), char.class);
        assertEquals(ClassUtil.getBuiltIn("float"), float.class);
        assertEquals(ClassUtil.getBuiltIn("double"), double.class);
    }

}
