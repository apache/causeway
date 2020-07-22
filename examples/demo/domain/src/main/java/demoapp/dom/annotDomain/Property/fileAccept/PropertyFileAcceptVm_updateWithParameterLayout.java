package demoapp.dom.annotDomain.Property.fileAccept;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Blob;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "propertyUsingAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyFileAcceptVm_updateWithParameterLayout {

    private final PropertyFileAcceptVm propertyFileAcceptVm;

//tag::annotation[]
    public PropertyFileAcceptVm act(
            @Parameter(
                fileAccept = "pdf"                                 // <.>
                , optionality = Optionality.OPTIONAL
            )
            @ParameterLayout(
                describedAs =
                    "@Parameter(fileAccept = \"pdf\")"
            )
            final Blob parameterUsingAnnotation) {
        propertyFileAcceptVm.setPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyFileAcceptVm;
    }
//end::annotation[]
    public Blob default0Act() {
        return propertyFileAcceptVm.getPropertyUsingAnnotation();
    }

}
