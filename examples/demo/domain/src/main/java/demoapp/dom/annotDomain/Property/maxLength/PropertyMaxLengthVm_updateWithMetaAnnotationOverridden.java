package demoapp.dom.annotDomain.Property.maxLength;

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
public class PropertyMaxLengthVm_updateWithMetaAnnotationOverridden {

    private final PropertyMaxLengthVm propertyMaxLengthVm;

//tag::meta-annotation-overridden[]
    public PropertyMaxLengthVm act(
            @Parameter(
                maxLength = 3                                   // <.>
                , optionality = Optionality.OPTIONAL
            )
            @MaxLengthMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs =
                    "@MaxLengthMetaAnnotation @ParameterLayout(...)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyMaxLengthVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyMaxLengthVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyMaxLengthVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
