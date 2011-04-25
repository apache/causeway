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

package org.apache.isis.core.testsupport.jmock;

import org.junit.Rule;

public abstract class AbstractJMockTest {

    protected abstract ConvenienceMockery getContext();

    /**
     * JUnit requires that this is <tt>public</tt>.
     */
    @Rule
    public JMockRule jmockRule = new JMockRule();

    protected void ignoring(final Object mock) {
        getContext().ignoring(mock);
    }

    protected void allowing(final Object mock) {
        getContext().allowing(mock);
    }

    protected void never(final Object mock) {
        getContext().never(mock);
    }

    protected void prohibit(final Object mock) {
        getContext().prohibit(mock);
    }

}
