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
package org.apache.isis.viewer.json.viewer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.json.viewer.RepContext;
import org.junit.Test;


public class RepresentationContextTest_relFor {

    private RepContext context;
    
    @Test
    public void bothNonNull() throws Exception {
        context = new RepContext(null, "attribute");
        String relFor = context.relFor("relSuffix");
        assertThat(relFor, is("attribute.relSuffix"));
    }

    @Test
    public void attributeNull() throws Exception {
        context = new RepContext(null, null);
        String relFor = context.relFor("relSuffix");
        assertThat(relFor, is("relSuffix"));
    }

    @Test
    public void relSuffixNull() throws Exception {
        context = new RepContext(null, "attribute");
        String relFor = context.relFor(null);
        assertThat(relFor, is("attribute"));
    }

    @Test
    public void bothNull() throws Exception {
        context = new RepContext(null, null);
        String relFor = context.relFor(null);
        assertThat(relFor, is(""));
    }

}
