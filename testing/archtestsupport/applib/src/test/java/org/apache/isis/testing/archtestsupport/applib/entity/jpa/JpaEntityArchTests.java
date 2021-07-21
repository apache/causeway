package org.apache.isis.testing.archtestsupport.applib.entity.jpa;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules;
import org.apache.isis.testing.archtestsupport.applib.entity.jpa.dom.JpaDomModule;

@AnalyzeClasses(
    packagesOf = {
      JpaDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class JpaEntityArchTests {

  @ArchTest
  public static ArchRule classes_annotated_with_Entity_must_also_be_an_IsisEntityListener =
      ArchitectureJpaRules.classes_annotated_with_Entity_must_also_be_an_IsisEntityListener();

  @ArchTest
  public static ArchRule classes_annotated_with_Entity_must_also_implement_Comparable =
      ArchitectureJpaRules.classes_annotated_with_Entity_must_also_implement_Comparable();

  @ArchTest
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_DomainObject_nature_of_ENTITY =
      ArchitectureJpaRules.classes_annotated_with_Entity_must_also_be_annotated_with_DomainObject_nature_of_ENTITY();

  @ArchTest
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_XmlJavaAdapter_PersistentEntityAdapter =
      ArchitectureJpaRules.classes_annotated_with_Entity_must_also_be_annotated_with_XmlJavaAdapter_PersistentEntityAdapter();

  @ArchTest
  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_Table_with_uniqueConstraints =
      ArchitectureJpaRules.classes_annotated_with_Entity_must_also_be_annotated_with_Table_with_uniqueConstraints();

}
