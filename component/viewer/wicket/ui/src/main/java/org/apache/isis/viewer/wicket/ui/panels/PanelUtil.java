package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Strings;

public final class PanelUtil {

    private PanelUtil(){}

    /**
     * The contribution to the header performed implicitly by {@link PanelAbstract}.
     * 
     * <p>
     * Factored out for reuse by {@link LinksSelectorPanelAbstract}.
     */
    public static void renderHead(final IHeaderResponse response, final Class<?> cls) {
        String simpleName = cls.getSimpleName();
        if(Strings.isNullOrEmpty(simpleName)) {
            return; // eg inner classes
        }
        final String url = simpleName + ".css";
        response.render(CssHeaderItem.forReference(new CssResourceReference(cls, url)));
    }
}
