package demoapp.dom.domain.objects.other.embedded.samples;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import demoapp.dom.domain.objects.other.embedded.ComplexNumber;
import demoapp.dom.types.Samples;

@Service
public class ComplexNumberSamples implements Samples<ComplexNumber> {

    @Override
    public Stream<ComplexNumber> stream() {
        return Stream.of(
                ComplexNumber.named("Pi", Math.PI, 0.),
                ComplexNumber.named("Euler's Constant", Math.E, 0.),
                ComplexNumber.named("Imaginary Unit", 0, 1)
            );
    }

}