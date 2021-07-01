package org.apache.isis.testing.archtestsupport.applib.modulerules;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.library.Architectures;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import lombok.val;
import lombok.experimental.UtilityClass;


@UtilityClass
public class ArchitectureModuleRules {

  /**
   * Utility method.
   *
   * @param clazz
   * @return
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
   */
  public static Architectures.LayeredArchitecture code_dependencies_follow_module_Imports(List<Class<?>> moduleClasses) {
    val layeredArchitecture = Architectures.layeredArchitecture();

    defineLayers(moduleClasses, layeredArchitecture);

    val directDependenciesByImported = new HashMap<Class<?>, Set<Class<?>>>();
    val directDependenciesByImporting = new HashMap<Class<?>, Set<Class<?>>>();
    computeDirectDependencies(moduleClasses, directDependenciesByImported, directDependenciesByImporting);

    val transitiveDependenciesByImporting = new HashMap<Class<?>, Set<Class<?>>>();
    computeTransitiveDependencies(moduleClasses, directDependenciesByImporting, transitiveDependenciesByImporting);

    val transitiveDependenciesByImported = invert(transitiveDependenciesByImporting);
    checkLayerAccess(layeredArchitecture, transitiveDependenciesByImported);

    val importingClassesNotImported = new LinkedHashSet<Class<?>>(transitiveDependenciesByImporting.keySet());
    importingClassesNotImported.removeAll(transitiveDependenciesByImported.keySet());

    checkNoAccessToTopmostLayers(layeredArchitecture, importingClassesNotImported);
    return layeredArchitecture;
  }

  private static void defineLayers(List<Class<?>> moduleClasses, Architectures.LayeredArchitecture layeredArchitecture) {
    moduleClasses.forEach(x -> {
      final String moduleName = nameOf(x);
      final String s = packageIdentifierFor(x);
      layeredArchitecture.layer(moduleName).definedBy(s);
    });
  }

  private static void computeDirectDependencies(List<Class<?>> moduleClasses, Map<Class<?>, Set<Class<?>>> directDependenciesByImported, Map<Class<?>, Set<Class<?>>> directDependenciesByImporting) {
    moduleClasses.forEach(
      moduleClass -> {
        final Import importAnnotation = AnnotationUtils.findAnnotation(moduleClass, Import.class);
        if (importAnnotation != null) {
          val importedClassesFromAnnot = importAnnotation.value();
          val importedClasses = new LinkedHashSet<>(Arrays.asList(importedClassesFromAnnot));
          directDependenciesByImporting.put(moduleClass, importedClasses);
          importedClasses.forEach(
            importedClass -> {
              val importingClasses = directDependenciesByImported.computeIfAbsent(importedClass, k -> new LinkedHashSet<>());
              importingClasses.add(moduleClass);
            }
          );
        }
      }
    );
  }

  private static void computeTransitiveDependencies(List<Class<?>> moduleClasses, Map<Class<?>, Set<Class<?>>> directDependenciesByImporting, Map<Class<?>, Set<Class<?>>> transitiveDependenciesByImporting) {
    moduleClasses.forEach((moduleClass) -> {
      val transitiveDependencies = new LinkedHashSet<Class<?>>();
      accumulateTransitiveDependencies(moduleClass, directDependenciesByImporting, transitiveDependencies);
      transitiveDependenciesByImporting.put(moduleClass, transitiveDependencies);
    });
  }

  private static void checkLayerAccess(Architectures.LayeredArchitecture layeredArchitecture, Map<Class<?>, Set<Class<?>>> transitiveDependenciesByImported) {
    transitiveDependenciesByImported.forEach((importedClass, importingClasses) -> {
      final String importedModuleName = nameOf(importedClass);
      final String[] importingModuleNames = namesOf(importingClasses);
      layeredArchitecture
        .whereLayer(importedModuleName)
        .mayOnlyBeAccessedByLayers(importingModuleNames);
    });
  }

  private static void checkNoAccessToTopmostLayers(Architectures.LayeredArchitecture layeredArchitecture, Set<Class<?>> importingClassesNotImported) {
    importingClassesNotImported.forEach(importingClass -> {
      final String importingModuleName = nameOf(importingClass);
      layeredArchitecture
        .whereLayer(importingModuleName)
        .mayNotBeAccessedByAnyLayer();
    });
  }

  static String nameOf(Class<?> moduleClass) {
    return moduleClass.getSimpleName();
  }

  static String packageIdentifierFor(Class<?> moduleClass) {
    return moduleClass.getPackage().getName() + "..";
  }

  static String[] namesOf(Set<Class<?>> importingClasses) {
    return importingClasses.stream()
      .map(ArchitectureModuleRules::nameOf)
      .collect(Collectors.toList())
      .toArray(new String[]{});
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
    final Class<?> referringClass
    , final Map<Class<?>, Set<Class<?>>> directDependenciesByReferringClass
    , final Set<Class<?>> transitiveDependenciesOfReferringClass) {
    val directDependencies = directDependenciesByReferringClass.getOrDefault(referringClass, Collections.emptySet());
    transitiveDependenciesOfReferringClass.addAll(directDependencies);
    directDependencies.forEach(directDependency ->
      accumulateTransitiveDependencies(directDependency, directDependenciesByReferringClass, transitiveDependenciesOfReferringClass));
  }

}
