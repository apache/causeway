package org.apache.isis.viewer.wicket.ui.components.about;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

public final class JarManifestListView extends ListView<JarManifestAttributes> {
    
    private static final long serialVersionUID = 1L;
    private final String idLine;

    public JarManifestListView(String id, String idLine, List<? extends JarManifestAttributes> list) {
        super(id, list);
        this.idLine = idLine;
    }

    @Override
    protected void populateItem(ListItem<JarManifestAttributes> item) {
        final JarManifestAttributes detail = item.getModelObject();
        Label label = new Label(idLine, detail.getLine());
        item.add(new AttributeAppender("class", detail.getType().name().toLowerCase()));
        item.add(label);
    }
}