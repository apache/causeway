package org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.bootstrap3.BS3Tab;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.Util;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class TabPanel extends PanelAbstract {

    private static final long serialVersionUID = 1L;
    private final BS3Tab bs3Tab;

    public TabPanel(String id, final EntityModel model, final BS3Tab bs3Tab) {
        super(id);

        this.bs3Tab = bs3Tab;
        buildGui(model, bs3Tab);
    }

    protected void buildGui(final EntityModel model, final BS3Tab bs3Tab) {

        Util.appendCssClassIfRequired(this, bs3Tab);

        final RepeatingView rv = new RepeatingView("rows");

        for(final BS3Row bs3Row: bs3Tab.getRows()) {

            final String newChildId = rv.newChildId();
            final EntityModel entityModelWithHints = model.cloneWithLayoutMetadata(bs3Row);

            final WebMarkupContainer row = new Row(newChildId, entityModelWithHints);

            rv.add(row);
        }
        add(rv);
    }
}
