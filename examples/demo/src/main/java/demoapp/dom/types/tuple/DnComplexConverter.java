package demoapp.dom.types.tuple;

import org.datanucleus.store.types.converters.MultiColumnConverter;
import org.datanucleus.store.types.converters.TypeConverter;

public class DnComplexConverter implements TypeConverter<ComplexNumber, double[]>, MultiColumnConverter {

    private static final long serialVersionUID = 1L;

    @Override
    public Class[] getDatastoreColumnTypes() {
        return new Class<?>[] {double.class, double.class};
    }

    @Override
    public double[] toDatastoreType(ComplexNumber memberValue) {
        return new double[] {memberValue.getRe(), memberValue.getIm()};
    }

    @Override
    public ComplexNumber toMemberType(double[] datastoreValue) {
        return ComplexNumber.of(datastoreValue[0], datastoreValue[1]);
    }

}
