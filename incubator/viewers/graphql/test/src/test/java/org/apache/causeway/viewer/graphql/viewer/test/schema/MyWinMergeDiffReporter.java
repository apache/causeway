package org.apache.causeway.viewer.graphql.viewer.test.schema;

import java.util.Arrays;
import java.util.List;

import org.approvaltests.reporters.DiffInfo;
import org.approvaltests.reporters.DiffPrograms;
import org.approvaltests.reporters.GenericDiffReporter;

import com.spun.util.ArrayUtils;

public class MyWinMergeDiffReporter extends GenericDiffReporter {

    private static DiffInfo WIN_MERGE_REPORTER =
            new DiffInfo(DiffPrograms.Windows.WIN_MERGE_REPORTER.diffProgram,
                    ArrayUtils.combine(
                            List.of(".gql"),
                            ArrayUtils.combine(
                                    GenericDiffReporter.TEXT_FILE_EXTENSIONS,
                                    GenericDiffReporter.IMAGE_FILE_EXTENSIONS)
                            )
                    );

    public MyWinMergeDiffReporter() {
        super(WIN_MERGE_REPORTER);
    }

}
