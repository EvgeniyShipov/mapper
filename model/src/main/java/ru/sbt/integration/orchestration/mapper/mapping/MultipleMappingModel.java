package ru.sbt.integration.orchestration.mapper.mapping;

import ru.sbt.integration.orchestration.mapper.model.*;

import java.lang.reflect.Field;
import java.util.*;

public class MultipleMappingModel {
    private final Set<MappingPair> mappingPairs = new LinkedHashSet<>();
    private final List<TreeModel> sourceModelList = new ArrayList<>();
    private final TreeModel destinationModel;
    private final List<Class<?>> destinationArtifactListClasses;

    public MultipleMappingModel(List<Class<?>> sourceClassList, Class<?> destinationClass, List<Class<?>> destinationArtifactListClasses) {
        this.destinationArtifactListClasses = destinationArtifactListClasses;
        for (Class<?> sourceClass : sourceClassList) {
            sourceModelList.add(new FieldTreeModel(sourceClass));
        }
        destinationModel = new FieldTreeModel(destinationClass);
    }

    public MultipleMappingModel(Class<?> sourceClass, Class<?> destinationClass, List<Class<?>> destinationArtifactListClasses) {
        this.destinationArtifactListClasses = destinationArtifactListClasses;
        sourceModelList.add(new FieldTreeModel(sourceClass));
        destinationModel = new FieldTreeModel(destinationClass);
    }

    public List<TreeModel> getSourceList() {
        return sourceModelList;
    }

    public TreeModel getDestination() {
        return destinationModel;
    }

    public Set<MappingPair> getMappingPairs() {
        return mappingPairs;
    }

    public void clearMapping() {
        mappingPairs.clear();
    }

    public boolean addMapping(FieldNode source, FieldNode destination) {
        if (mappingPairs.add(new MappingPair(source, destination))) {
            source.increaseMappedLinkCount();
            destination.increaseMappedLinkCount();
            return true;
        }
        return false;
    }

    public void addMapping(Class<?> sourceClass, Field source, Field destination) {
        TreeModel sourceModel = getSourceModel(sourceClass);
        if (sourceModel != null) {
            mappingPairs.add(new MappingPair(sourceModel.getNode(source), destinationModel.getNode(destination)));
        }
    }

    public TreeModel getSourceModel(Class<?> sourceClass) {
        return sourceModelList
                .stream()
                .filter(tm -> (tm.getObject().equals(sourceClass)))
                .findFirst().orElse(null);
    }

    public FieldNode addChildField(CollectionNode collectionNode, String key) {
        return addChildField(collectionNode, key, destinationModel);
    }

    public FieldNode addChildField(CollectionNode collectionNode, String key, TreeModel model) {
        FieldNode child = collectionNode.addChild();
        child.setName(key);
        //todo похоже ошибка. вроде надо закоментить строку
//        model.getParentNodes().add(child);
        return child;
    }

    public List<Class<?>> getDestinationArtifactListClasses() {
        return new ArrayList<>(destinationArtifactListClasses);
    }
}
