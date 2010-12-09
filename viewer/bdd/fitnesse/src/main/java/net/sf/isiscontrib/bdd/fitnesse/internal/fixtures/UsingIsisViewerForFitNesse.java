package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import java.util.ArrayList;
import java.util.List;

import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractFixture;
import net.sf.isiscontrib.bdd.fitnesse.internal.CellBindingForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.perform.StoryCellForFitNesse;
import net.sf.isiscontrib.bdd.fitnesse.internal.util.FitnesseUtil;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.DateParser;
import org.apache.isis.viewer.bdd.common.fixtures.UsingIsisViewerPeer;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;

import com.google.common.collect.Lists;

import fit.Parse;
import fit.exception.FitFailureException;

public class UsingIsisViewerForFitNesse extends AbstractFixture<UsingIsisViewerPeer> {

    private final List<Parse> argumentCells = new ArrayList<Parse>();

    public UsingIsisViewerForFitNesse(final AliasRegistry aliasesRegistry, final DeploymentType deploymentType, final DateParser dateParser, final Perform.Mode mode) {
        this(aliasesRegistry, deploymentType, dateParser, mode, 
            CellBindingForFitNesse
            .builder(IsisViewerConstants.ON_OBJECT_NAME, 
                IsisViewerConstants.ON_OBJECT_HEAD_SET).ditto().build(), CellBindingForFitNesse
            .builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).optional().build(),
            CellBindingForFitNesse.builder(IsisViewerConstants.PERFORM_NAME, IsisViewerConstants.PERFORM_HEAD_SET).ditto().build(),
            CellBindingForFitNesse.builder(IsisViewerConstants.ON_MEMBER_NAME, IsisViewerConstants.ON_MEMBER_HEAD_SET).optional().build(),
            CellBindingForFitNesse.builder(IsisViewerConstants.THAT_IT_NAME, IsisViewerConstants.THAT_IT_HEAD_SET).ditto().optional()
                .build(), CellBindingForFitNesse
                .builder(IsisViewerConstants.WITH_ARGUMENTS_NAME, IsisViewerConstants.WITH_ARGUMENTS_HEAD_SET).optional().build());
    }

    private UsingIsisViewerForFitNesse(final AliasRegistry aliasesRegistry, final DeploymentType deploymentType, final DateParser dateParser, final Perform.Mode mode,
        final CellBinding onObjectBinding, final CellBinding aliasResultAsBinding, final CellBinding performBinding,
        final CellBinding onMemberBinding, final CellBinding thatItBinding, final CellBinding arg0Binding) {
        super(new UsingIsisViewerPeer(aliasesRegistry, deploymentType, dateParser, mode, onObjectBinding, aliasResultAsBinding, performBinding,
            onMemberBinding, thatItBinding, arg0Binding));
    }

    @Override
    public void doRows(final Parse headRow) {
        super.doRows(headRow);
        ensureArgBindingLast();
    }

    private void ensureArgBindingLast() {
        if (getPeer().isArg0BindingLast()) {
            return;
        }
        FitnesseUtil.exception(this, getPeer().getArg0Binding().getHeadCell(), "(must be last column)");
        throw new FitFailureException("(invalid binding order)");
    }

    @Override
    public void doRow(final Parse row) {
        argumentCells.clear();
        super.doRow(row);
        execute();
    }

    @Override
    public void doCell(final Parse cell, final int columnNum) {
        super.doCell(cell, columnNum);

        captureArgumentCellsIfAny(cell, columnNum);
    }

    private void captureArgumentCellsIfAny(final Parse cell, final int columnNum) {
        if (!getPeer().getArg0Binding().isFound()) {
            return;
        }
        if (columnNum < getPeer().getArg0Binding().getColumn()) {
            return;
        }
        argumentCells.add(cell);
    }

    private void execute() {

        ObjectAdapter onAdapter = null;
        try {
            onAdapter = getPeer().validateOnObject();
        } catch (StoryBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
            return;
        }

        String aliasAs = null;
        try {
            aliasAs = getPeer().validateAliasAs();
        } catch (StoryBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
            return;
        }

        Perform performCommand = null;
        try {
            performCommand = getPeer().validatePerform();
        } catch (StoryBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
            return;
        }

        ObjectMember objectMember = null;
        if (performCommand.requiresMember()) {
            try {
                objectMember = getPeer().validateOnMember(onAdapter);
            } catch (StoryBoundValueException ex) {
                FitnesseUtil.exception(this, ex);
                return;
            }
        }

        try {
            List<StoryCell> argumentStoryCells = asValues(argumentCells);
            getPeer().performCommand(onAdapter, aliasAs, objectMember, performCommand, argumentStoryCells);
        } catch (StoryBoundValueException ex) {
            FitnesseUtil.exception(this, ex);
            return;
        }

        rightMemberElseOnObjectCell();
    }

    private static List<StoryCell> asValues(List<Parse> argCells) {
        List<StoryCell> storyValues = Lists.newArrayList();
        for (Parse parse : argCells) {
            storyValues.add(new StoryCellForFitNesse(parse));
        }
        return storyValues;
    }

    private void rightMemberElseOnObjectCell() {
        UsingIsisViewerPeer peer = getPeer();
        StoryCell storyCell =
            coalesce(peer.getOnMemberBinding().getCurrentCell(), peer.getOnObjectBinding().getCurrentCell());
        right(storyCell);
    }

    private static <T> T coalesce(T... objects) {
        for (T object : objects) {
            if (object != null) {
                return object;
            }
        }
        return null;
    }

}
