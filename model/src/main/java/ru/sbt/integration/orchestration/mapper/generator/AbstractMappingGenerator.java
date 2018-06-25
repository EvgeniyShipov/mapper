package ru.sbt.integration.orchestration.mapper.generator;

import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.model.TreeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class AbstractMappingGenerator implements MappingGenerator {
    private final List<TreeModel> sourceModelList = new ArrayList<>();
    private final TreeModel destinationModel;
    private final MultipleMappingModel mappingModel;
    private final TreeBranchList branchList = new TreeBranchList();

    AbstractMappingGenerator(MultipleMappingModel mappingModel) {
        this.mappingModel = mappingModel;
        this.sourceModelList.addAll(mappingModel.getSourceList());
        this.destinationModel = mappingModel.getDestination();
    }

    List<TreeModel> getSourceList() {
        return sourceModelList;
    }

    TreeModel getDestination() {
        return destinationModel;
    }

    public MultipleMappingModel getModel() {
        return mappingModel;
    }

    List<FieldNode> getBranch(FieldNode node) {
        return branchList.getBranch(node);
    }

    FieldNode getRoot(FieldNode node) {
        return branchList.getRoot(node);
    }

    /**
     * Inner class for presentation of route from object you need to map to it's root
     * So if you have class Organization that contains field Info that contains field Name
     * nodeBranch will contain: {Name, Field, Organization}
     */
    //todo: зачем так сложно.
    private class TreeBranchList {
        private final List<FieldNode> nodeBranch = new ArrayList<>();

        private List<FieldNode> getBranch(FieldNode node) {
            Class<?> rootClass = node.getRootClass();
            nodeBranch.clear();
//            while (node != null && rootClass != node.getType()) {
            while (node != null) {
                nodeBranch.add(node);
                node = node.getParent();
            }
            Collections.reverse(nodeBranch);
            return nodeBranch;
        }

        private FieldNode getRoot(FieldNode node) {
            getBranch(node);
            return nodeBranch.get(0);
        }
    }
}
