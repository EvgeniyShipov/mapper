package ru.sbt.integration.orchestration.mapper;

import com.vaadin.data.Binder;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeGrid;
import ru.sbt.integration.orchestration.mapper.mapping.MappingPair;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;

public class EditorWindowController {
    private final MultipleMappingModel model;

    public EditorWindowController(MultipleMappingModel model) {
        this.model = model;
    }

    public void configureGrid(Grid<MappingPair> grid) {
        grid.setItems(model.getMappingPairs());

        Binder<MappingPair> binder = grid.getEditor().getBinder();


        Binder.Binding<MappingPair, FieldNode.Required> sourceBind = binder.bind(getComboBoxWithRequired(),
                pair -> pair.getSource().getRequired(),
                (pair, value) -> pair.getSource().setRequired(value));

        grid.addColumn(pair -> pair.getSource().getRequired().toString())
                .setEditorBinding(sourceBind)
                .setCaption("0..n")
                .setMaximumWidth(90);
        grid.addColumn(pair -> getFieldPath(pair.getSource()))
                .setCaption("Source field name")
                .setMaximumWidth(300);
        grid.addColumn(pair -> pair.getSource().getSignature())
                .setCaption("Type")
                .setMaximumWidth(120);

        grid.addColumn(MappingPair::getSourceDescription)
                .setEditorComponent(new TextField(), MappingPair::setSourceDescription)
                .setCaption("Description (click to edit)");

        grid.addColumn(pair -> pair.getDestination().getSignature())
                .setCaption("Type")
                .setMaximumWidth(120);
        grid.addColumn(pair -> getFieldPath(pair.getDestination()))
                .setCaption("Destination field name")
                .setMaximumWidth(300);
        Binder.Binding<MappingPair, FieldNode.Required> destinationBind = binder.bind(getComboBoxWithRequired(),
                pair -> pair.getDestination().getRequired(),
                (pair, value) -> pair.getDestination().setRequired(value));
        grid.addColumn(pair -> pair.getDestination().getRequired().toString())
                .setEditorBinding(destinationBind)
                .setCaption("0..n")
                .setMaximumWidth(90);

        grid.getEditor().setEnabled(true);
    }

    private String getFieldPath(FieldNode fieldNode) {
        //todo добавить как методы у fieldNode. обьединить с методом batchName
        if (fieldNode == null || fieldNode.getName() == null) {
            return "";
        }

        StringBuilder fieldPath = new StringBuilder();
        FieldNode node = fieldNode;
        do {
            String nameBlock = node.getName();
            String prefix = node.getParent() == null ? "" : ".";
            if (node instanceof CollectionChildNode) {
                if (CollectionNode.CollectionType.LIST.equals(((CollectionChildNode) node).getCollectionType())) {
                    nameBlock = "[" + nameBlock + "]";
                    prefix = "";
                }
                if (CollectionNode.CollectionType.MAP.equals(((CollectionChildNode) node).getCollectionType())) {
                    nameBlock = "(key=\"" + nameBlock + "\")";
                    prefix = "";
                }
            }
            fieldPath.insert(0, prefix + nameBlock);
        } while ((node = node.getParent()) != null);
        return fieldPath.toString();
    }

    private ComboBox<FieldNode.Required> getComboBoxWithRequired() {
        ComboBox<FieldNode.Required> comboBox = new ComboBox<>();
        comboBox.setItems(FieldNode.Required.values());
        return comboBox;
    }

    public ShortcutListener getDeleteShortCutListener(Grid<MappingPair> grid) {
        return new ShortcutListener("delete", ShortcutAction.KeyCode.DELETE, new int[0]) {
            @Override
            public void handleAction(Object sender, Object target) {
                grid.getSelectedItems().forEach(pair -> {
                    model.getMappingPairs().remove(pair);
                    //todo почему без этого не работает?!
                    ((TreeGrid) grid).getTreeData().removeItem(pair);
                    pair.getSource().reduceMappedLinkCount();
                    //todo mapped меняется на фолс только если в mappingPairs больше нет такого source
                });
                grid.deselectAll();
                grid.getDataProvider().refreshAll();
            }
        };
    }
}