package demoapp.dom.annotDomain.Property.fileAccept;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.IDEMPOTENT,
    associateWith = "txtPropertyUsingAnnotation", associateWithSequence = "1"
)
@RequiredArgsConstructor
public class PropertyFileAcceptVm_updateClobWithParameterLayout {

    private final PropertyFileAcceptVm propertyFileAcceptVm;

//tag::annotation[]
    public PropertyFileAcceptVm act(
            @Parameter(
                fileAccept = ".txt"                     // <.>
                , optionality = Optionality.OPTIONAL
            )
            @ParameterLayout(
                describedAs =
                    "@Parameter(fileAccept = \".txt\")"
            )
            final Clob parameterUsingAnnotation) {
        propertyFileAcceptVm.setTxtPropertyUsingAnnotation(parameterUsingAnnotation);
        return propertyFileAcceptVm;
    }
//end::annotation[]
    public Clob default0Act() {
        return propertyFileAcceptVm.getTxtPropertyUsingAnnotation();
    }

}
