package org.apache.isis.core.runtimeservices.command;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.util.schema.DtoContext;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@Named("isis.runtimeservices.DtoContextDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Getter(onMethod_ = {@Override})
@RequiredArgsConstructor
public class DtoContextDefault implements DtoContext {

    @Inject private BookmarkService bookmarkService;
    @Inject private ValueSemanticsResolver valueSemanticsResolver;

}
