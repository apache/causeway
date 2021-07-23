package org.apache.isis.testing.archtestsupport.applib.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules;
import org.apache.isis.testing.archtestsupport.applib.domain.dom.DomainDomModule;

@AnalyzeClasses(
    packagesOf = {
      DomainDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class DomainArchTests {

  @ArchTest
  public static ArchRule every_DomainObject_must_also_be_annotated_with_DomainObjectLayout =
      ArchitectureDomainRules.every_DomainObject_must_also_be_annotated_with_DomainObjectLayout();

  @ArchTest
  public static ArchRule every_DomainObject_must_specify_logicalTypeName =
      ArchitectureDomainRules.every_DomainObject_must_specify_logicalTypeName();

  @ArchTest
  public static ArchRule every_DomainService_must_also_be_annotated_with_DomainServiceLayout =
      ArchitectureDomainRules.every_DomainService_must_also_be_annotated_with_DomainServiceLayout();

  @ArchTest
  public static ArchRule every_DomainService_must_specify_logicalTypeName =
      ArchitectureDomainRules.every_DomainService_must_specify_logicalTypeName();

  @ArchTest
  public static ArchRule every_Repository_must_be_follow_naming_conventions =
      ArchitectureDomainRules.every_Repository_must_be_follow_naming_conventions();

  @ArchTest
  public static ArchRule every_Controller_must_be_follow_naming_conventions =
      ArchitectureDomainRules.every_Controller_must_be_follow_naming_conventions();

  @ArchTest
  public static ArchRule every_class_named_Repository_must_be_annotated_correctly =
      ArchitectureDomainRules.every_class_named_Repository_must_be_annotated_correctly();

  @ArchTest
  public static ArchRule every_class_named_Controller_must_be_annotated_correctly =
      ArchitectureDomainRules.every_class_named_Controller_must_be_annotated_correctly();

  @ArchTest
  public static ArchRule every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient =
      ArchitectureDomainRules.every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient();

  @ArchTest
  public static ArchRule every_injected_field_of_serializable_view_model_must_be_transient =
      ArchitectureDomainRules.every_injected_field_of_serializable_view_model_must_be_transient();

  @ArchTest
  public static ArchRule every_finder_method_in_Repository_must_return_either_Collection_or_Optional =
      ArchitectureDomainRules.every_finder_method_in_Repository_must_return_either_Collection_or_Optional();

}
