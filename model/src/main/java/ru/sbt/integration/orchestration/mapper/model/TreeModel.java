package ru.sbt.integration.orchestration.mapper.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public interface TreeModel {
    List<FieldNode> getParentNodes();

    List<FieldNode> getAllFieldNodes();

    List<FieldNode> getAllFieldNodes(List<FieldNode> fullNodesList, List<FieldNode> list);

    Class<?> getObject();

    String getCanonicalObjectName();

    FieldNode getNode(Field field);

    FieldNode getNode(String name);

    Set<Class<?>> getFieldTypes();

    List<FieldNode> updateNodeType(Class<?> rootClass, FieldNode node);
}
