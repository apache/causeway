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
package org.apache.isis.extensions.fullcalendar.ui.component;

import org.apache.wicket.RestartResponseException;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Config;
import net.ftlines.wicket.fullcalendar.FullCalendar;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;

final class FullCalendarWithEventHandling extends FullCalendar {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private final NotificationPanel feedback;
    private transient IsisAppCommonContext commonContext;


    FullCalendarWithEventHandling(
            final String id,
            final Config config,
            final NotificationPanel feedback) {
        super(id, config);
        this.feedback = feedback;
    }

    @Override
    protected void onEventClicked(
            final ClickedEvent event,
            final CalendarResponse response) {

        final String oidStr = (String) event.getEvent().getPayload();
        final Bookmark bookmark = Bookmark.parse(oidStr).orElse(null);
        if(bookmark==null) {
            return;
        }

        val commonContext = getCommonContext();

        final SpecificationLoader specificationLoader = commonContext.getSpecificationLoader();
        final MetaModelContext metaModelContext = commonContext.getMetaModelContext();
        final ObjectManager objectManager = commonContext.getObjectManager();
        final IsisAppCommonContext webAppCommonContext = IsisAppCommonContext.of(metaModelContext);

        val spec = specificationLoader.specForLogicalTypeName(bookmark.getLogicalTypeName()).orElse(null);
        val managedObject = objectManager.loadObject(ObjectLoader.Request.of(spec, bookmark));

        final EntityModel entityModel = EntityModel.ofAdapter(webAppCommonContext, managedObject);

        val pageParameters = entityModel.getPageParameters();
        if(pageParameters!=null) {
            throw new RestartResponseException(EntityPage.class, pageParameters);
        }

        // otherwise ignore
    }

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

}
