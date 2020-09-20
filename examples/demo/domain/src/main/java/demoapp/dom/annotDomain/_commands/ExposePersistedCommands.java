package demoapp.dom.annotDomain._commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.annotDomain.Action.command.ActionCommandJdo;

/**
 * Marker interface for mixins to contribute to.
 */
//tag::class[]
public interface ExposePersistedCommands {

    @Service
    @Order(OrderPrecedence.EARLY)
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<CommandJdo> {

        public TableColumnOrderDefault() { super(CommandJdo.class); }

        @Override
        protected List<String> orderParented(Object parent, String collectionId, List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(List<String> propertyIds) {
            return Arrays.asList(
                    "timestamp", "commandDto", "username", "complete", "resultSummary"
            );
        }
    }

}
//end::class[]
