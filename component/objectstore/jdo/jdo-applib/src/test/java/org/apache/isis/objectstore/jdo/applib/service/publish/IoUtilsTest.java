/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.publish;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class IoUtilsTest {

    @Test
    public void roundtrip() {
        final String str = "3784y5hrfbdgkjh3qyri f£$%$YTRGFDGER$£\"Eu098987u'!\"£%^&*IO(LUKJM)";
        final byte[] utf8ZippedBytes = IoUtils.toUtf8ZippedBytes("serializedForm", str);
        
        final String str2 = IoUtils.fromUtf8ZippedBytes("serializedForm", utf8ZippedBytes);
        
        assertThat(str, (is(str2)));
    }

}
