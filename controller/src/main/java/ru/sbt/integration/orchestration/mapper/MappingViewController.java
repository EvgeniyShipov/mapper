package ru.sbt.integration.orchestration.mapper;

import com.google.common.base.Strings;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import org.apache.commons.lang3.StringUtils;
import ru.sbt.integration.orchestration.mapper.exception.EmptyMappingFieldsException;
import ru.sbt.integration.orchestration.mapper.exception.FieldNotFoundException;
import ru.sbt.integration.orchestration.mapper.exception.UnsupportedMappingFormatException;
import ru.sbt.integration.orchestration.mapper.mapping.MappingPair;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.model.TreeModel;
import ru.sbt.integration.orchestration.mapper.utils.MappingUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.LIST;
import static ru.sbt.integration.orchestration.mapper.utils.MappingUtils.checkMappingErrors;
import static ru.sbt.integration.orchestration.mapper.utils.MappingUtils.isTrivialType;

public class MappingViewController {
    private MultipleMappingModel model;
    private FieldNode sourceNode;
    private FieldNode destinationNode;
    private String uuid;

    private static final String MAPPING_ADDED_NOTIFICATION = "Added mapping from %s to %s";

    public MappingViewController(MultipleMappingModel model) {
        this.model = model;
    }

    public MappingViewController(List<Class<?>> sources, Class<?> destination, List<Class<?>> destinationArtifactListClasses) {
        this(new MultipleMappingModel(sources, destination, destinationArtifactListClasses));
    }

    public MultipleMappingModel getModel() {
        return model;
    }

    public void setSourceItemClickListener(Grid.ItemClick<FieldNode> event, Grid<FieldNode> destinationGrid, Button addMappingButton) {
        //todo логика снятия выделения
        sourceNode = event.getItem();
        setLinkCountOnAddMappingButton(addMappingButton);

        model.getDestination().getAllFieldNodes().forEach(FieldNode::resetMappedLinkCount);
        model.getMappingPairs().stream()
                .filter(pair -> pair.getSource().equals(sourceNode))
                .map(MappingPair::getDestination)
                .forEach(FieldNode::increaseMappedLinkCount);
        destinationGrid.getDataProvider().refreshAll();
    }

    private void setLinkCountOnAddMappingButton(Button addMappingButton) {
        addMappingButton.setCaption("Add mapping" + " " +
                (sourceNode.getMappedLinksCount() > 0 ? "(" + sourceNode.getMappedLinksCount() + ")" : ""));
    }

    public void setDestinationItemClickListener(Grid.ItemClick<FieldNode> event) {
        //todo логика снятия выделения
        destinationNode = event.getItem();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void configureClearMappingButtonEvent(DataProvider<FieldNode, ?> sourceGridProvider, DataProvider<FieldNode, ?> destinationGridProvider) {
        model.clearMapping();
        model.getSourceList().forEach(treeModel ->
                Optional.of(treeModel)
                        .map(TreeModel::getAllFieldNodes)
                        .ifPresent(nodes -> nodes.forEach(FieldNode::resetMappedLinkCount))
        );
        sourceGridProvider.refreshAll();

        model.getDestination().getAllFieldNodes().forEach(FieldNode::resetMappedLinkCount);
        destinationGridProvider.refreshAll();

    }

    public void configureAddMappingButtonEvent(Button addMappingButton) {
        try {
            checkMappingErrors(sourceNode, destinationNode);
        } catch (Exception e) {
            Notification notification = new Notification(e.getMessage(), ERROR_MESSAGE);
            notification.setPosition(Position.BOTTOM_CENTER);
            notification.show(Page.getCurrent());
            return;
        }

        if (!isAddingString(sourceNode) && !isAddingString(destinationNode) && model.addMapping(sourceNode, destinationNode)) {
//            addParentChildCollectionNodesMappings(sourceNode, destinationNode);
            setLinkCountOnAddMappingButton(addMappingButton);

            showNotificationMappingAdded();
        }
    }

    //добавляет маппинг CollectionChildNode соответствующих вложенностей
    private void addParentChildCollectionNodesMappings(FieldNode sourceField, FieldNode destinationField) {
        //todo проверить на множественной вложенности(мапа в мапе - в мапу в мапе)
        if (sourceField == null) return;

        ArrayList<FieldNode> sourceCollectionNodes = new ArrayList<>();
        ArrayList<FieldNode> destinationCollectionNodes = new ArrayList<>();

        while ((sourceField = sourceField.getParent()) != null) {
            if (sourceField instanceof CollectionChildNode) {
                sourceCollectionNodes.add(sourceField);
            }
        }

        while ((destinationField = destinationField.getParent()) != null) {
            if (destinationField instanceof CollectionChildNode) {
                destinationCollectionNodes.add(destinationField);
            }
        }

        for (int i = 0; i < (sourceCollectionNodes.size() > destinationCollectionNodes.size() ? destinationCollectionNodes.size() : sourceCollectionNodes.size()); i++) {
            model.addMapping(sourceCollectionNodes.get(i), destinationCollectionNodes.get(i));
        }
    }

    public void configureCloseListener() {
        getModel().getDestination().getAllFieldNodes().forEach(FieldNode::resetMappedLinkCount);
        model.getMappingPairs().stream()
                .filter(mappingPair -> mappingPair.getSource().equals(sourceNode))
                .forEach(mappingPair -> mappingPair.getDestination().increaseMappedLinkCount());
    }

    private void showNotificationMappingAdded() {
        Notification notification = new Notification(String.format(MAPPING_ADDED_NOTIFICATION, ((sourceNode instanceof CollectionChildNode) ?
                        sourceNode.getParent().getName() : sourceNode.getName()),
                ((destinationNode instanceof CollectionChildNode) ?
                        destinationNode.getParent().getName() : destinationNode.getName())), null, Notification.Type.HUMANIZED_MESSAGE);
        notification.setPosition(Position.MIDDLE_CENTER);
        notification.setDelayMsec(500);
        notification.show(Page.getCurrent());
    }

    public void configureSourceNodes(List<FieldNode> nodes) {
        model.getSourceList().forEach(treeModel -> nodes.add(configureNodes(treeModel)));
    }

    public void configureDestinationNodes(List<FieldNode> nodes) {
        nodes.add(configureNodes(model.getDestination()));
    }

    public void configureBindingType(TreeDataProvider<FieldNode> provider, FieldNode node, String value) throws ClassNotFoundException {
        if (node instanceof CollectionChildNode && value != null) {
            if (!(Objects.equals(node.getType().getName(), value) || (Objects.equals(node.getType().getSimpleName(), value)))) {
                checkRestrictions(node, value);
                ((CollectionChildNode) node).setSignature(value);
                updateCollectionChildNodeType(node, provider);
                provider.refreshAll();
            }
        }
    }

    public void configureBindingName(TreeDataProvider<FieldNode> provider, FieldNode node, String value) {
        if (node instanceof CollectionChildNode) {
            CollectionNode parent = (CollectionNode) node.getParent();
            if (isAddingString(node)) {
                if (isNewChild(value, parent) && isValidValue(node, value)) {
                    CollectionChildNode child = parent.addChild(value);
                    provider.getTreeData().addItem(parent, child);
                    updateCollectionChildNodeType(child, provider);
                }
            } else if (!isValidValue(node, value)) {
                parent.removeChild(node);
                provider.getTreeData().removeItem(node);
            } else {
                node.setName(value);
            }
            provider.refreshAll();
        }
    }

    private boolean isNewChild(String value, CollectionNode parent) {
        return parent.getChildren().stream().map(FieldNode::getName).noneMatch(value::equals);
    }

    private boolean isValidValue(FieldNode node, String value) {
        if (Strings.isNullOrEmpty(value))
            return false;
        return ((CollectionNode) node.getParent()).getCollectionType() != LIST || StringUtils.isNumeric(value);
    }

    private void updateCollectionChildNodeType(FieldNode collectionChildNode, TreeDataProvider<FieldNode> provider) {
        clearCollectionChildNodeChildren(collectionChildNode, provider);

        if (!isTrivialType(collectionChildNode.getType())) {
            setCompositeTypeForChildNode(collectionChildNode, provider);
        }
    }

    private void clearCollectionChildNodeChildren(FieldNode collectionChildNode, TreeDataProvider<FieldNode> provider) {
        collectionChildNode.getChildren().clear();
        List<FieldNode> children = new ArrayList<>(provider.getTreeData().getChildren(collectionChildNode));
        for (FieldNode child : children) {
            provider.getTreeData().removeItem(child);
        }
    }

    private void checkRestrictions(FieldNode node, String value) {
        checkAddingString(node);
        checkUnsupportedTypes(value);
    }

    private void checkAddingString(FieldNode node) {
        if (isAddingString(node))
            throw new UnsupportedMappingFormatException("Нельзя изменить тип данной строки");
    }

    private void checkUnsupportedTypes(String className) {
        if (CollectionNode.CollectionType.MAP.getSuperInterfaceName().equals(className) ||
                CollectionNode.CollectionType.LIST.getSuperInterfaceName().equals(className))
            throw new UnsupportedMappingFormatException("Данный тип пока не поддерживается");
    }

    public List<String> getTypeComboBoxItems(boolean isDestination) {
        Set<Class<?>> classes = MappingUtils.getMappableTypes().stream()
                .filter(clazz -> !clazz.isPrimitive())
                .collect(Collectors.toSet());
        classes.addAll(MappingUtils.getCollectionTypes());
        classes.addAll((isDestination ? model.getDestinationArtifactListClasses() : model.getDestination().getFieldTypes())
                .stream()
                .filter(clazz -> !clazz.isPrimitive())
                .collect(Collectors.toSet()));

        //todo реализовать логику наследования дженериков
//        final List<String> availableClassTypeNames = new ArrayList<>();
//        if (isDestination) {
//            if (destinationNode != null && destinationNode.getClass() == CollectionChildNode.class) {
//                availableClassTypes
//                        .forEach(clazz -> {
//                            if (clazz.isAssignableFrom(ReflectionUtils.loadChildClass
//                                    (ReflectionUtils.getFullChildGenericSignature((CollectionNode) destinationNode.getParent()), clazz.getClassLoader()))) {
//                                availableClassTypeNames.add(clazz.getName());
//                            }
//                        });
//            }
//        } else {
//            if (sourceNode != null && sourceNode.getClass() == CollectionChildNode.class) {
//                availableClassTypes
//                        .forEach(clazz -> {
//                            if (clazz.isAssignableFrom(ReflectionUtils.loadChildClass
//                                    (ReflectionUtils.getFullChildGenericSignature((CollectionNode) sourceNode.getParent()), clazz.getClassLoader()))) {
//                                availableClassTypeNames.add(clazz.getName());
//                            }
//                        });
//            }
//        }
        return classes.stream().map(Class::getName).sorted().collect(Collectors.toList());
    }

    public void setCompositeTypeForChildNode(FieldNode collectionChildNode, TreeDataProvider<FieldNode> provider) {
        try {
            model.getDestination().getNode(collectionChildNode.getParent().getField());
            updateCollectionChildNodeTypeInModel(model.getDestination(), collectionChildNode, provider);
        } catch (FieldNotFoundException e) {
            for (TreeModel sourceModel : model.getSourceList()) {
                try {
                    sourceModel.getNode(collectionChildNode.getParent().getField());
                    updateCollectionChildNodeTypeInModel(sourceModel, collectionChildNode, provider);
                    break;
                } catch (FieldNotFoundException ignored) {
                }
            }
        }
    }

    public void fillNodes(TreeDataProvider<FieldNode> dataProvider, List<FieldNode> nodes) {
        for (FieldNode node : nodes) {
            if (!MappingUtils.isTrivialType(node)) {
                fillChildNodes(dataProvider, node);
                nodes = node.getChildren();
                fillNodes(dataProvider, nodes);
            }
        }
    }

    private void fillChildNodes(TreeDataProvider<FieldNode> dataProvider, FieldNode node) {
        TreeData<FieldNode> data = dataProvider.getTreeData();
        data.addItems(node, node.getChildren());
    }

    private void updateCollectionChildNodeTypeInModel(TreeModel updModel, FieldNode collectionChildNode, TreeDataProvider<FieldNode> provider) {
        List<FieldNode> list = updModel.updateNodeType(collectionChildNode.getType(), collectionChildNode);
        provider.getTreeData().addItems(collectionChildNode, list);
        fillNodes(provider, list);
    }

    private FieldNode configureNodes(TreeModel treeModel) {
        FieldNode rootNode = new FieldNode(treeModel.getObject());
        rootNode.setChildren(treeModel.getParentNodes());
        return rootNode;
    }

    private boolean isAddingString(FieldNode node) {
        if (node == null)
            return false;
        if (!(node.getParent() instanceof CollectionNode))
            return false;

        return Objects.equals(node.getName(), ((CollectionNode) node.getParent()).getCollectionType().getFirstStringName());
    }

    public boolean isValidForMapping(FieldNode destinationNode) {
        try {
            //todo сделать отдельный метод и не городить исключения
            checkMappingErrors(sourceNode, destinationNode);
            return true;
        } catch (EmptyMappingFieldsException | UnsupportedMappingFormatException e) {
            return false;
        }
    }
}
