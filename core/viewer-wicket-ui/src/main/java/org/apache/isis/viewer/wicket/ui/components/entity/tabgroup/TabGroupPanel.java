package org.apache.isis.viewer.wicket.ui.components.entity.tabgroup;

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.layout.v1_0.TabMetadata;
import org.apache.isis.applib.layout.v1_0.TabGroupMetadata;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.tabpanel.TabPanel;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

public class TabGroupPanel extends AjaxBootstrapTabbedPanel {

    private final EntityModel entityModel;
    // the view metadata
    private final TabGroupMetadata tabGroup;

    private static final String ID_TAB_GROUP = "tabGroup";

    private static List<ITab> tabsFor(final EntityModel entityModel) {
        final List<ITab> tabs = Lists.newArrayList();

        final TabGroupMetadata tabGroup = entityModel.getTabGroupMetadata();
        final List<TabMetadata> tabMetadataList = FluentIterable
                .from(tabGroup.getTabs())
                .filter(TabMetadata.Predicates.notEmpty())
                .toList();

        for (final TabMetadata tabMetadata : tabMetadataList) {
            tabs.add(new AbstractTab(Model.of(tabMetadata.getName())) {
                private static final long serialVersionUID1 = 1L;

                @Override
                public Panel getPanel(String panelId) {
                    return new TabPanel(panelId, entityModel, tabMetadata);
                }
            });
        }
        return tabs;
    }

    public TabGroupPanel(final EntityModel entityModel) {
        super(ID_TAB_GROUP, tabsFor(entityModel));

        this.entityModel = entityModel;
        this.tabGroup = entityModel.getTabGroupMetadata();

        setSelectedTabFromSessionIfAny(this.tabGroup, this, entityModel);
    }

    @Override
    public TabbedPanel setSelectedTab(final int index) {
        saveSelectedTabInSession(tabGroup, index, entityModel);
        return super.setSelectedTab(index);
    }

    private void setSelectedTabFromSessionIfAny(
            final TabGroupMetadata tabGroup,
            final AjaxBootstrapTabbedPanel ajaxBootstrapTabbedPanel,
            final EntityModel entityModel) {
        final String key = buildKey(tabGroup, entityModel);
        final String value = (String) getSession().getAttribute(key);
        if (value != null) {
            final int tabIndex = Integer.parseInt(value);
            final int numTabs = ajaxBootstrapTabbedPanel.getTabs().size();
            if (tabIndex < numTabs) {
                // to support dynamic reloading; the data in the session might not be compatible with current layout.
                ajaxBootstrapTabbedPanel.setSelectedTab(tabIndex);
            }
        }
    }

    private void saveSelectedTabInSession(
            final TabGroupMetadata tabGroup,
            final int tabIndex,
            final EntityModel entityModel) {
        final String key = buildKey(tabGroup, entityModel);
        getSession().setAttribute(key, "" + tabIndex);
    }

    private String buildKey(final TabGroupMetadata tabGroup, final EntityModel entityModel) {
        final ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        final RootOid oid = (RootOid) objectAdapterMemento.getObjectAdapter(
                AdapterManager.ConcurrencyChecking.NO_CHECK).getOid();
        final String key =
                IsisContext.getOidMarshaller().marshalNoVersion(oid) + ":" + tabGroup.getPath() + "#selectedTab";
        return key;
    }

}
