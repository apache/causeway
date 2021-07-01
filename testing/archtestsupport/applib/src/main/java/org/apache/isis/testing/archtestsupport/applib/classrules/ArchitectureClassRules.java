package org.apache.isis.testing.archtestsupport.applib.classrules;

import javax.persistence.Entity;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArchitectureClassRules {

  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_DomainObject() {
    return classes()
          .that().areAnnotatedWith(Entity.class)
          .should().beAnnotatedWith(DomainObject.class);
  }

  public static ArchRule classes_annotated_with_Entity_must_also_be_annotated_with_XmlJavaAdapter() {
    return classes()
      .that().areAnnotatedWith(Entity.class)
      .should().beAnnotatedWith(XmlJavaTypeAdapter.class);
  }

  public static ArchRule classes_annotated_with_DomainObject_must_also_be_annotated_with_DomainObjectLayout() {
    return classes()
      .that().areAnnotatedWith(DomainObject.class)
      .should().beAnnotatedWith(DomainObjectLayout.class);
  }
}
