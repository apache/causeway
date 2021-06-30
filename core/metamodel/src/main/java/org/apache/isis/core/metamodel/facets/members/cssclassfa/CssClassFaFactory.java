package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;

/**
 * @since 2.0
 */
public interface CssClassFaFactory {

    /**
     * Position of <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> icon.
     */
    CssClassFaPosition getPosition();

    Stream<String> streamCssClasses();

    /**
     * Space separated (distinct) CSS-class strings.
     */
    default String asSpaceSeparated() {
        return streamCssClasses()
                .collect(Collectors.joining(" "));
    }

    /**
     * Space separated (distinct) CSS-class strings.
     * @param additionalClasses - trimmed and filtered by non-empty, then added to the resulting string
     */
    default String asSpaceSeparatedWithAdditional(String ... additionalClasses) {

        if(_NullSafe.size(additionalClasses)==0) {
            return asSpaceSeparated();
        }

        return Stream.concat(
                streamCssClasses(),
                _NullSafe.stream(additionalClasses)
                    .map(String::trim)
                    .filter(_Strings::isNotEmpty))
        .distinct()
        .collect(Collectors.joining(" "));

    }

}
