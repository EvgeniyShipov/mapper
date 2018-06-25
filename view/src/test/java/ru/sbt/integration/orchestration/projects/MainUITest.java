package ru.sbt.integration.orchestration.projects;

import com.sbt.bm.rdm.api.CibOrganization;
import com.sbt.bm.ucp.corp.model.party.organization.Organization;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.VerticalLayout;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.model.FieldTreeModel;
import ru.sbt.integration.orchestration.mapper.model.TreeModel;

public class MainUITest {
    private TreeGrid<FieldNode> grid;
    final VerticalLayout layout = new VerticalLayout();
    private static TreeModel modelSource;
    private static TreeModel modelDest;

    @BeforeClass
    public static void generateModel() {
        modelSource = new FieldTreeModel(Organization.class);
        modelDest = new FieldTreeModel(CibOrganization.class);
    }

    @Test
    public void generate() {
        grid = new TreeGrid<>();
        layout.addComponent(grid);
        grid.addColumn(FieldNode::getName).setCaption("Fields - " + modelSource.getObject().getSimpleName());
        grid.setItems(modelSource.getParentNodes());

        TreeDataProvider<FieldNode> provider = (TreeDataProvider<FieldNode>) grid.getDataProvider();

        TreeData<FieldNode> data = provider.getTreeData();

        data.addItems(modelSource.getParentNodes().get(10), modelSource.getParentNodes().get(0).getChildren());

        grid.expand(modelSource.getParentNodes().get(0).getChildren());

        grid.expand(modelSource.getParentNodes().get(0));

        provider.refreshAll();
    }
}