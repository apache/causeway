package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;

public class HintUtil {
    private HintUtil() {
    }

    public static String hintPathFor(Component component) {
        return Strings.afterFirstPathComponent(fullHintPathFor(component), Component.PATH_SEPARATOR);
    }

    private static String fullHintPathFor(Component component) {
        final PrependingStringBuffer buffer = new PrependingStringBuffer(32);
        for (Component c = component; c != null; c = c.getParent()) {
            if (buffer.length() > 0) {
                buffer.prepend(Component.PATH_SEPARATOR);
            }
            buffer.prepend(c.getId());
        }
        return buffer.toString();
    }

}
