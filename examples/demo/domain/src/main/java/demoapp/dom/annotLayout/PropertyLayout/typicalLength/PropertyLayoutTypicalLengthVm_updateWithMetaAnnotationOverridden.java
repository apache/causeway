package demoapp.dom.annotLayout.PropertyLayout.typicalLength;

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
public class PropertyLayoutTypicalLengthVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutTypicalLengthVm propertyLayoutTypicalLengthVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutTypicalLengthVm act(
            @TypicalLengthMetaAnnotation                            // <.>
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                typicalLength = 3                                   // <.>
                , describedAs =
                    "@TypicalLengthMetaAnnotation @ParameterLayout(...)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyLayoutTypicalLengthVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyLayoutTypicalLengthVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutTypicalLengthVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
