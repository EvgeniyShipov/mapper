package ru.sbt.integration.orchestration.mapper.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.LIST;
import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.MAP;

public class FieldNode {
    private Field field;
    private Class<?> type;
    private String name;
    private FieldNode parent;
    private final List<FieldNode> children = new ArrayList<>();
    private Required required = Required.ZERO_TO_ONE;
    private Method getter;
    private Method setter;
    private Class<?> rootClass;
    //отображает сколько элементов соответствует выбранной FieldNode мз другой таблицы(sourceTable-destinationTable).
    private int mappedLinksCount;

    protected FieldNode(Field field, Class<?> rootClass) {
        this.field = field;
        this.type = field.getType();
        this.name = field.getName();
        this.rootClass = rootClass;
    }

    public FieldNode(Class<?> rootClass) {
        this.type = rootClass;
        this.name = "";
        this.rootClass = rootClass;
    }

    public Field getField() {
        return field;
    }

    public FieldNode getParent() {
        return parent;
    }

    public void setParent(FieldNode parent) {
        this.parent = parent;
    }

    //todo неоопшно
    public List<FieldNode> getChildren() {
        return children;
    }

    public void addChild(Field child) {
        FieldNode childNode = createNewFieldNode(child, this.rootClass);
        childNode.setParent(this);
        children.add(childNode);
    }

    public void setChildren(List<FieldNode> children) {
        this.children.clear();
        this.children.addAll(children);
    }

    public boolean isChildOf(Class<?> clazz) {
        return clazz.getName().equals(getField().getDeclaringClass().getName()) ||
                getParent() != null && getParent().isChildOf(clazz);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method method) {
        this.getter = method;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method method) {
        this.setter = method;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getSignature() {
        return getType().getSimpleName();
    }

    public Required getRequired() {
        return required;
    }

    public void setRequired(Required required) {
        this.required = required;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public void setRootClass(Class<?> rootClass) {
        this.rootClass = rootClass;
    }

    public int getMappedLinksCount() {
        return mappedLinksCount;
    }

    public void increaseMappedLinkCount() {
        mappedLinksCount++;
    }

    public void reduceMappedLinkCount() {
        mappedLinksCount--;
    }

    public void resetMappedLinkCount() {
        mappedLinksCount = 0;
    }

    public static FieldNode createNewFieldNode(Field value, Class<?> rootClass) {
        if (MAP.getSuperInterfaceName().equals(value.getType().getName())) {
            return new CollectionNode(value, MAP, rootClass);
        }
        if (LIST.getSuperInterfaceName().equals(value.getType().getName())) {
            return new CollectionNode(value, LIST, rootClass);
        }

        Class[] interfaces = value.getType().getInterfaces();

        if (interfaces != null) {
            for (Class clazz : interfaces) {
                String interfaceName = clazz.getName();
                if (MAP.getSuperInterfaceName().equals(interfaceName)) {
                    return new CollectionNode(value, MAP, rootClass);
                }
                if (LIST.getSuperInterfaceName().equals(interfaceName)) {
                    return new CollectionNode(value, LIST, rootClass);
                }
            }
        }
        return new FieldNode(value, rootClass);
    }

    public enum Required {
        ONE("1"),
        ZERO_TO_ONE("0..1"),
        ZERO_TO_INFINITY("0..n"),
        ONE_TO_INFINITY("1..n");

        private final String description;

        Required(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
