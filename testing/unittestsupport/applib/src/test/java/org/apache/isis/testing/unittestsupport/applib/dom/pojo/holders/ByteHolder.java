package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ByteHolder extends HolderAbstract<ByteHolder> {
    private byte someByte;

    public void setSomeByte(byte someByte) {
        bump();
        this.someByte = broken ? 0 : someByte;
    }
}
