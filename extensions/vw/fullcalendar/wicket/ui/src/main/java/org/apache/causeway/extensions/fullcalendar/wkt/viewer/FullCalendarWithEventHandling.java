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
package org.apache.causeway.extensions.fullcalendar.wkt.viewer;

import org.apache.wicket.RestartResponseException;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ProtoObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarConfig;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.CalendarResponse;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.FullCalendar;
import org.apache.causeway.extensions.fullcalendar.wkt.fullcalendar.callback.ClickedEvent;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import lombok.val;

final class FullCalendarWithEventHandling extends FullCalendar {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private final NotificationPanel feedback;
    private transient MetaModelContext commonContext;


    FullCalendarWithEventHandling(
            final String id,
            final CalendarConfig config,
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

        val commonContext = getMetaModelContext();

        final SpecificationLoader specificationLoader = commonContext.getSpecificationLoader();
        final ObjectManager objectManager = commonContext.getObjectManager();

        val managedObject = objectManager
                .loadObject(ProtoObject.resolveElseFail(specificationLoader, bookmark));

        final UiObjectWkt entityModel = UiObjectWkt.ofAdapter(commonContext, managedObject);

        val pageParameters = entityModel.getPageParameters();
        if(pageParameters!=null) {
            throw new RestartResponseException(EntityPage.class, pageParameters);
        }

        // otherwise ignore
    }

    public MetaModelContext getMetaModelContext() {
        return commonContext = WktContext.computeIfAbsent(commonContext);
    }

}
