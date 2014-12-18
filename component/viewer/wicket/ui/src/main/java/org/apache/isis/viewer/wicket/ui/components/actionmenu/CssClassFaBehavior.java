package org.apache.isis.viewer.wicket.ui.components.actionmenu;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.isis.applib.annotation.ActionLayout;

/**
 * A behavior that prepends or appends the markup needed to show a Font Awesome icon
 * for a LinkAndLabel
 */
public class CssClassFaBehavior extends Behavior {

    private final String cssClassFa;
    private final ActionLayout.ClassFaPosition position;

    public CssClassFaBehavior(String cssClassFa, ActionLayout.ClassFaPosition position) {
        this.cssClassFa = cssClassFa;
        this.position = position;
    }

    @Override
    public void beforeRender(Component component) {
        super.beforeRender(component);
        if (position == null || ActionLayout.ClassFaPosition.LEFT == position) {
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
    }

    @Override
    public void afterRender(Component component) {
        if (ActionLayout.ClassFaPosition.RIGHT == position) {
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
        super.afterRender(component);
    }
}
