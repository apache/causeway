package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.Locale;

import javax.persistence.Column;

import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;

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
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
})
@Retention(RetentionPolicy.RUNTIME)
@Domain.Include // meta annotation, in support of meta-model validation
public @interface ValueSemantics {

    /**
     * Allows to select {@link ValueSemanticsProvider}(s) by qualifier.
     *
     * @apiNote the selection (qualifier inclusion/exclusion) mechanics could be improved,
     * yet a single qualifier declared here must exactly match that of the targeted bean
     */
    String provider()
            default "";

    // -- NUMBER CONSTRAINTS

    /**
     * EXPERIMENTAL - considered to be moved to a separate {@code @Digits} annotation<p>
     * If associated with a {@link Number}, the maximum number of total digits accepted for
     * this number.<br>
     * Can be omitted, if {@link Column#precision()} is used.<br>
     * default = {@code 65}
     * @apiNote SQL's DECIMAL(precision, scale) has max-precision=65 and max-scale=30
     * @see Column#precision()
     */
    int maxTotalDigits()
            default 65;

    /**
     * EXPERIMENTAL - considered to be moved to a separate {@code @Digits} annotation<p>
     * If associated with a {@link Number}, the minimum number of integer digits required for
     * this number.<br>
     * default = {@code 1}
     */
    int minIntegerDigits()
            default 1;

    /**
     * EXPERIMENTAL - considered to be moved to a separate {@code @Digits} annotation<p>
     * If associated with a {@link BigDecimal}, the maximum number of fractional digits accepted
     * for this number.<br>
     * Can be omitted, if {@link Column#scale()} is used.<br>
     * default = {@code 30}
     * @apiNote SQL's DECIMAL(precision, scale) has max-precision=65 and max-scale=30
     * @see Column#scale()
     */
    int maxFractionalDigits()
            default 30;

    /**
     * EXPERIMENTAL - considered to be moved to a separate {@code @Digits} annotation<p>
     * If associated with a {@link BigDecimal}, the minimum number of fractional digits
     * required for this number.<br>
     * default = {@code 0}
     */
    int minFractionalDigits()
            default 0;

}
