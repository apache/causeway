package demoapp.dom.annotLayout.PropertyLayout.multiLine;

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
            @MultiLine10MetaAnnotation                            // <.>
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                multiLine = 3                                   // <.>
                , describedAs =
                    "@MultiLine10MetaAnnotation " +
                    "@ParameterLayout(multiLine = 3)"
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
