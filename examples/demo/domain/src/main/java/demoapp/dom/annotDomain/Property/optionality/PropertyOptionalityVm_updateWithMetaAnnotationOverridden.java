package demoapp.dom.annotDomain.Property.optionality;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotationButOverridden", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyOptionalityVm_updateWithMetaAnnotationOverridden {

    private final PropertyOptionalityVm propertyOptionalityVm;

//tag::meta-annotation-overridden[]
    public PropertyOptionalityVm act(
            @OptionalityOptionalMetaAnnotation                  // <.>
            @Parameter(
                optionality = Optionality.MANDATORY             // <.>
            )
            @ParameterLayout(
                describedAs =
                    "@OptionalityOptionalMetaAnnotation " +
                    "@ParameterLayout(optionality = MANDATORY)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyOptionalityVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyOptionalityVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyOptionalityVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
