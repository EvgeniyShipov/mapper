package ru.sbt.integration.orchestration.mapper.views;

import com.vaadin.data.Binder;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.SerializableComparator;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.ItemClickListener;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.mapper.EditorWindowController;
import ru.sbt.integration.orchestration.mapper.MainUI;
import ru.sbt.integration.orchestration.mapper.MappingViewController;
import ru.sbt.integration.orchestration.mapper.exception.UnsupportedMappingFormatException;
import ru.sbt.integration.orchestration.mapper.generator.MappingGenerator;
import ru.sbt.integration.orchestration.mapper.generator.XmlFileMappingGenerator;
import ru.sbt.integration.orchestration.mapper.mapping.MappingPair;
import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.utils.Notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Окно визуального маппинга
 */
public class MappingView extends VerticalLayout implements View {
    private final MappingViewController mappingViewController;
    private final boolean isSinglePageMode;
    private static final String MAPPING_IS_NOT_ALLOWED = "v-grid-row-mapping-is-not-allowed";
    private static final String MAPPING_IS_ALLOWED = "v-grid-row-simple";
    private static final String MAPPING_WAS_PERFORMED = "v-grid-row-is-mapped";

    public MappingView(Navigator navigator, MappingViewController controller, boolean isSinglePageMode) {
        this.isSinglePageMode = isSinglePageMode;
        mappingViewController = controller;
        initView(navigator);
    }

    public MappingView(Navigator navigator, List<Class<?>> sources, Class<?> destination,
                       List<Class<?>> destinationArtifactListClasses, boolean isSinglePageMode) {
        this(navigator, new MappingViewController(sources, destination, destinationArtifactListClasses), isSinglePageMode);
    }

    private void initView(Navigator navigator) {
        setSizeFull();
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth(85, Unit.PERCENTAGE);
        horizontalLayout.setHeight(100, Unit.PERCENTAGE);

        VerticalLayout sourceLayout = new VerticalLayout();
        sourceLayout.setSizeFull();
        VerticalLayout destinationLayout = new VerticalLayout();
        destinationLayout.setSizeFull();

        Grid<FieldNode> destinationGrid = createDestinationGrid();
        Grid<FieldNode> sourceGrid = createSourceGrid();

        sourceLayout.addComponent(sourceGrid);
        destinationLayout.addComponent(destinationGrid);

        Button buttonAddMapping = createButton("Add mapping" + " ", event -> {
            mappingViewController.configureAddMappingButtonEvent(event.getButton());
            closeEditWindow(navigator);
            sourceGrid.getDataProvider().refreshAll();
            destinationGrid.getDataProvider().refreshAll();
        });

        Button buttonClearMapping = createButton("Clear mapping history", event ->
                mappingViewController.configureClearMappingButtonEvent(sourceGrid.getDataProvider(), destinationGrid.getDataProvider()));

        Button buttonSubEdit = createButton("Edit", event -> createWindow(navigator, sourceGrid, destinationGrid));

        sourceGrid.addItemClickListener((ItemClickListener<FieldNode>) event ->
                mappingViewController.setSourceItemClickListener(event, destinationGrid, buttonAddMapping));

        AbstractOrderedLayout actionButtonsLayout = new HorizontalLayout();

        HorizontalLayout footter = this.isSinglePageMode ?
                createSaveButton(navigator) :
                createNavigateButtons(navigator);

        addComponentOnLayout(actionButtonsLayout, buttonAddMapping, Alignment.BOTTOM_CENTER);
        addComponentOnLayout(actionButtonsLayout, buttonClearMapping, Alignment.BOTTOM_CENTER);
        addComponentOnLayout(actionButtonsLayout, buttonSubEdit, Alignment.BOTTOM_CENTER);

        horizontalLayout.addComponent(sourceLayout);
        horizontalLayout.addComponent(destinationLayout);

        addComponentOnLayout(this, horizontalLayout, Alignment.MIDDLE_CENTER, 0.9f);
        addComponentOnLayout(this, actionButtonsLayout, Alignment.BOTTOM_CENTER, 0.05f);
        addComponentOnLayout(this, footter, Alignment.BOTTOM_CENTER, 0.05f);

        this.addLayoutClickListener(event -> closeEditWindow(navigator));
    }

    private void addComponentOnLayout(AbstractOrderedLayout layout, Component component, Alignment alignment, Float ratio) {
        layout.addComponent(component);
        layout.setComponentAlignment(component, alignment);
        layout.setExpandRatio(component, ratio);
    }

    private void addComponentOnLayout(AbstractOrderedLayout layout, Component component, Alignment alignment) {
        layout.addComponent(component);
        layout.setComponentAlignment(component, alignment);
    }

    private Grid<FieldNode> createSourceGrid() {
        Grid<FieldNode> sourceGrid = new TreeGrid<>();
        sourceGrid.setCaption("<h2>Source<h2>");
        sourceGrid.setCaptionAsHtml(true);

        List<FieldNode> rootNodes = new ArrayList<>();
        mappingViewController.configureSourceNodes(rootNodes);
        configureGrid(sourceGrid, rootNodes, false);
        sourceGrid.setStyleGenerator(this::getSourceGridStyle);
        return sourceGrid;
    }

    private Grid<FieldNode> createDestinationGrid() {
        Grid<FieldNode> destinationGrid = new TreeGrid<>();
        destinationGrid.setCaption("<h2>Destination<h2>");
        destinationGrid.setCaptionAsHtml(true);

        List<FieldNode> rootNodes = new ArrayList<>();
        mappingViewController.configureDestinationNodes(rootNodes);
        configureGrid(destinationGrid, rootNodes, true);
        destinationGrid.setStyleGenerator(this::getDestinationGridStyle);
        destinationGrid.addItemClickListener(mappingViewController::setDestinationItemClickListener);

        return destinationGrid;
    }

    private void configureGrid(Grid<FieldNode> grid, List<FieldNode> nodes, boolean isDestinationGrid) {
        grid.setSizeFull();
        grid.setItems(nodes);

        TreeDataProvider<FieldNode> provider = (TreeDataProvider<FieldNode>) grid.getDataProvider();
        Binder<FieldNode> binder = grid.getEditor().getBinder();
        grid.addColumn(FieldNode::getSignature)
                .setEditorBinding(getBindType(binder, provider, isDestinationGrid))
                .setCaption("Type")
                .setId("type");
        grid.addColumn(FieldNode::getName)
                .setEditorBinding(getBindName(provider, binder))
                .setCaption("Field name")
                .setId("field");
        grid.addColumn(FieldNode::getRequired)
                .setCaption("0..n")
                .setEditorBinding(getBindRequired(binder))
                .setMinimumWidth(70)
                .setMaximumWidth(90);

        mappingViewController.fillNodes(provider, nodes);
        provider.setSortComparator(getComparator());
        provider.refreshAll();
        grid.getEditor().setEnabled(true);
    }

    private Binder.Binding<FieldNode, String> getBindType(Binder<FieldNode> binder, TreeDataProvider<FieldNode> provider, boolean isDestinationGrid) {
        return binder.bind(
                getComboBoxWithType(isDestinationGrid),
                FieldNode::getSignature,
                (node, value) -> {
                    try {
                        mappingViewController.configureBindingType(provider, node, value);
                    } catch (ClassNotFoundException e) {
                        Notifications.ERROR_NOTIFICATION.show("Невозможно загрузить класс ", e.getMessage());
                    } catch (UnsupportedMappingFormatException e) {
                        Notifications.ERROR_NOTIFICATION.show("", e.getMessage());
                    }
                });
    }

    private ComboBox<String> getComboBoxWithType(boolean isDestination) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPopupWidth(null);
        comboBox.setEmptySelectionAllowed(false);
        if (!isDestination) {
            comboBox.addFocusListener(event -> comboBox.setItems(mappingViewController.getTypeComboBoxItems(false)));
        }
        comboBox.setItems(mappingViewController.getTypeComboBoxItems(isDestination));
        return comboBox;
    }

    private Binder.Binding<FieldNode, String> getBindName(TreeDataProvider<FieldNode> provider, Binder<FieldNode> binder) {
        return binder.bind(
                new TextField(),
                FieldNode::getName,
                (node, value) -> mappingViewController.configureBindingName(provider, node, value));
    }

    private Binder.Binding<FieldNode, FieldNode.Required> getBindRequired(Binder<FieldNode> binder) {
        return binder.bind(getComboBoxWithRequired(), FieldNode::getRequired, FieldNode::setRequired);
    }

    private ComboBox<FieldNode.Required> getComboBoxWithRequired() {
        ComboBox<FieldNode.Required> comboBox = new ComboBox<>();
        comboBox.setItems(FieldNode.Required.values());
        return comboBox;
    }

    private SerializableComparator<FieldNode> getComparator() {
        return (fn1, fn2) -> {
            if (StringUtils.isNumeric(fn1.getName()) && StringUtils.isNumeric(fn2.getName()))
                return Integer.valueOf(fn1.getName()).compareTo(Integer.valueOf(fn2.getName()));
            return fn1.getName().compareTo(fn2.getName());
        };
    }

    private void navigateToNextView(Navigator navigator) {
        navigator.addView(MainUI.GENERATOR_VIEW, new GeneratedMappingView(mappingViewController.getModel(), navigator));
        navigator.navigateTo(MainUI.GENERATOR_VIEW);
    }

    @NotNull
    private Button createButton(String text, Button.ClickListener listener) {
        Button buttonAddMapping = new Button(text, listener);
        buttonAddMapping.setWidth(15, Unit.PICAS);
        return buttonAddMapping;
    }

    private String getSourceGridStyle(FieldNode node) {
        if (!(node instanceof CollectionChildNode) && node.getGetter() == null) {
            return MAPPING_IS_NOT_ALLOWED;
        }
        return node.getMappedLinksCount() > 0 ? MAPPING_WAS_PERFORMED : MAPPING_IS_ALLOWED;
    }

    private String getDestinationGridStyle(FieldNode node) {
        if (!(node instanceof CollectionChildNode) && node.getGetter() == null) {
            return MAPPING_IS_NOT_ALLOWED;
        }
        if (node.getMappedLinksCount() > 0) {
            return "v-grid-row-is-mapped";
        }
        return mappingViewController.isValidForMapping(node) ? MAPPING_IS_ALLOWED : MAPPING_IS_NOT_ALLOWED;
    }

    private HorizontalLayout createSaveButton(Navigator navigator) {
        HorizontalLayout button = new HorizontalLayout();
        Button downloadButton = createButton("Save&Close", event -> {
            saveXML();
            closeEditWindow(navigator);
        });
        button.setWidth(100, Unit.PERCENTAGE);
        button.addComponent(downloadButton);
        button.setComponentAlignment(downloadButton, Alignment.BOTTOM_RIGHT);

        return button;
    }

    private void saveXML() {
        try {
            MappingGenerator generator = new XmlFileMappingGenerator(mappingViewController.getModel());
            generator.generate();
            String xml = generator.getGeneratedCode().replaceAll("\\r|\\n", "");
            String uuid = mappingViewController.getUuid();
            this.removeAllComponents();
            String escapedMappingXML = StringEscapeUtils.escapeEcmaScript(xml);
            Page.getCurrent().getJavaScript().execute("saveAndCloseMapper('" + escapedMappingXML +
                    "', '" + uuid + "');");
        } catch (Exception e) {
            Notifications.ERROR_NOTIFICATION.show("Mapping was failed", e.getMessage());
        }
    }

    private HorizontalLayout createNavigateButtons(Navigator navigator) {
        HorizontalLayout buttons = new HorizontalLayout();
        Button nextButton = createButton("Generate mapping", event -> {
            navigateToNextView(navigator);
            closeEditWindow(navigator);
        });
        Button backButton = createButton("Back", event -> {
            navigator.navigateTo(MainUI.CHOOSE_VIEW);
            closeEditWindow(navigator);
        });
        backButton.setWidth(10, Unit.PICAS);
        buttons.setWidth(100, Unit.PERCENTAGE);
        buttons.addComponent(backButton);
        buttons.addComponent(nextButton);
        buttons.setComponentAlignment(backButton, Alignment.BOTTOM_LEFT);
        buttons.setComponentAlignment(nextButton, Alignment.BOTTOM_RIGHT);

        return buttons;
    }

    private void closeEditWindow(Navigator navigator) {
        navigator.getUI().getWindows().stream()
                .filter(w -> w.getContent() instanceof EditorWindow)
                .forEach(Window::close);
    }

    public void createWindow(Navigator navigator, Grid<FieldNode> sourceGrid, Grid<FieldNode> destinationGrid) {
        navigator.getUI().getWindows().stream()
                .filter(w -> w.getContent() instanceof EditorWindow)
                .findAny().orElseGet(() -> {
            Window editWindow = new Window();
            navigator.getUI().addWindow(editWindow);
            editWindow.setWidth(65, Unit.PERCENTAGE);
            editWindow.setHeight(65, Unit.PERCENTAGE);
            editWindow.setContent(new EditorWindow());
            editWindow.center();
            editWindow.focus();

            editWindow.addCloseListener(event -> {
                mappingViewController.configureCloseListener();
                sourceGrid.getDataProvider().refreshAll();
                destinationGrid.getDataProvider().refreshAll();
            });
            return editWindow;
        });
    }

    class EditorWindow extends VerticalLayout {
        private final EditorWindowController editorViewController;

        EditorWindow() {
            editorViewController = new EditorWindowController(mappingViewController.getModel());
            setSizeFull();

            Grid<MappingPair> grid = getGrid();
            TextField searchTextField = getSearchTextField(grid);

            HorizontalLayout gridLayout = new HorizontalLayout();
            gridLayout.addComponent(grid);
            gridLayout.setSizeFull();

            HorizontalLayout searchTextLayout = new HorizontalLayout();
            searchTextLayout.addComponent(searchTextField);
            searchTextLayout.setSizeFull();

            addComponent(gridLayout);
            setExpandRatio(gridLayout, 0.9f);
            setComponentAlignment(gridLayout, Alignment.MIDDLE_CENTER);

            addComponent(searchTextLayout);
            setExpandRatio(searchTextLayout, 0.1f);
            setComponentAlignment(searchTextLayout, Alignment.MIDDLE_CENTER);
        }

        private TextField getSearchTextField(Grid<MappingPair> grid) {
            TextField searchTextField = new TextField();
            searchTextField.setPlaceholder("Search...");
            searchTextField.setWidth(100, Unit.PERCENTAGE);
            searchTextField.addValueChangeListener(event ->
                    ((TreeDataProvider<MappingPair>) grid.getDataProvider()).setFilter(mappingPair ->
                            mappingPair.getSource().getName().contains(event.getValue())
                                    || mappingPair.getDestination().getName().contains(event.getValue())));
            return searchTextField;
        }

        private Grid<MappingPair> getGrid() {
            Grid<MappingPair> grid = new TreeGrid<>();
            grid.setSizeFull();
            grid.setCaptionAsHtml(true);
            editorViewController.configureGrid(grid);
            this.addShortcutListener(editorViewController.getDeleteShortCutListener(grid));

            return grid;
        }
    }
}