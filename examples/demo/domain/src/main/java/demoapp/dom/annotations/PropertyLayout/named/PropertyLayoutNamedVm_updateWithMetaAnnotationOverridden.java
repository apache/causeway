package demoapp.dom.annotations.PropertyLayout.named;

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
public class PropertyLayoutNamedVm_updateWithMetaAnnotationOverridden {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::meta-annotation-overridden[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @NamedMetaAnnotation                                // <.>
            @ParameterLayout(
                named = "@ParameterLayout name " +
                            "overrides meta-annotation"         // <.>
                , describedAs =
                    "@NamedMetaAnnotation @ParameterLayout(...)"
            )
            final String newValue) {
        propertyLayoutNamedVm.setPropertyUsingMetaAnnotationButOverridden(newValue);
        return propertyLayoutNamedVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingMetaAnnotationButOverridden();
    }

}
