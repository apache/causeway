package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ByteWrapperHolder extends HolderAbstract<ByteWrapperHolder> {
    private Byte someByteWrapper;

    public void setSomeByteWrapper(Byte someByteWrapper) {
        bump();
        this.someByteWrapper = broken ? null : someByteWrapper;
    }
}
