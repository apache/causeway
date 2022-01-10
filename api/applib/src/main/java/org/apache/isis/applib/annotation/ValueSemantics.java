package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.time.format.FormatStyle;
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
     * @apiNote a single qualifier declared here must exactly match that of the
     * targeted {@link ValueSemanticsProvider} bean
     */
    String provider()
        default "";

    // -- NUMBER CONSTRAINTS

    /**
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
     * If associated with a {@link Number}, the minimum number of integer digits required for
     * this number.<br>
     * default = {@code 1}
     */
    int minIntegerDigits()
        default 1;

    /**
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
     * If associated with a {@link BigDecimal}, the minimum number of fractional digits
     * required for this number.<br>
     * default = {@code 0}
     */
    int minFractionalDigits()
        default 0;

    // -- TEMPORAL FORMATTING

    /**
     * If associated with a temporal date value, the rendering style of a localized date.
     * @see FormatStyle
     */
    FormatStyle dateFormatStyle()
        default FormatStyle.MEDIUM;

    /**
     * If associated with a temporal time value, the rendering style of a localized time.
     * @see FormatStyle
     */
    FormatStyle timeFormatStyle()
        default FormatStyle.MEDIUM;

    /**
     * If associated with a temporal time value, the time of day precision,
     * used for editing a time field in the UI.<br>
     * default = {@link TimePrecision#SECOND}
     * @see TimePrecision
     */
    TimePrecision timePrecision()
        default TimePrecision.SECOND;

    /**
     * eg. &#64;ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)
     */
    public final static int AS_DAY_BEFORE = -1;

    /**
     * If associated with a date or date-time value,
     * instructs whether the date should be rendered as <i>n</i> days
     * after the actually stored date.
     * For negative <i>n</i> its days before respectively.
     *
     * <p>
     * This is intended to be used so that an exclusive end date of an interval
     * can be rendered as 1 day before the actual value stored.
     * </p>
     *
     * <p>
     * For example:
     * </p>
     * <pre>
     * public LocalDate getStartDate() { ... }
     *
     * &#64;ValueSemantics(dateRenderAdjustDays = ValueSemantics.AS_DAY_BEFORE)
     * public LocalDate getEndDate() { ... }
     * </pre>
     *
     * <p>
     * Here, the interval of the [1-may-2013,1-jun-2013) would be rendered as the dates
     * 1-may-2013 for the start date but using 31-may-2013 (the day before) for the end date.  What is stored
     * In the domain object, itself, however, the value stored is 1-jun-2013.
     * </p>
     */
    int dateRenderAdjustDays()
        default 0;
}
