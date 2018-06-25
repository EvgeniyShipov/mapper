package ru.sbt.integration.orchestration.mapper.model;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;
import ru.sbt.integration.orchestration.mapper.exception.FieldNotFoundException;
import ru.sbt.integration.orchestration.mapper.utils.MappingUtils;
import ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.reflections.ReflectionUtils.*;
import static ru.sbt.integration.orchestration.mapper.model.FieldNode.createNewFieldNode;
import static ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils.loadChildClass;


/**
 * Class for Tree representation of Class object
 */
public class FieldTreeModel implements TreeModel {
    private final Class<?> object;
    //todo что за  тема. почему только parentNodes где все остальные? обьединить с методом getFieldTypes
    private final List<FieldNode> parentNodes = new ArrayList<>();
    private FieldNode foundNode = null;
    private final Set<Class<?>> fieldTypes = new HashSet<>();
    private boolean fieldTypesUpdated = false;

    public FieldTreeModel(Class<?> object) {
        this.object = object;
        initializeModel(object);
    }

    @Override
    public List<FieldNode> getParentNodes() {
        return parentNodes;
    }

    @Override
    public List<FieldNode> getAllFieldNodes() {
        return getAllFieldNodes(new ArrayList<>(), parentNodes);
    }

    @Override
    public List<FieldNode> getAllFieldNodes(List<FieldNode> fullNodesList, List<FieldNode> list) {
        fullNodesList.addAll(list);
        for (FieldNode node : list) {
            //todo покрасивее
            if (!isRecursion(node) && !node.getChildren().isEmpty() &&
                    !MappingUtils.isTrivialType(node) && !MappingUtils.isStatic(node.getField())) {
                getAllFieldNodes(fullNodesList, node.getChildren());
            }
        }
        return fullNodesList;
    }

    @Override
    public Class<?> getObject() {
        return object;
    }

    @Override
    public String getCanonicalObjectName() {
        return object.getCanonicalName();
    }

    /**
     * Generates getChildren of chosen class object
     */
    private void initializeModel(Class<?> rootClass) {
        for (Field field : getFields(rootClass)) {
            FieldNode node = createNewFieldNode(field, rootClass);
            if (!MappingUtils.isStatic(node.getField())) {
                //todo почему null да и в целом переделать всю эту генерацию
                node.setParent(null);
                assignMethods(node);
                parentNodes.add(node);
            }
        }
        generateNodes(parentNodes);
        this.fieldTypesUpdated = true;
    }

    //todo придумать название методу
    public List<FieldNode> updateNodeType(Class<?> rootClass, FieldNode fieldNode) {
        final List<FieldNode> rootChildren = new ArrayList<>();
        for (Field field : getFields(rootClass)) {
            FieldNode node = createNewFieldNode(field, rootClass);
            if (!MappingUtils.isStatic(node.getField())) {
                node.setParent(fieldNode);
                assignMethods(node);
                rootChildren.add(node);
            }
        }
        generateNodes(rootChildren);
        fieldNode.getChildren().addAll(rootChildren);
        this.fieldTypesUpdated = true;
        return rootChildren;
    }

    /**
     * Recursively generates getChildren of child nodes
     */
    private void generateNodes(List<FieldNode> nodes) {
        for (FieldNode node : nodes) {
            assignMethods(node);
            if (isRecursion(node)) {
                continue;
            }
            // TODO: 15.02.2018 починить StackOverFlow , на примере api-class-generator, класс ApiGenerator
            //warning! если убрать условие - работает намного медленнее
            if (node.getClass() == CollectionNode.class && node.getChildren().size() == 0) {
                ((CollectionNode) node).addChild();
            }
            if (!MappingUtils.isTrivialType(node) && !MappingUtils.isStatic(node.getField())) {
                addChildNodes(node, getFields(node));
                generateNodes(node.getChildren());
            }
        }
    }

    public Set<Class<?>> getFieldTypes() {
        if (!fieldTypesUpdated) {
            return fieldTypes;
        }
        updateFieldTypes();
        return fieldTypes;
    }

    private void updateFieldTypes() {
        fieldTypes.clear();
        getAllFieldNodes().forEach(node -> fieldTypes.add(node.getType()));
        fieldTypesUpdated = false;
    }

    private void addChildNodes(FieldNode node, List<Field> childNodes) {
        for (Field f : childNodes) {
            if (!MappingUtils.isStatic(f)) node.addChild(f);
        }
    }

    /**
     * Method checks is node containing as field in one of its parent
     */
    private boolean isRecursion(FieldNode node) {
        if (node == null) return true;
        if (MappingUtils.isCollectionType(node) || MappingUtils.isTrivialType(node)) return false;

        FieldNode parent = node.getParent();
        FieldNode parentOfParent = parent != null ? parent.getParent() : null;

        return parentOfParent != null
                && (node.getType().equals(parentOfParent.getType())
                || node.getType().equals(parent.getType()));
    }

    private List<Field> getFields(Class<?> rootClass) {
        return FieldUtils.getAllFieldsList(rootClass);
    }

    private List<Field> getFields(FieldNode node) {
        return FieldUtils.getAllFieldsList((node.getField()).getType());
    }

    /**
     * Method for representing given field as a node
     */
    @Override
    public FieldNode getNode(Field field) {
        foundNode = null;
        FieldNode node = getNode(field, null, parentNodes);
        if (node != null)
            return node;
        else
            throw new FieldNotFoundException();
    }

    /**
     * Method for representing given fieldName as a node
     */
    @Override
    public FieldNode getNode(String fieldName) {
        foundNode = null;
        FieldNode node = getNode(null, fieldName, parentNodes);
        if (node != null)
            return node;
        else
            throw new FieldNotFoundException();
    }

    /**
     * Method recursively searches given field in TreeModel of it's source Class object
     * foundNode object needs to break out of a recursion
     */
    @Nullable
    private FieldNode getNode(Field field, String fieldName, List<FieldNode> nodes) {
        for (FieldNode node : nodes) {
            if (isRecursion(node)) continue;
            if (isNodeEquals(node, field, fieldName)) {
                foundNode = node;
                return foundNode;
            } else {
                nodes = node.getChildren();
                getNode(field, fieldName, nodes);
            }
            if (foundNode != null)
                return foundNode;
        }
        return null;
    }

    private boolean isNodeEquals(FieldNode node, Field field, String fieldName) {
        if (field != null)
            return node.getField().equals(field);
        else
            return node.getField().getName().equals(fieldName);
    }

    private void assignMethods(FieldNode node) {
        assignGetter(node);
        assignSetter(node);
    }

    /**
     * Method gets all methods with given strategy (is it getGetter or getSetter) for given node
     * Then it finds necessary method from set of all methods and assigns it to node
     */
    private void assignGetter(FieldNode node) {
        Set<Method> getters = getAllMethodsWithStrategy(node, Strategy.GET);
        Method getter = findCorrespondingMethod(getters, node, Strategy.GET);
        node.setGetter(getter);
    }

    private void assignSetter(FieldNode node) {
        Set<Method> setters = getAllMethodsWithStrategy(node, Strategy.SET);
        Method setter = findCorrespondingMethod(setters, node, Strategy.SET);
        node.setSetter(setter);
    }

    private Set<Method> getAllMethodsWithStrategy(FieldNode node, Strategy strategy) {
        try {
            if (strategy.equals(Strategy.GET)) {
                return getAllMethods(node.getField().getDeclaringClass(),
                        withModifier(Modifier.PUBLIC),
                        withPrefix(strategy.toString().toLowerCase()),
                        withReturnTypeAssignableTo(node.getType()));
            } else {
                return getAllMethods(node.getField().getDeclaringClass(),
                        withModifier(Modifier.PUBLIC),
                        withPrefix(strategy.toString().toLowerCase()),
                        withReturnTypeAssignableTo(void.class));
            }
        } catch (Throwable e) { // TODO: 15.02.2018  fix the problem with ClassDefNotFound
            return Collections.emptySet();
        }
    }

    @Nullable
    private Method findCorrespondingMethod(Set<Method> methods, FieldNode node, Strategy strategy) {
        for (Method method : methods) {
            if (isMethodFound(method, node, strategy))
                return method;
        }
        return null;
    }

    private boolean isMethodFound(Method method, FieldNode node, Strategy strategy) {
        return method.getName().equalsIgnoreCase(strategy + node.getField().getName());
    }

    private enum Strategy {
        GET, SET
    }
}
