package demoapp.dom.annotDomain.Property.regexPattern;

import java.util.regex.Pattern;

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
public class PropertyRegexPatternVm_updateWithMetaAnnotationOverridden {

    private final PropertyRegexPatternVm propertyRegexPatternVm;

//tag::meta-annotation-overridden[]
    public PropertyRegexPatternVm act(
            @Parameter(
                regexPattern = "^[^@+]@[^\\.+]\\.org$"          // <.>
                , regexPatternReplacement = "Must be .org email address"    // <.>
                , regexPatternFlags = Pattern.COMMENTS                      // <.>
                , optionality = Optionality.OPTIONAL
            )
            @RegexPatternMetaAnnotation                         // <.>
            @ParameterLayout(
                describedAs =
                    "@RegexPatternMetaAnnotation @ParameterLayout(...)"
            )
            final String emailAddressParameterUsingMetaAnnotationButOverridden) {
        propertyRegexPatternVm.setEmailAddressPropertyUsingMetaAnnotationButOverridden(emailAddressParameterUsingMetaAnnotationButOverridden);
        return propertyRegexPatternVm;
    }
//end::meta-annotation-overridden[]
    public String default0Act() {
        return propertyRegexPatternVm.getEmailAddressPropertyUsingMetaAnnotationButOverridden();
    }

}
