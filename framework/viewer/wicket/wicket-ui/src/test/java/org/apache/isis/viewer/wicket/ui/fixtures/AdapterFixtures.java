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

package org.apache.isis.viewer.wicket.ui.fixtures;

import org.jmock.Expectations;
import org.jmock.Mockery;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public final class AdapterFixtures {

    private final Mockery context;

    public AdapterFixtures(final Mockery context) {
        this.context = context;
    }

    public void getOid(final ObjectAdapter mockAdapter, final Object returns) {
        context.checking(new Expectations() {
            {
                allowing(mockAdapter).getOid();
                will(returnValue(returns));
            }
        });
    }

}
