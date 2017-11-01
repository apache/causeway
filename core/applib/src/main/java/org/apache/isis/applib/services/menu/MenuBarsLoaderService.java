package org.apache.isis.applib.services.menu;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.menus.MenuBars;

public interface MenuBarsLoaderService {

    /**
     * Whether dynamic reloading of layouts is enabled.
     */
    @Programmatic
    boolean supportsReloading();

    /**
     * Returns a new instance of a {@link MenuBars}, else <tt>null</tt>.
     */
    @Programmatic
    MenuBars menuBars();

}
