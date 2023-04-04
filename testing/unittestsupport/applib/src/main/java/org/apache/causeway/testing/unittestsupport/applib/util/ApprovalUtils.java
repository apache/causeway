package org.apache.causeway.testing.unittestsupport.applib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.approvaltests.reporters.GenericDiffReporter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApprovalUtils {

    /**
     * Enables approval testing's text compare for given file extension.
     * @param ext - should include the leading dot '.' like in say {@code .yaml}
     */
    public void registerFileExtensionForTextCompare(final String ext) {
        if(GenericDiffReporter.TEXT_FILE_EXTENSIONS.contains(ext)) {
            return; // nothing to do
        }
        final List<String> textFileExtensions = new ArrayList<>(GenericDiffReporter.TEXT_FILE_EXTENSIONS);
        textFileExtensions.add(ext);
        GenericDiffReporter.TEXT_FILE_EXTENSIONS = Collections.unmodifiableList(textFileExtensions);
    }

}
