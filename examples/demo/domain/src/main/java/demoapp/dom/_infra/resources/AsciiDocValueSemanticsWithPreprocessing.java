package demoapp.dom._infra.resources;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.isis.valuetypes.asciidoc.metamodel.semantics.AsciiDocValueSemantics;

@Component
@Named("demo.AsciiDocValueSemantics")
@Qualifier("adoc-pre-processor")
public class AsciiDocValueSemanticsWithPreprocessing
extends AsciiDocValueSemantics {

    //FIXME add pre-processing stuff

}
