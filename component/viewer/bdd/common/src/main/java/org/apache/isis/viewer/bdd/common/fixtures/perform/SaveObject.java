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
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;

public class SaveObject extends PerformAbstractTypeParams {

    private ObjectAdapter result;

    public SaveObject(final Perform.Mode mode) {
        super("save", Type.OBJECT, NumParameters.ZERO, mode);
    }

    @Override
    public void doHandle(final PerformContext performContext) throws ScenarioBoundValueException {

        final ObjectAdapter onAdapter = performContext.getOnAdapter();

        final Consent valid = onAdapter.getSpecification().isValid(onAdapter);

        final CellBinding performBinding = performContext.getPeer().getPerformBinding();
        if (valid.isVetoed()) {
            throw ScenarioBoundValueException.current(performBinding, valid.getReason());
        }

        if (onAdapter.representsPersistent()) {
            throw ScenarioBoundValueException.current(performBinding, "(already persistent)");
        }

        // persist

        // xactn mgmt now performed by PersistenceSession#makePersistent
        // getOwner().getTransactionManager().startTransaction();
        getPersistenceSession().makePersistent(onAdapter);
        // getOwner().getTransactionManager().endTransaction();

        // all OK.
    }

    protected Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

}
