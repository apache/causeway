package org.apache.causeway.testdomain.value;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Utils {

    void assertNumberEquals(final BigDecimal a, final BigDecimal b) {
        val maxScale = Math.max(a.scale(), b.scale());
        assertEquals(
                a.setScale(maxScale),
                b.setScale(maxScale));
    }
    
    InteractionContext interactionContext() {
        return InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build();
    }
    
    // eg.. <ValueWithTypeDto type="string"><com:string>anotherString</com:string></ValueWithTypeDto>
    String valueDtoToXml(final ValueWithTypeDto valueWithTypeDto) {
        val rawXml = Try.call(()->JaxbUtils.toStringUtf8(valueWithTypeDto, opts->opts
                .useContextCache(true)
                .formattedOutput(true)))
        .getValue().orElseThrow();

        return TextUtils.cutter(rawXml)
                .dropBefore("<ValueWithTypeDto")
                .keepBeforeLast("</ValueWithTypeDto>")
                .getValue()
                .replace(" null=\"false\" xmlns:com=\"https://causeway.apache.org/schema/common\" xmlns:cmd=\"https://causeway.apache.org/schema/cmd\"", "")
                + "</ValueWithTypeDto>";

    }
    
}
