package org.apache.isis.testing.archtestsupport.applib.entity.jdo;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJdoRules;
import org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom.JdoDomModule;

@AnalyzeClasses(
    packagesOf = {
      JdoDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class JdoEntityArchTests {

  @ArchTest
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_implement_Comparable =
      ArchitectureJdoRules.classes_annotated_with_PersistenceCapable_must_also_implement_Comparable();

  @ArchTest
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_DomainObject_nature_of_ENTITY =
      ArchitectureJdoRules.classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_DomainObject_nature_of_ENTITY();

  @ArchTest
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_XmlJavaAdapter_PersistentEntityAdapter =
      ArchitectureJdoRules.classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_XmlJavaAdapter_PersistentEntityAdapter();

  @ArchTest
  public static ArchRule classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_Uniques_or_Unique =
      ArchitectureJdoRules.classes_annotated_with_PersistenceCapable_must_also_be_annotated_with_Uniques_or_Unique();

}
