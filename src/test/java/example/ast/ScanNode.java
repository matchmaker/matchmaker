package example.ast;

import java.util.List;

import static java.util.Collections.emptyList;

public class ScanNode implements PlanNode {

    private final String tableName;

    public ScanNode(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public List<PlanNode> getSources() {
        return emptyList();
    }

    public String getTableName() {
        return tableName;
    }
}
