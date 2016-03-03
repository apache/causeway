package org.apache.isis.applib.layout.grid.bootstrap3;

/**
 * As per <a href="http://getbootstrap.com/css/#grid-options">grid options</a>, also used in
 * <a href="http://getbootstrap.com/css/#responsive-utilities">responsive utility</a> classes.
 */
public enum Size {
    XS,
    SM,
    MD,
    LG;

    public String toCssClassFragment() {
        return name().toLowerCase();
    }
}
