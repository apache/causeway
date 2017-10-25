package org.apache.isis.applib.services.menu;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.menus.MenuBars;

public interface MenuBarsService {
    @Programmatic
    MenuBars menuBars();
}
