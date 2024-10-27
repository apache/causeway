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
package org.apache.causeway.viewer.graphql.model.domain.common.query;

import graphql.schema.DataFetchingEnvironment;

import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.model.context.Context;
import org.apache.causeway.viewer.graphql.model.domain.ElementCustom;
import org.apache.causeway.viewer.graphql.model.domain.TypeNames;
import org.apache.causeway.viewer.graphql.model.domain.common.interactors.ObjectInteractor;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaCssClass;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaFetcher;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaGrid;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaIcon;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaId;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaLayout;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaLogicalTypeName;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaSaveAs;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaTitle;
import org.apache.causeway.viewer.graphql.model.domain.common.query.meta.CommonMetaVersion;

public class CommonMeta extends ElementCustom {

    private final ObjectInteractor holder;
    private final CommonMetaId metaId;
    private final CommonMetaLogicalTypeName metaLogicalTypeName;
    private final CommonMetaVersion metaVersion;
    private final CommonMetaTitle metaTitle;
    private final CommonMetaIcon metaIcon;
    private final CommonMetaCssClass metaCssClass;
    private final CommonMetaLayout metaLayout;
    private final CommonMetaGrid metaGrid;
    private final CommonMetaSaveAs metaSaveAs;

    private final CausewayConfiguration.Viewer.Graphql graphqlConfiguration;

    public CommonMeta(
            final ObjectInteractor objectInteractor,
            final Context context
    ) {
        super(TypeNames.metaTypeNameFor(objectInteractor.getObjectSpecification(), objectInteractor.getSchemaType()), context);
        this.holder = objectInteractor;

        this.graphqlConfiguration = context.causewayConfiguration.getViewer().getGraphql();

        if(isBuilt()) {
            this.metaId = null;
            this.metaLogicalTypeName = null;
            this.metaVersion = null;
            this.metaTitle = null;
            this.metaIcon = null;
            this.metaCssClass = null;
            this.metaLayout = null;
            this.metaGrid = null;
            this.metaSaveAs = null;
            return;
        }

        addChildFieldFor(this.metaId = new CommonMetaId(context));
        addChildFieldFor(this.metaLogicalTypeName = new CommonMetaLogicalTypeName(context));
        addChildFieldFor(this.metaVersion = isEntity() ? new CommonMetaVersion(context) : null);
        addChildFieldFor(this.metaTitle = new CommonMetaTitle(context));
        addChildFieldFor(this.metaCssClass = new CommonMetaCssClass(context));
        addChildFieldFor(this.metaLayout = new CommonMetaLayout(context));
        addChildFieldFor(this.metaSaveAs = new CommonMetaSaveAs(context));

        addChildFieldFor(this.metaIcon = isResourceNotForbidden() ? new CommonMetaIcon(context) : null);
        addChildFieldFor(this.metaGrid = isResourceNotForbidden() ? new CommonMetaGrid(context) : null);

        var fieldName = graphqlConfiguration.getMetaData().getFieldName();
        buildObjectTypeAndField(fieldName, "Object metadata");
    }

    private boolean isResourceNotForbidden() {
        return graphqlConfiguration.getResources().getResponseType() != CausewayConfiguration.Viewer.Graphql.ResponseType.FORBIDDEN;
    }

    private boolean isEntity() {
        return holder.getObjectSpecification().getBeanSort() == BeanSort.ENTITY;
    }

    @Override
    protected void addDataFetchersForChildren() {
        if (metaId == null) {
            // none of the fields will have been initialized
            return;
        }
        metaId.addDataFetcher(this);
        metaLogicalTypeName.addDataFetcher(this);
        if (isEntity()) {
            metaVersion.addDataFetcher(this);
        }
        metaTitle.addDataFetcher(this);
        metaCssClass.addDataFetcher(this);
        metaLayout.addDataFetcher(this);
        metaSaveAs.addDataFetcher(this);
        if (metaGrid != null) {
            metaGrid.addDataFetcher(this);
        }
        if (metaIcon != null) {
            metaIcon.addDataFetcher(this);
        }
    }

    @Override
    public Object fetchData(final DataFetchingEnvironment environment) {
        return context.bookmarkService.bookmarkFor(environment.getSource())
                .map(bookmark -> new CommonMetaFetcher(bookmark, context.bookmarkService, context.objectManager, context.causewayConfiguration))
                .orElseThrow();
    }

}
