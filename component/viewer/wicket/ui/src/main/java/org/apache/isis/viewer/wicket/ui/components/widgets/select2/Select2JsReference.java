package org.apache.isis.viewer.wicket.ui.components.widgets.select2;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

import com.google.common.collect.Lists;
import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A JavaScript reference that loads <a href="https://github.com/ivaynberg/select2/">Select2.js</a>
 * <p>Depends on JQuery.</p>
 */
public class Select2JsReference extends WebjarsJavaScriptResourceReference {

    public Select2JsReference() {
        super("/select2/current/select2.js");
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        ResourceReference jQueryReference = Application.get().getJavaScriptLibrarySettings().getJQueryReference();
        return Lists.newArrayList(JavaScriptHeaderItem.forReference(jQueryReference));
    }
}
