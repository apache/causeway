package org.apache.isis.incubator.viewer.vaadin.ui.components.debug;

import java.util.Map;

import org.apache.isis.core.commons.internal.collections._Maps;

import lombok.Value;

@Value(staticConstructor = "of")
public class DebugUiModel {

    private final String summaryText;
    private final Map<String, String> keyValuePairs = _Maps.newLinkedHashMap();
    
    public DebugUiModel withProperty(String key, String value) {
        keyValuePairs.put(key, value);
        return this;
    }
    
}
