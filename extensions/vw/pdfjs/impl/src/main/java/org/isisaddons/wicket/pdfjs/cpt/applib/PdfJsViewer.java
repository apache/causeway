package org.isisaddons.wicket.pdfjs.cpt.applib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.wicketstuff.pdfjs.Scale;

/**
 * An annotation that could be applied on a property or parameter
 * of type {@link org.apache.isis.applib.value.Blob}. Such property/parameter will be visualized
 * with <a href="https://github.com/mozilla/pdf.js">PDF.js</a> viewer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD})
public @interface PdfJsViewer {

    int initialPageNum() default 1;
    Scale initialScale() default Scale._1_00;
    int initialHeight() default 800;

}
