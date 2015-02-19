package org.apache.isis.viewer.wicket.model.common;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * A helper class for dealing with PageParameters
 */
public class PageParametersUtils {

    /**
     * The name of the special request parameter that controls whether the page header/navigation bar
     * should be shown or not
     */
    public static final String ISIS_NO_HEADER_PARAMETER_NAME = "isis.no.header";

    /**
     * The name of the special request parameter that controls whether the page footer
     * should be shown or not
     */
    public static final String ISIS_NO_FOOTER_PARAMETER_NAME = "isis.no.footer";

    /**
     * Creates a new instance of PageParameters that preserves some special request parameters
     * which should propagate in all links created by Isis
     *
     * @return a new PageParameters instance
     */
    public static PageParameters newPageParameters() {
        final PageParameters newPageParameters = new PageParameters();
        final RequestCycle cycle = RequestCycle.get();

        if (cycle != null) {
            final IPageRequestHandler pageRequestHandler = PageRequestHandlerTracker.getFirstHandler(cycle);
            final PageParameters currentPageParameters = pageRequestHandler.getPageParameters();
            if (currentPageParameters != null) {
                final StringValue noHeader = currentPageParameters.get(ISIS_NO_HEADER_PARAMETER_NAME);
                if (!noHeader.isNull()) {
                    newPageParameters.set(ISIS_NO_HEADER_PARAMETER_NAME, noHeader.toString());
                }
                final StringValue noFooter = currentPageParameters.get(ISIS_NO_FOOTER_PARAMETER_NAME);
                if (!noFooter.isNull()) {
                    newPageParameters.set(ISIS_NO_FOOTER_PARAMETER_NAME, noFooter.toString());
                }
            }
        }
        return newPageParameters;
    }
}
