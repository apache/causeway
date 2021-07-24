package org.apache.isis.testing.archtestsupport.applib.modulerules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.library.Architectures;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * A library of architecture tests to ensure correct layering and usage of packages.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ArchitectureModuleRules {

    /**
     * Utility method to aggregate the module classes (named &quot;XxxModule&quot;) that are extracted from the
     * {@link AnalyzeClasses} annotation on the provided class.
     *
     * <p>
     * The result is intended to be passed into {@link #code_dependencies_follow_module_Imports(List)} and
     * {@link #code_dependencies_follow_module_Imports_and_subpackage_rules(List, List)}.
     * </p>
     *
     * @see #code_dependencies_follow_module_Imports(List)
     * @see #code_dependencies_follow_module_Imports_and_subpackage_rules(List, List)
     */
    public static List<Class<?>> analyzeClasses_packagesOf(Class<?> clazz) {
        val analyzeClassesAnnot = AnnotationUtils.findAnnotation(clazz, AnalyzeClasses.class);
        return Arrays.stream(analyzeClassesAnnot.packagesOf())
                .filter(x -> x.getSimpleName().endsWith("Module"))
                .collect(Collectors.toList());
    }

    /**
     * Ensures that the actual dependencies between classes within modules honour the module dependency graph inferred
     * from the {@link Import} statements of each module.
     *
     * @see #code_dependencies_follow_module_Imports_and_subpackage_rules(List, List)
     */
    public static Architectures.LayeredArchitecture code_dependencies_follow_module_Imports(
            List<Class<?>> moduleClasses) {

        return code_dependencies_follow_module_Imports_and_subpackage_rules(moduleClasses, Collections.emptyList());
    }

    /**
     * Ensures that the actual dependencies between classes within modules honour the module dependency graph inferred
     * from the {@link Import} statements of each module AND also ensures that the subpackages within those rules only
     * access the allowed subpackages of both their own &local;local&local; and the subpackages of other modules that
     * they might reference.
     *
     * @see #code_dependencies_follow_module_Imports(List)
     */
    public static Architectures.LayeredArchitecture code_dependencies_follow_module_Imports_and_subpackage_rules(
            List<Class<?>> moduleClasses, List<Subpackage> subpackages) {
        val layeredArchitecture = Architectures.layeredArchitecture();

        defineLayers(moduleClasses, layeredArchitecture, subpackages);

        val directDependenciesByImported = new HashMap<Class<?>, Set<Class<?>>>();
        val directDependenciesByImporting = new HashMap<Class<?>, Set<Class<?>>>();
        computeDirectDependencies(moduleClasses, directDependenciesByImported,
                directDependenciesByImporting);

        val transitiveDependenciesByImporting = new HashMap<Class<?>, Set<Class<?>>>();
        computeTransitiveDependencies(moduleClasses, directDependenciesByImporting,
                transitiveDependenciesByImporting);

        val transitiveDependenciesByImported = invert(transitiveDependenciesByImporting);
        checkLayerAccess(layeredArchitecture, transitiveDependenciesByImported, subpackages);

        val importingClassesNotImported = new LinkedHashSet<>(
                transitiveDependenciesByImporting.keySet());
        importingClassesNotImported.removeAll(transitiveDependenciesByImported.keySet());

        return layeredArchitecture;
    }

    private static void defineLayers(
            List<Class<?>> moduleClasses,
            Architectures.LayeredArchitecture layeredArchitecture,
            List<Subpackage> subpackages) {
        moduleClasses.forEach(moduleClass ->
                subpackages.forEach(subpackage -> {
                    val subpackageName = subpackage.getName();
                    layeredArchitecture.optionalLayer(nameOf(moduleClass, subpackageName))
                            .definedBy(packageIdentifierFor(moduleClass, subpackage));
                }));
    }

    private static void computeDirectDependencies(
            List<Class<?>> moduleClasses,
            Map<Class<?>, Set<Class<?>>> directDependenciesByImported,
            Map<Class<?>, Set<Class<?>>> directDependenciesByImporting) {
        moduleClasses.forEach(
                moduleClass -> {
                    final Import importAnnotation = AnnotationUtils.findAnnotation(moduleClass, Import.class);
                    if (importAnnotation != null) {
                        val importedClassesFromAnnot = importAnnotation.value();
                        val importedClasses = new LinkedHashSet<>(Arrays.asList(importedClassesFromAnnot));
                        directDependenciesByImporting.put(moduleClass, importedClasses);
                        importedClasses.forEach(
                                importedClass -> {
                                    val importingClasses = directDependenciesByImported
                                            .computeIfAbsent(importedClass, k -> new LinkedHashSet<>());
                                    importingClasses.add(moduleClass);
                                }
                        );
                    }
                }
        );
    }

    private static void computeTransitiveDependencies(
            List<Class<?>> moduleClasses,
            Map<Class<?>, Set<Class<?>>> directDependenciesByImporting,
            Map<Class<?>, Set<Class<?>>> transitiveDependenciesByImporting) {
        moduleClasses.forEach((moduleClass) -> {
            val transitiveDependencies = new LinkedHashSet<Class<?>>();
            accumulateTransitiveDependencies(moduleClass, directDependenciesByImporting,
                    transitiveDependencies);
            transitiveDependenciesByImporting.put(moduleClass, transitiveDependencies);
        });
    }

    private static void checkLayerAccess(
            Architectures.LayeredArchitecture layeredArchitecture,
            Map<Class<?>, Set<Class<?>>> transitiveDependenciesByImported,
            List<Subpackage> subpackages) {
        transitiveDependenciesByImported.forEach((importedModule, importingModules) -> {

            subpackages.forEach(subpackage -> {

                val localModule = asArray(subpackage.mayBeAccessedBySubpackagesInSameModule(), subpackages);
                val otherModules = asArray(subpackage.mayBeAccessedBySubpackagesInReferencingModules(), subpackages);

                final String moduleName = nameOf(importedModule, subpackage.getName());
                val localModulePackageNames = namesOf(importedModule, localModule);
                val importingModulePackageNames = namesOf(importingModules, otherModules);
                val accessingModules = both(localModulePackageNames, importingModulePackageNames);
                if(accessingModules.length > 0) {
                    layeredArchitecture
                            .whereLayer(moduleName)
                            .mayOnlyBeAccessedByLayers(accessingModules);

                } else {
                    layeredArchitecture
                            .whereLayer(moduleName)
                            .mayNotBeAccessedByAnyLayer();
                }
            });

        });
    }

    private static String[] asArray(
            final List<String> strings,
            final List<Subpackage> subpackages) {
        String[] otherModules;
        if (strings.size() == 1 && strings.get(0).equals("*")) {
            otherModules = subpackages.stream().map(Subpackage::getName).collect(Collectors.toList()).toArray(new String[]{});
        }else {
            otherModules = strings.toArray(new String[] {});
        }
        return otherModules;
    }


    static String nameOf(Class<?> moduleClass, @Nullable final String subpackageName) {
        val simpleName = moduleClass.getSimpleName();
        val moduleName = simpleName.replace("Module", "");
        return moduleName + (subpackageName != null ? (" " + subpackageName) : "");
    }

    static String[] namesOf(Class<?> moduleClass, String... subpackageNames) {
        val names = Arrays.stream(subpackageNames)
                .map(subpackageName -> nameOf(moduleClass, subpackageName))
                .collect(Collectors.toList());
        return names.toArray(new String[] {});
    }

    static String[] namesOf(Set<Class<?>> importingClasses, String... subpackageNames) {
        val names = new ArrayList<String>();
        importingClasses.forEach(importingClass -> {
            Stream.of(subpackageNames).forEach(subpackageName ->
                    names.add(nameOf(importingClass, subpackageName)));
        });
        return names.toArray(new String[] {});
    }

    static String packageIdentifierFor(Class<?> moduleClass) {
        return packageIdentifierFor(moduleClass, null);
    }

    static String packageIdentifierFor(Class<?> moduleClass, @Nullable Subpackage subpackage) {
        return moduleClass.getPackage().getName() +
                (subpackage != null ? subpackage.packageIdentifier(): "..");
    }

    static String[] both(String str, String[] arr) {
        val strings = new ArrayList<String>();
        strings.add(str);
        strings.addAll(Arrays.asList(arr));
        return strings.toArray(new String[] {});
    }

    static String[] both(String[] arr1, String[] arr2) {
        val strings = new ArrayList<String>();
        strings.addAll(Arrays.asList(arr1));
        strings.addAll(Arrays.asList(arr2));
        return strings.toArray(new String[] {});
    }

    static <T> Map<T, Set<T>> invert(Map<T, Set<T>> valueSetByKey) {
        val inverted = new LinkedHashMap<T, Set<T>>();
        valueSetByKey.forEach((key, values) ->
                values.forEach(value -> {
                    val keySet = inverted.computeIfAbsent(value, k -> new LinkedHashSet<>());
                    keySet.add(key);
                }));
        return inverted;
    }

    static void accumulateTransitiveDependencies(
            final Class<?> referringClass,
            final Map<Class<?>, Set<Class<?>>> directDependenciesByReferringClass,
            final Set<Class<?>> transitiveDependenciesOfReferringClass) {
        val directDependencies = directDependenciesByReferringClass
                .getOrDefault(referringClass, Collections.emptySet());
        transitiveDependenciesOfReferringClass.addAll(directDependencies);
        directDependencies.forEach(directDependency ->
                accumulateTransitiveDependencies(directDependency, directDependenciesByReferringClass,
                        transitiveDependenciesOfReferringClass));
    }
}