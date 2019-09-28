package org.apache.isis.config;

import org.apache.isis.applib.annotation.LabelPosition;

public interface ConfigPropsForPropertyOrParameterLayout {
    public LabelPosition getLabelPosition();

    /**
     * Alias for {@link #getLabelPosition()}
     */
    public LabelPosition getLabel();
}
