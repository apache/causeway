package org.apache.isis.viewer.dnd.view;

import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.util.Properties;

public final class ViewConstants {

    /** Horizontal padding (||) between two components */
    public static final int HPADDING = IsisContext.getConfiguration().getInteger(Properties.PROPERTY_BASE + "hpadding", 3);
    /** Vertical padding (=) between two components */
    public static final int VPADDING = IsisContext.getConfiguration().getInteger(Properties.PROPERTY_BASE + "vpadding", 3);

}
