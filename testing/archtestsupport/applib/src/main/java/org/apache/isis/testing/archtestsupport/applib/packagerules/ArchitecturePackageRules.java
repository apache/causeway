package org.apache.isis.testing.archtestsupport.applib.packagerules;

import java.util.List;
import java.util.stream.Collectors;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ArchitecturePackageRules {

    public static ArchRule code_dependencies_follow_module_subpackages(Class<?> moduleClass, List<Subpackage> subpackages) {
        val layeredArchitecture = Architectures.layeredArchitecture();
        defineAndCheckSubpackageDependencies(moduleClass, layeredArchitecture, subpackages);
        return layeredArchitecture;
    }

    void defineAndCheckSubpackageDependencies(Class<?> moduleClass, Architectures.LayeredArchitecture layeredArchitecture, List<Subpackage> subpackages) {
        subpackages.forEach(subpackage -> subpackage.defineLayer(layeredArchitecture, moduleClass));

        subpackages.forEach(referenced -> {
            final String[] referencingSubpackageNames =
                    subpackages.stream().filter(subpackage -> subpackage.canReference(referenced))
                            .map(Subpackage::getName)
                            .collect(Collectors.toList()).toArray(new String[]{});
            layeredArchitecture.whereLayer(referenced.getName()).mayOnlyBeAccessedByLayers(referencingSubpackageNames);
        });
    }

}
