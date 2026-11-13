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
package org.apache.causeway.viewer.wicket.model.models.coll;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;

record CollectionModelEmpty()
implements CollectionModel {

    @Override
    public int getElementCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "hidden";
    }

    @Override
    public ManagedObject getParentObject() {
        return ManagedObject.unspecified();
    }

    @Override
    public boolean isTableDataLoaded() {
        return true;
    }

    @Override
    public Can<ActionModel> getLinks() {
        return Can.empty();
    }

    @Override
    public DataTableInteractive getDataTableModel() {
        return getObject();
    }

    // -- NOT IMPLEMENTED

    @Override
    public DataTableInteractive getObject() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Variant getVariant() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public ObjectMember getMetaModel() {
        throw _Exceptions.notImplemented();
    }

}
