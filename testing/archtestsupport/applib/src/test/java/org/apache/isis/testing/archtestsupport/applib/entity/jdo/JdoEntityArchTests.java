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
  public static ArchRule every_jdo_PersistenceCapable_must_implement_Comparable =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_implement_Comparable();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_with_DomainObject_nature_of_ENTITY =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_be_annotated_with_DomainObject_nature_of_ENTITY();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_as_XmlJavaAdapter_PersistentEntityAdapter =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_be_annotated_as_XmlJavaAdapter_PersistentEntityAdapter();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_as_Uniques_or_Unique =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_be_annotated_as_Uniques_or_Unique();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_have_schema =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_have_schema();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_be_annotated_with_Version =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_be_annotated_with_Version();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_with_DATASTORE_identityType_must_be_annotated_as_DataStoreIdentity =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_with_DATASTORE_identityType_must_be_annotated_as_DataStoreIdentity();

  @ArchTest
  public static ArchRule every_injected_field_of_jdo_PersistenceCapable_must_be_annotated_with_NotPersistent =
      ArchitectureJdoRules.every_injected_field_of_jdo_PersistenceCapable_must_be_annotated_with_NotPersistent();

  @ArchTest
  public static ArchRule every_jdo_PersistenceCapable_must_have_protected_no_arg_constructor =
      ArchitectureJdoRules.every_jdo_PersistenceCapable_must_have_protected_no_arg_constructor();

}
