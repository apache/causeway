package demoapp.dom.types.tuple;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Value;

@PersistenceCapable
@EmbeddedOnly
@Value(semanticsProviderClass = ComplexNumberValueSemantics.class)
@lombok.Data
@lombok.AllArgsConstructor(staticName = "of")
public class ComplexNumber {
    
    @javax.jdo.annotations.Column(allowsNull = "false")
    private double re;
    
    @javax.jdo.annotations.Column(allowsNull = "false")
    private double im;

}
