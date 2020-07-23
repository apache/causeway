package demoapp.dom.annotDomain.Property.regexPattern;

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
public class PropertyRegexPatternVm_updateWithParameterLayout {

    private final PropertyRegexPatternVm propertyRegexPatternVm;

//tag::annotation[]
    public PropertyRegexPatternVm act(
            @Parameter(
                regexPattern = "^[^@+]@[^\\.+]\\.com$"                  // <.>
            )
            @ParameterLayout(
                describedAs =
                    "@Parameter(regexPattern = \"^[^@+]@[^\\.+]\\.com$\")"
            )
            final String emailAddressParameterUsingAnnotation) {
        propertyRegexPatternVm.setEmailAddressPropertyUsingAnnotation(emailAddressParameterUsingAnnotation);
        return propertyRegexPatternVm;
    }
//end::annotation[]
    public String default0Act() {
        return propertyRegexPatternVm.getEmailAddressPropertyUsingAnnotation();
    }

}
