package org.apache.isis.testing.archtestsupport.applib.entity.jpa;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.entity.jpa.dom.JpaDomModule;

import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_injected_field_of_jpa_Entity_must_be_annotated_with_Transient;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_be_annotated_as_Table_with_schema;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_be_annotated_as_Table_with_uniqueConstraints;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_be_annotated_as_an_IsisEntityListener;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_be_annotated_with_DomainObject_nature_of_ENTITY;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_be_annotated_with_XmlJavaAdapter_of_PersistentEntityAdapter;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_have_a_version_field;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_have_an_id_field;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_have_protected_no_arg_constructor;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureJpaRules.every_jpa_Entity_must_implement_Comparable;

@AnalyzeClasses(
    packagesOf = {
      JpaDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class JpaEntityArchTests {

  @ArchTest
  public static ArchRule every_jpa_Entity_must_be_annotated_as_an_IsisEntityListener =
      every_jpa_Entity_must_be_annotated_as_an_IsisEntityListener();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_also_implement_Comparable =
      every_jpa_Entity_must_implement_Comparable();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_be_annotated_with_DomainObject_nature_of_ENTITY =
      every_jpa_Entity_must_be_annotated_with_DomainObject_nature_of_ENTITY();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_be_annotated_with_XmlJavaAdapter_of_PersistentEntityAdapter =
      every_jpa_Entity_must_be_annotated_with_XmlJavaAdapter_of_PersistentEntityAdapter();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_be_annotated_as_Table_with_uniqueConstraints =
      every_jpa_Entity_must_be_annotated_as_Table_with_uniqueConstraints();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_be_annotated_as_Table_with_schema =
      every_jpa_Entity_must_be_annotated_as_Table_with_schema();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_have_an_id_field =
      every_jpa_Entity_must_have_an_id_field();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_have_a_version_field =
      every_jpa_Entity_must_have_a_version_field();

  @ArchTest
  public static ArchRule every_injected_field_of_jpa_Entity_must_be_annotated_with_Transient =
      every_injected_field_of_jpa_Entity_must_be_annotated_with_Transient();

  @ArchTest
  public static ArchRule every_jpa_Entity_must_have_protected_no_arg_constructor =
          every_jpa_Entity_must_have_protected_no_arg_constructor();

}
