package org.apache.isis.testing.archtestsupport.applib.packagerules;

import com.tngtech.archunit.library.Architectures;

public enum SubpackageType {
    MANDATORY {
        @Override
        Architectures.LayeredArchitecture.LayerDefinition defineLayer(
                Subpackage subpackage, Class<?> moduleClass, Architectures.LayeredArchitecture layeredArchitecture) {
            return layeredArchitecture.layer(subpackage.getName());
        }
    },
    OPTIONAL {
        @Override
        Architectures.LayeredArchitecture.LayerDefinition defineLayer(
                Subpackage subpackage, Class<?> moduleClass, Architectures.LayeredArchitecture layeredArchitecture) {
            return layeredArchitecture.optionalLayer(subpackage.getName());
        }
    };

    abstract Architectures.LayeredArchitecture.LayerDefinition defineLayer(Subpackage subpackage, Class<?> moduleClass, Architectures.LayeredArchitecture layeredArchitecture);
}
