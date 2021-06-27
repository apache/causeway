package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.apache.isis.applib.value.Blob;

import lombok.Getter;

@Getter
public class ApplibBlobHolder extends HolderAbstract<ApplibBlobHolder> {
    private Blob someBlob;
    public void setSomeBlob(Blob someBlob) {
        bump();
        this.someBlob = broken ? null : someBlob;
    }
}
