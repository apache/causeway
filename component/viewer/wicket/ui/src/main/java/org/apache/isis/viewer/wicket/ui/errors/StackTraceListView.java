package org.apache.isis.viewer.wicket.ui.errors;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

public final class StackTraceListView extends ListView<StackTraceDetail> {
    
    private static final long serialVersionUID = 1L;
    private final String idLine;

    public StackTraceListView(String id, String idLine, List<? extends org.apache.isis.viewer.wicket.ui.errors.StackTraceDetail> list) {
        super(id, list);
        this.idLine = idLine;
    }

    @Override
    protected void populateItem(ListItem<StackTraceDetail> item) {
        final StackTraceDetail detail = item.getModelObject();
        Label label = new Label(idLine, detail.getLine());
        item.add(new AttributeAppender("class", detail.getType().name().toLowerCase()));
        item.add(label);
    }
}