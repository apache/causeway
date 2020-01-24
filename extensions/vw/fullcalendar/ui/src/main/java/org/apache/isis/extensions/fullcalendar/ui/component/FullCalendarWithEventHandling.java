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

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import net.ftlines.wicket.fullcalendar.CalendarResponse;
import net.ftlines.wicket.fullcalendar.Config;
import net.ftlines.wicket.fullcalendar.FullCalendar;
import net.ftlines.wicket.fullcalendar.callback.ClickedEvent;

final class FullCalendarWithEventHandling extends FullCalendar {
    
    @SuppressWarnings("unused")
	private final NotificationPanel feedback;
    private static final long serialVersionUID = 1L;

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
        final RootOid oid = RootOid.deString(oidStr);

        IsisContext.getCurrentIsisSession()
                .map(isisSession -> {
                    final SpecificationLoader specificationLoader = isisSession.getSpecificationLoader();
                    final MetaModelContext metaModelContext = isisSession.getMetaModelContext();
                    final ObjectManager objectManager = isisSession.getObjectManager();
                    final IsisWebAppCommonContext webAppCommonContext = IsisWebAppCommonContext.of(metaModelContext);

                    val spec = specificationLoader.loadSpecification(oid.getObjectSpecId());
                    val objectId = oid.getIdentifier();
                    val managedObject = objectManager.loadObject(ObjectLoader.Request.of(spec, objectId));

                    final EntityModel entityModel = EntityModel.ofAdapter(webAppCommonContext, managedObject);
                    return entityModel.getPageParameters();
                }).ifPresent(pageParameters -> {
                    throw new RestartResponseException(EntityPage.class, pageParameters);
                });

        // otherwise ignore
    }



}
