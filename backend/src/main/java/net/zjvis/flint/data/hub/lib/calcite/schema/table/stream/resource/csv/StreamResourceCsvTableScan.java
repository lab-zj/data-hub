package net.zjvis.flint.data.hub.lib.calcite.schema.table.stream.resource.csv;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.adapter.file.CsvTranslatableTable;
import org.apache.calcite.adapter.file.FileRules;
import org.apache.calcite.linq4j.tree.Blocks;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class StreamResourceCsvTableScan extends TableScan implements EnumerableRel {
    final StreamResourceCsvTranslatableTable csvTable;
    private final int[] fields;

    protected StreamResourceCsvTableScan(
            RelOptCluster cluster,
            RelOptTable table,
            @NonNull StreamResourceCsvTranslatableTable csvTable,
            int[] fields
    ) {
        super(cluster, cluster.traitSetOf(EnumerableConvention.INSTANCE), ImmutableList.of(), table);
        this.csvTable = csvTable;
        this.fields = fields;
    }

    @Override
    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        assert inputs.isEmpty();
        return new StreamResourceCsvTableScan(getCluster(), table, csvTable, fields);
    }

    @Override
    public RelWriter explainTerms(RelWriter pw) {
        return super.explainTerms(pw)
                .item("fields", Primitive.asList(fields));
    }

    @Override
    public RelDataType deriveRowType() {
        final List<RelDataTypeField> fieldList = table.getRowType().getFieldList();
        final RelDataTypeFactory.Builder builder =
                getCluster().getTypeFactory().builder();
        for (int field : fields) {
            builder.add(fieldList.get(field));
        }
        return builder.build();
    }

    @Override
    public void register(RelOptPlanner planner) {
        planner.addRule(FileRules.PROJECT_SCAN);
    }

    @Override
    public @Nullable RelOptCost computeSelfCost(RelOptPlanner planner,
                                                RelMetadataQuery mq) {
        // Multiply the cost by a factor that makes a scan more attractive if it
        // has significantly fewer fields than the original scan.
        //
        // The "+ 2D" on top and bottom keeps the function fairly smooth.
        //
        // For example, if table has 3 fields, project has 1 field,
        // then factor = (1 + 2) / (3 + 2) = 0.6
        return super.computeSelfCost(planner, mq)
                .multiplyBy(((double) fields.length + 2D)
                        / ((double) table.getRowType().getFieldCount() + 2D));
    }

    @Override
    public Result implement(EnumerableRelImplementor implementor, Prefer pref) {
        PhysType physType =
                PhysTypeImpl.of(
                        implementor.getTypeFactory(),
                        getRowType(),
                        pref.preferArray());

        return implementor.result(
                physType,
                Blocks.toBlock(
                        Expressions.call(table.getExpression(CsvTranslatableTable.class),
                                "project", implementor.getRootExpression(),
                                Expressions.constant(fields))));
    }
}
