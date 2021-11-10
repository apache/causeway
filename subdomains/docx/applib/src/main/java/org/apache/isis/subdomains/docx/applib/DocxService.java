package org.apache.isis.subdomains.docx.applib;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.apache.isis.subdomains.docx.applib.exceptions.LoadInputException;
import org.apache.isis.subdomains.docx.applib.exceptions.LoadTemplateException;
import org.apache.isis.subdomains.docx.applib.exceptions.MergeException;

import lombok.Builder;
import lombok.Getter;

/**
 * Merges input HTML against a provided <i>docx</i> template, generating a <i>Word docx</i>.
 *
 * @since 2.x {@index}
 */
public interface DocxService {

    /**
     * Load and return an in-memory representation of a docx.
     *
     * <p>
     * This is public API because building the in-memory structure can be
     * quite slow.  Thus, clients can use this method to cache the in-memory
     * structure, and pass it in the {@link MergeParams} (through the
     * {@link MergeParams.Builder#docxTemplateAsWpMlPackage(WordprocessingMLPackage) builder method})
     */
    WordprocessingMLPackage loadPackage(InputStream docxTemplate) throws LoadTemplateException;

    /**
     * Merge the input arguments (as HTML) against the Docx template, writing out as a Word docx..
     */
    void merge(MergeParams mergeDefn) throws LoadInputException, LoadTemplateException, MergeException;

    /**
     * @since 2.x {@index}
     */
    @Getter
    @Builder(builderClassName = "Builder")
    static class MergeParams {

        /**
         * Defines the policy for matching input to placeholders.
         *
         * <p>
         *     Does not need to be specified, will default to {@link MatchingPolicy#STRICT strict}
         * </p>
         */
        @lombok.Builder.Default private MatchingPolicy matchingPolicy = MatchingPolicy.STRICT;

        /**
         * Defines whether to output as Word docx or PDF.
         *
         * <p>
         *     Does not need to be specified, will default to {@link OutputType#DOCX docx}.
         * </p>
         */
        @lombok.Builder.Default private OutputType outputType = OutputType.DOCX;

        /**
         * Holds the input arguments to be merged into the template.
         *
         * <p>
         * Either this or {@link #getInputAsHtmlDoc()} must be specified.
         * Preference is given to {@link #getInputAsHtmlDoc()}.
         * </p>
         *
         * @see #getInputAsHtmlDoc()
         */
        private String inputAsHtml;

        /**
         * Holds the input arguments to be merged into the template.
         *
         * <p>
         * Either this or {@link #getInputAsHtml()} must be specified, with
         * preference given to this.
         * </p>
         *
         * @see #getInputAsHtml()
         */
        private org.w3c.dom.Document inputAsHtmlDoc;

        /**
         * Refers to the template with place holders to be merged into.
         *
         * <p>
         *     Either this or {@link #getDocxTemplateAsWpMlPackage()} myst be
         *     specified, with preference given to {@link #getDocxTemplateAsWpMlPackage()}
         * </p>
         *
         * @see #getDocxTemplateAsWpMlPackage()
         */
        private InputStream docxTemplate;

        /**
         * Refers to the template with place holders to be merged into.
         *
         * <p>
         *     Either this or {@link #getDocxTemplate()} myst be
         *     specified, with preference given to this.
         * </p>
         *
         * @see #getDocxTemplate()
         */
        private WordprocessingMLPackage docxTemplateAsWpMlPackage;

        /**
         * The output stream to write to.
         */
        private OutputStream output;

    }

    /**
     * Defines the strategy as to whether placeholders must exactly input data
     * (or whether there can be unmatched placeholders, or conversely unused input data).
     */
    enum MatchingPolicy {
        STRICT(false, false),
        ALLOW_UNMATCHED_INPUT(true, false),
        ALLOW_UNMATCHED_PLACEHOLDERS(false, true),
        /**
         * Combination of both {@link #ALLOW_UNMATCHED_INPUT} and {@link #ALLOW_UNMATCHED_PLACEHOLDERS}.
         */
        LAX(true, true);
        private final boolean allowUnmatchedInput;
        private final boolean allowUnmatchedPlaceholders;

        private MatchingPolicy(final boolean allowUnmatchedInput, final boolean allowUnmatchedPlaceholders) {
            this.allowUnmatchedInput = allowUnmatchedInput;
            this.allowUnmatchedPlaceholders = allowUnmatchedPlaceholders;
        }

        public void unmatchedInputs(final List<String> unmatched) throws MergeException {
            if (!allowUnmatchedInput && !unmatched.isEmpty()) {
                throw new MergeException("Input elements " + unmatched + " were not matched to placeholders");
            }
        }

        public void unmatchedPlaceholders(final List<String> unmatched) throws MergeException {
            if (!allowUnmatchedPlaceholders && !unmatched.isEmpty()) {
                throw new MergeException("Placeholders " + unmatched + " were not matched to input");
            }
        }
    }

    /**
     * The type of the file to generate
     */
    enum OutputType {
        DOCX,
        /**
         * Support for PDF should be considered experimental.
         */
        PDF
    }

}