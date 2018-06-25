package ru.sbt.integration.orchestration.mapper.model;

import java.lang.reflect.Field;

import static ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils.cutPackages;

public class CollectionNode extends FieldNode {
    private String signature;
    private final CollectionType collectionType;

    CollectionNode(Field field, CollectionType collectionType, Class<?> rootClass) {
        super(field, rootClass);
        this.signature = cutPackages(field.getGenericType().toString());
        this.collectionType = collectionType;
        this.setRequired(Required.ZERO_TO_INFINITY);
        this.setRootClass(rootClass);
    }

    public CollectionChildNode addChild() {
        CollectionChildNode child = new CollectionChildNode(this);
        this.getChildren().add(child);
        return child;
    }

    public CollectionChildNode addChild(String name) {
        return new CollectionChildNode(this, name);
    }

    public void removeChild(FieldNode child) {
        this.getChildren().remove(child);
    }

    @Override
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = cutPackages(signature);
    }

    public enum CollectionType {
        MAP(",", "java.util.Map", "add new key"),
        LIST("<", "java.util.List", "n");

        CollectionType(String splitFilter, String superInterfaceName, String firstStringName) {
            this.splitFilter = splitFilter;
            this.superInterfaceName = superInterfaceName;
            this.firstStringName = firstStringName;
        }

        private final String splitFilter;
        private final String superInterfaceName;
        private final String firstStringName;

        public String getFirstStringName() {
            return firstStringName;
        }

        public String getSuperInterfaceName() {
            return superInterfaceName;
        }

        public String getSplitFilter() {
            return splitFilter;
        }
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }


}