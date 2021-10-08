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
package org.apache.isis.viewer.wicket.ui.pages.standalonecollection;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class StandaloneCollectionPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    /**
     * For use with {@link Component#setResponsePage(org.apache.wicket.request.component.IRequestablePage)}
     */
    public StandaloneCollectionPage(final EntityCollectionModelStandalone collectionModel) {
        super(PageParameterUtils.newPageParameters(),
                collectionModel.getName(),
                ComponentType.STANDALONE_COLLECTION);

        addChildComponents(themeDiv, collectionModel);
        addBookmarkedPages(themeDiv);
    }

}
