package demoapp.dom.types.tuple;

import lombok.Value;

@Value(staticConstructor = "of")
public class ComplexNumber {
    
    final double re;
    final double im;

}
