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

package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;

public class LongFormView extends AbstractFormView {

    @Override
    protected void addField(final Request request, final ObjectAdapter object, final ObjectAssociation field,
        final LinkedObject linkedObject, final boolean showIcon) {
        if (field.isOneToManyAssociation()) {
            IsisContext.getPersistenceSession().resolveField(object, field);
            final ObjectAdapter collection = field.get(object);
            final ObjectSpecification elementSpec = collection.getElementSpecification();
            final List<ObjectAssociation> fields =
                elementSpec.getAssociations(ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS);
            final boolean isFieldEditable = field.isUsable(IsisContext.getAuthenticationSession(), object).isAllowed();
            final String summary = "Table of elements in " + field.getName();
            TableView.write(request, summary, object, field, collection, fields, isFieldEditable);
        } else {
            FieldValue.write(request, object, field, linkedObject, null, showIcon, 0);
        }
    }

    @Override
    public String getName() {
        return "long-form";
    }

}
