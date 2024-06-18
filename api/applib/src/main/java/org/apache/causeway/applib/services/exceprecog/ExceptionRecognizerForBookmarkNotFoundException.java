package org.apache.causeway.applib.services.exceprecog;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.unrecoverable.BookmarkNotFoundException;

@Service
@Named(CausewayModuleApplib.NAMESPACE + ".ExceptionRecognizerForBookmarkNotFoundException")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExceptionRecognizerForBookmarkNotFoundException extends ExceptionRecognizerForType {

    public ExceptionRecognizerForBookmarkNotFoundException() {
        super(Category.NOT_FOUND, BookmarkNotFoundException.class);
    }
}