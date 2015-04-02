package org.apache.isis.viewer.wicket.ui.components.widgets.favicon;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.util.string.Strings;

/**
 * A component for application favorite icon
 */
public class Favicon extends WebComponent {

    @Inject(optional = true)
    @Named("faviconUrl")
    private String url;

    @Inject(optional = true)
    @Named("faviconContentType")
    private String contentType;

    public Favicon(String id) {
        super(id);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        setVisible(!Strings.isEmpty(url));
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        tag.put("href", url);

        if (!Strings.isEmpty(contentType)) {
            tag.put("type", contentType);
        }
    }
}
