package demoapp.dom.annotDomain.Property.mustSatisfy;

import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "customerAgePropertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyMustSatisfyVm_updateWithMetaAnnotationOverridden {

    private final PropertyMustSatisfyVm propertyMustSatisfyVm;

//tag::meta-annotation-overridden[]
    public PropertyMustSatisfyVm act(
            @Parameter(
                mustSatisfy = OfRetirementAgeSpecification.class            // <.>
            )
            @MustSatisfyOfWorkingAgeMetaAnnotation                         // <.>
            @ParameterLayout(
                describedAs =
                    "@MustSatisfyOfWorkingAgeMetaAnnotation " +
                    "@ParameterLayout(mustSatisfy = OfRetirementAgeSpecification.class)"
            )
            final Integer customerAgeParameterUsingMetaAnnotationButOverridden) {
        propertyMustSatisfyVm.setCustomerAgePropertyUsingMetaAnnotationButOverridden(customerAgeParameterUsingMetaAnnotationButOverridden);
        return propertyMustSatisfyVm;
    }
//end::meta-annotation-overridden[]
    public Integer default0Act() {
        return propertyMustSatisfyVm.getCustomerAgePropertyUsingMetaAnnotationButOverridden();
    }

}
