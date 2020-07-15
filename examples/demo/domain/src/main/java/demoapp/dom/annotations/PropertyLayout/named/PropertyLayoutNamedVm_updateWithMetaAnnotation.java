package demoapp.dom.annotations.PropertyLayout.named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutNamedVm_updateWithMetaAnnotation {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::meta-annotation[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @NamedMetaAnnotation                            // <.>
            @ParameterLayout(
                describedAs = "@NamedMetaAnnotation"
            )
            final String parameterUsingMetaAnnotation) {
        propertyLayoutNamedVm.setPropertyUsingMetaAnnotation(parameterUsingMetaAnnotation);
        return propertyLayoutNamedVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingMetaAnnotation();
    }

}
