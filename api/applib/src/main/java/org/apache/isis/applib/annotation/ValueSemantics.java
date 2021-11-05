package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import org.apache.isis.applib.adapters.ValueSemanticsProvider;

/**
 * Collects all the value-type specific customization attributes.
 *
 * @apiNote These are understood to be agnostic to the actual {@link Locale} in use.
 *
 * @see Action
 * @see Collection
 * @see Property
 * @see Parameter
 *
 * @since 2.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Domain.Include // meta annotation, in support of meta-model validation
public @interface ValueSemantics {

    /**
     * <p>
     * Allows to select {@link ValueSemanticsProvider}(s) by qualifier.
     *
     * @apiNote the selection (qualifier inclusion/exclusion) mechanics is not yet finalized,
     * currently a single qualifier declared here must exactly match that of the targeted bean
     */
    String provider()
            default "";

}
