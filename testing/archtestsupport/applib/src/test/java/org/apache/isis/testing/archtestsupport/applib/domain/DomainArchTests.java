package org.apache.isis.testing.archtestsupport.applib.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainObjectRules;
import org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainServiceRules;
import org.apache.isis.testing.archtestsupport.applib.domain.dom.DomainDomModule;

@AnalyzeClasses(
    packagesOf = {
      DomainDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class DomainArchTests {

  @ArchTest
  public static ArchRule classes_annotated_with_DomainObject_must_also_be_annotated_with_DomainObjectLayout =
      ArchitectureDomainObjectRules.classes_annotated_with_DomainObject_must_also_be_annotated_with_DomainObjectLayout();

  @ArchTest
  public static ArchRule classes_annotated_with_DomainObject_must_specify_logicalTypeName =
      ArchitectureDomainObjectRules.classes_annotated_with_DomainObject_must_specify_logicalTypeName();

  @ArchTest
  public static ArchRule classes_annotated_with_DomainService_must_also_be_annotated_with_DomainServiceLayout =
      ArchitectureDomainServiceRules.classes_annotated_with_DomainService_must_also_be_annotated_with_DomainServiceLayout();

  @ArchTest
  public static ArchRule classes_annotated_with_DomainService_must_specify_logicalTypeName =
      ArchitectureDomainServiceRules.classes_annotated_with_DomainService_must_specify_logicalTypeName();

}
