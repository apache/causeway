package org.apache.isis.testing.archtestsupport.applib.packagerules;

import com.tngtech.archunit.library.Architectures;

import lombok.val;

public interface Subpackage {
    String getName();

    SubpackageType getSubpackageType();

    String packageIdentifierWithin(Class<?> moduleClass);

    default void defineLayer(Architectures.LayeredArchitecture layeredArchitecture, Class<?> moduleClass) {
        val layerDefinition = getSubpackageType().defineLayer(this, moduleClass, layeredArchitecture);
        layerDefinition.definedBy(packageIdentifierWithin(moduleClass));
    }

    boolean canReference(Subpackage referenced);
}
