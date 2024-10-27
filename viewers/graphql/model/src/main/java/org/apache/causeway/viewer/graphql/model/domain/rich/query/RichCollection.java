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
package org.apache.causeway.viewer.graphql.model.domain.rich.query;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.SchemaType;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.MemberInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;

public class RichCollection
        extends RichAssociation<OneToManyAssociation, ObjectInteractor>
        implements MemberInteractor<OneToManyAssociation> {

    private final RichMemberHidden<OneToManyAssociation> hidden;
    private final RichMemberDisabled<OneToManyAssociation> disabled;
    private final RichCollectionGet get;
    private final RichCollectionDatatype datatype;

    public RichCollection(
            final ObjectInteractor objectInteractor,
            final OneToManyAssociation otma,
            final Context context
    ) {
        super(objectInteractor, otma, TypeNames.collectionTypeNameFor(objectInteractor.getObjectSpecification(), otma, objectInteractor.getSchemaType()), context);

        if(isBuilt()) {
            this.hidden = null;
            this.disabled = null;
            this.get = null;
            this.datatype = null;
            return;
        }
        addChildFieldFor(this.hidden = new RichMemberHidden<>(this, context));
        addChildFieldFor(this.disabled = new RichMemberDisabled<>(this, context));
        addChildFieldFor(this.get = new RichCollectionGet(this, context));
        addChildFieldFor(this.datatype = new RichCollectionDatatype(this, context));

        buildObjectTypeAndField(otma.asciiId(), otma.getCanonicalDescription().orElse(otma.getCanonicalFriendlyName()));
    }

    @Override
    public ObjectSpecification getObjectSpecification() {
        return interactor.getObjectSpecification();
    }

    @Override
    protected void addDataFetchersForChildren() {
        if(hidden == null) {
            return;
        }
        hidden.addDataFetcher(this);
        disabled.addDataFetcher(this);
        get.addDataFetcher(this);
        datatype.addDataFetcher(this);
    }

    @Override
    public SchemaType getSchemaType() {
        return interactor.getSchemaType();
    }

}
