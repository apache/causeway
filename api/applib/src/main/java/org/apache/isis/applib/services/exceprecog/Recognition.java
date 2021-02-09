package org.apache.isis.applib.services.exceprecog;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.i18n.TranslationService;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * Represents a user-friendly representation of an exception that has been
 * recognised by an available implementation of an {@link ExceptionRecognizer}.
 *
 * <p>
 * Returned by {@link ExceptionRecognizer#recognize(Throwable)} when the
 * exception recognizer has recognised the exception
 */
@Value
public class Recognition {

    /**
     * @return optionally a recognition of the specified type, based on a whether given reason is non-null
     */
    public static Optional<Recognition> of(
            @Nullable final Category category,
            @Nullable final String reason) {

        if (reason == null) {
            return Optional.empty();
        }

        val nonNullCategory = category != null ? category : Category.OTHER;
        return Optional.of(new Recognition(nonNullCategory, reason));
        // ...
    }

    /**
     * Categorises the exception as per {@link Category}.
     *
     * <p>
     *     This category can also optionally be used in the translation of the
     *     {@link #getReason() reason} for the exception.
     * </p>
     *
     * @see #toMessage(TranslationService)
     */
    @NonNull
    Category category;

    /**
     * The untranslated user-friendly reason for the exception.
     *
     * <p>
     *     The reason can also be translated (prepended or not by the
     *     translation of the {@link #getCategory() category} using
     *     {@link #toMessage(TranslationService)} or
     *     {@link #toMessageNoCategory(TranslationService)}.
     *     .
     * </p>
     *
     * @see #toMessage(TranslationService)
     * @see #toMessageNoCategory(TranslationService)
     */
    @NonNull
    String reason;

    /**
     * Translates the {@link #getReason() reason} and prepends with a
     * translation of {@link #getCategory() category}, using the provided
     * {@link TranslationService}..
     *
     * @param translationService
     * @return
     */
    public String toMessage(@Nullable TranslationService translationService) {

        val categoryLiteral = translate(getCategory().getFriendlyName(), translationService);
        val reasonLiteral = translate(getReason(), translationService);

        return String.format("[%s]: %s", categoryLiteral, reasonLiteral);
        // ...
    }

    /**
     * Translates the {@link #getReason() reason} alone (ignoring the
     * {@link #getCategory() category}, using the provided
     * {@link TranslationService}..
     *
     * @param translationService
     * @return
     */
    public String toMessageNoCategory(@Nullable TranslationService translationService) {

        val reasonLiteral = translate(getReason(), translationService);
        return String.format("%s", reasonLiteral);
        // ...
    }

    private static String translate(
            @Nullable String x,
            @Nullable TranslationService translationService) {
        if (x == null || translationService == null) {
            return x;
        }
        return translationService.translate(
                Recognition.class.getName(), x);
    }

}
