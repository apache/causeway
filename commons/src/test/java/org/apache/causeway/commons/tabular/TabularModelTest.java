package org.apache.causeway.commons.tabular;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.collections.Can;

class TabularModelTest {
    
    @Test
    void test() {
        
        var col1 = new TabularModel.TabularColumn(0, "Col-1", "Column Description 1");
        var col2 = new TabularModel.TabularColumn(1, "Col-2", "Column Description 2");
        
        var row1 = new TabularModel.TabularRow(Can.of(
                TabularModel.TabularCell.single("cell1-1"), 
                TabularModel.TabularCell.single("cell1-2")));
        var row2 = new TabularModel.TabularRow(Can.of(
                TabularModel.TabularCell.single("cell1-1"), 
                TabularModel.TabularCell.single("cell1-2")));
        
        var sheet = new TabularModel.TabularSheet("sheet-1", Can.of(col1, col2), Can.of(row1, row2));
        
    }
}
