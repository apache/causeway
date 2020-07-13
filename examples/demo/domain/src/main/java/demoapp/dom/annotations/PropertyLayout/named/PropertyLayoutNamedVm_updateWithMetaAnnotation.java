package demoapp.dom.annotations.PropertyLayout.named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingMetaAnnotation", associateWithSequence = "1"
    // ,hidden = Where.EVERYWHERE // TODO: meta-annotations seem not to be picked up.
)
@RequiredArgsConstructor
public class PropertyLayoutNamedVm_updateWithMetaAnnotation {

    private final PropertyLayoutNamedVm propertyLayoutNamedVm;

//tag::meta-annotation[]
    public PropertyLayoutNamedVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @NamedMetaAnnotation                            // <.>
            final String newValue) {
        propertyLayoutNamedVm.setPropertyUsingMetaAnnotation(newValue);
        return propertyLayoutNamedVm;
    }
//end::meta-annotation[]
    public String default0Act() {
        return propertyLayoutNamedVm.getPropertyUsingMetaAnnotation();
    }

}
