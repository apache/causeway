package org.apache.isis.applib.value.semantics;

import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.resources._Json;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

public final class ValueDecomposition extends _Either<ValueWithTypeDto, TypedTupleDto> {
    private static final long serialVersionUID = 1L;

    public static ValueDecomposition ofFundamental(final ValueWithTypeDto valueWithTypeDto) {
        return new ValueDecomposition(valueWithTypeDto, null);
    }

    public static ValueDecomposition ofComposite(final TypedTupleDto typedTupleDto) {
        return new ValueDecomposition(null, typedTupleDto);
    }

    private ValueDecomposition(final ValueWithTypeDto left, final TypedTupleDto right) {
        super(left, right);
    }

    // used by RO-Viewer to render values
    public String toJson() {
        return this.<String>fold(
                fundamental->CommonDtoUtils.getFundamentalValueAsJson(fundamental),
                composite->composite!=null
                        ? _Json.toString(composite).presentElseFail()
                        : null);
    }

    // used by EncodableFacet
    public static ValueDecomposition fromJson(final ValueType vType, final String json) {
        if(vType==ValueType.COMPOSITE) {
            return _Json.readJson(ValueDecomposition.class, json).presentElseFail();
        }
        return ofFundamental(
                CommonDtoUtils.getFundamentalValueFromJson(vType, json));
    }

}