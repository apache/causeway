/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testing.archtestsupport.applib.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.domain.dom.DomainDomModule;

import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_Action_mixin_must_follow_naming_convention;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_Collection_mixin_must_follow_naming_convention;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_Controller_must_be_follow_naming_conventions;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_DomainObject_must_also_be_annotated_with_DomainObjectLayout;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_DomainObject_must_specify_logicalTypeName;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_DomainService_must_also_be_annotated_with_DomainServiceLayout;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_DomainService_must_specify_logicalTypeName;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_Property_mixin_must_follow_naming_convention;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_Repository_must_follow_naming_conventions;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_class_named_Controller_must_be_annotated_correctly;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_class_named_Repository_must_be_annotated_correctly;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_finder_method_in_Repository_must_return_either_Collection_or_Optional;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient;
import static org.apache.isis.testing.archtestsupport.applib.classrules.ArchitectureDomainRules.every_injected_field_of_serializable_view_model_must_be_transient;

@AnalyzeClasses(
    packagesOf = {
      DomainDomModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class DomainArchTests {

  @ArchTest
  public static ArchRule every_DomainObject_must_also_be_annotated_with_DomainObjectLayout =
      every_DomainObject_must_also_be_annotated_with_DomainObjectLayout();

  @ArchTest
  public static ArchRule every_DomainObject_must_specify_logicalTypeName =
      every_DomainObject_must_specify_logicalTypeName();

  @ArchTest
  public static ArchRule every_DomainService_must_also_be_annotated_with_DomainServiceLayout =
      every_DomainService_must_also_be_annotated_with_DomainServiceLayout();

  @ArchTest
  public static ArchRule every_DomainService_must_specify_logicalTypeName =
      every_DomainService_must_specify_logicalTypeName();

  @ArchTest
  public static ArchRule every_Repository_must_be_follow_naming_conventions =
      every_Repository_must_follow_naming_conventions();

  @ArchTest
  public static ArchRule every_Controller_must_be_follow_naming_conventions =
      every_Controller_must_be_follow_naming_conventions();

  @ArchTest
  public static ArchRule every_class_named_Repository_must_be_annotated_correctly =
      every_class_named_Repository_must_be_annotated_correctly();

  @ArchTest
  public static ArchRule every_class_named_Controller_must_be_annotated_correctly =
      every_class_named_Controller_must_be_annotated_correctly();

  @ArchTest
  public static ArchRule every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient =
      every_injected_field_of_jaxb_view_model_must_be_annotated_with_XmlTransient();

  @ArchTest
  public static ArchRule every_injected_field_of_serializable_view_model_must_be_transient =
      every_injected_field_of_serializable_view_model_must_be_transient();

  @ArchTest
  public static ArchRule every_finder_method_in_Repository_must_return_either_Collection_or_Optional =
      every_finder_method_in_Repository_must_return_either_Collection_or_Optional();

  @ArchTest
  public static ArchRule every_Action_mixin_must_follow_naming_convention =
      every_Action_mixin_must_follow_naming_convention();

  @ArchTest
  public static ArchRule every_Property_mixin_must_follow_naming_convention =
      every_Property_mixin_must_follow_naming_convention();

  @ArchTest
  public static ArchRule every_Collection_mixin_must_follow_naming_convention =
      every_Collection_mixin_must_follow_naming_convention();

}
