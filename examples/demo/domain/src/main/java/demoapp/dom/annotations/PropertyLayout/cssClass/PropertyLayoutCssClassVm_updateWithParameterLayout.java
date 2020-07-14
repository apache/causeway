package demoapp.dom.annotations.PropertyLayout.cssClass;

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
public class PropertyLayoutCssClassVm_updateWithParameterLayout {

    private final PropertyLayoutCssClassVm propertyLayoutCssClassVm;

//tag::annotation[]
    public PropertyLayoutCssClassVm act(
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(
                cssClass = "red"                            // <.>
                , describedAs =
                    "@ParameterLayout(cssClass = \"red\")"
            )
            final String newValue) {
        propertyLayoutCssClassVm.setPropertyUsingAnnotation(newValue);
        return propertyLayoutCssClassVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyLayoutCssClassVm.getPropertyUsingAnnotation();
    }

}
