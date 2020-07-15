package demoapp.dom.annotations.PropertyLayout.multiLine;

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
public class PropertyLayoutMultiLineVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutMultiLineVm propertyLayoutMultiLineVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutMultiLineVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @MultiLineMetaAnnotation                            // <.>
            @ParameterLayout(
                multiLine = 3                                   // <.>
                , describedAs =
                    "@MultiLineMetaAnnotation @ParameterLayout(...)"
            )
            final String parameterUsingMetaAnnotationButOverridden) {
        propertyLayoutMultiLineVm.setPropertyUsingMetaAnnotationButOverridden(parameterUsingMetaAnnotationButOverridden);
        return propertyLayoutMultiLineVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutMultiLineVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
