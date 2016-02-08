package org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.bootstrap3.BS3Tab;
import org.apache.isis.viewer.wicket.model.hints.HasUiHintDisambiguator;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.Util;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabPanel extends PanelAbstract implements HasUiHintDisambiguator {

    private static final long serialVersionUID = 1L;

    private static final String ID_TAB_PANEL = "tabPanel";
    private static final String ID_ROWS = "rows";

    private final BS3Tab bs3Tab;

    public TabPanel(String id, final EntityModel model, final BS3Tab bs3Tab) {
        super(id);

        this.bs3Tab = bs3Tab;
        buildGui(model, bs3Tab);
    }

    /**
     * when tabs are rendered, they don't distinguish within the path hierarchy: even if on different tabs, the first
     * panel will have the same Wicket path hierarchy.  This property allows us to distinguish.
     */
    public String getHintDisambiguator() {
        return bs3Tab.getName();
    }

    protected void buildGui(final EntityModel model, final BS3Tab bs3Tab) {

        final WebMarkupContainer container = new WebMarkupContainer(ID_TAB_PANEL);

        Util.appendCssClassIfRequired(this, bs3Tab);

        final RepeatingView rv = new RepeatingView(ID_ROWS);

        for(final BS3Row bs3Row: bs3Tab.getRows()) {

            final String newChildId = rv.newChildId();
            final EntityModel entityModelWithHints = model.cloneWithLayoutMetadata(bs3Row);

            final WebMarkupContainer row = new Row(newChildId, entityModelWithHints);

            rv.add(row);
        }
        container.add(rv);
        add(container);
    }
}
