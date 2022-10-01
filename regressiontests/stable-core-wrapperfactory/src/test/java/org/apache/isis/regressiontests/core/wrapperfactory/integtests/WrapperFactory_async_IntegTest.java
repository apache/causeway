package org.apache.isis.regressiontests.core.wrapperfactory.integtests;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.testdomain.wrapperfactory.Counter;
import org.apache.isis.testdomain.wrapperfactory.Counter_bumpUsingMixin;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import lombok.val;

/**
 * Run "sh enhance.sh -w" first, to enhance the test JDO entities.
 */
class WrapperFactory_async_IntegTest extends CoreWrapperFactory_IntegTestAbstract {

    @Inject WrapperFactory wrapperFactory;
    @Inject TransactionService transactionService;
    @Inject BookmarkService bookmarkService;

    Bookmark bookmark;

    @BeforeEach
    void setup_counter() {

        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            counterRepository.persist(newCounter("fred"));
            List<Counter> counters = counterRepository.find();
            assertThat(counters).hasSize(1);

            bookmark = bookmarkService.bookmarkForElseFail(counters.get(0));
        }).ifFailureFail();

        // given
        assertThat(bookmark).isNotNull();

        val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
        assertThat(counter.getNum()).isNull();
    }

    @SneakyThrows
    @Test
    void async_using_default_executor_service() {

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();

            wrapperFactory.asyncWrap(counter, AsyncControl.returning(Counter.class)).bumpUsingDeclaredAction();

            Thread.sleep(1_000);// horrid, but let's just wait 1 sec to allow executor to complete before continuing
        }).ifFailureFail();

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);
        }).ifFailureFail();

        // when
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter.getNum()).isEqualTo(1L);

            // when
            wrapperFactory.asyncWrapMixin(Counter_bumpUsingMixin.class, counter, AsyncControl.returning(Counter.class)).act();

            Thread.sleep(1_000);// horrid, but let's just wait 1 sec to allow executor to complete before continuing
        }).ifFailureFail();

        // then
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            val counter = bookmarkService.lookup(bookmark, Counter.class).orElseThrow();
            assertThat(counter).isNotNull();
            assertThat(counter.getNum()).isEqualTo(2L);
        }).ifFailureFail();
    }

}
