package demoapp.dom.annotations.PropertyLayout.describedAs;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyLayoutDescribedAsVm_updateWithParameterLayout {

    private final PropertyLayoutDescribedAsVm propertyLayoutDescribedAsVm;

//tag::annotation[]
    public PropertyLayoutDescribedAsVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                describedAs =
                    "@ParameterLayout(describedAs = \"...\")"   // <.>
            )
            final String newValue) {
        propertyLayoutDescribedAsVm.setPropertyUsingAnnotation(newValue);
        return propertyLayoutDescribedAsVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutDescribedAsVm.getPropertyUsingAnnotation();
    }

}
