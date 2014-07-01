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
package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class GetPropertyDefault extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public GetPropertyDefault(final Perform.Mode mode) {
        super("get property default", Type.PROPERTY, NumParameters.ZERO, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();
        final ObjectMember nakedObjectMember = performContext.getObjectMember();

        final OneToOneAssociation otoa = (OneToOneAssociation) nakedObjectMember;

        // TODO: the OTOA interface is wrong, should be declared as returning a
        // NakedObject
        // (which is indeed what the implementation does)
        result = otoa.getDefault(onAdapter);
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
