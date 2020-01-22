package org.apache.isis.extensions.fullcalendar.ui.component;

import org.apache.wicket.RestartResponseException;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
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
