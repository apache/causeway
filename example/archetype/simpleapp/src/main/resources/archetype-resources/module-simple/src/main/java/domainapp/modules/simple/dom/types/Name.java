#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package domainapp.modules.simple.dom.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jdo.annotations.Column;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.spec.AbstractSpecification2;

@Column(length = Name.MAX_LEN, allowsNull = "false")
@Property(editing = Editing.DISABLED, mustSatisfy = Name.NoExclamationMarks.class, maxLength = Name.MAX_LEN)
@Parameter(mustSatisfy = Name.NoExclamationMarks.class, maxLength = Name.MAX_LEN)
@ParameterLayout(named = "Name")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

    int MAX_LEN = 40;

    class NoExclamationMarks extends AbstractSpecification2<String> {

        @Override
        public TranslatableString satisfiesTranslatableSafely(final String name) {
            return name != null && name.contains("!")
                    ? TranslatableString.tr("Exclamation mark is not allowed")
                    : null;
        }
    }
}
