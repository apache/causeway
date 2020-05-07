package org.apache.isis.viewer.common.model.feature;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

public interface FeatureUiModel {

    ObjectFeature getMetaModel();
    
    /** property name */
    default String getName() {
        return getMetaModel().getName();
    }
    
}
