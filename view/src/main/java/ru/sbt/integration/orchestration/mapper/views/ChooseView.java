package ru.sbt.integration.orchestration.mapper.views;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;
import ru.sbt.integration.orchestration.mapper.ChooseViewController;
import ru.sbt.integration.orchestration.mapper.MainUI;
import ru.sbt.integration.orchestration.mapper.utils.Notifications;
import ru.sbt.integration.orchestration.exception.FailedReadResourceException;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by sbt-shipov-ev on 21.09.2017.
 * Окно выбора классов
 */
public class ChooseView extends VerticalLayout implements View {
    private final ChooseViewController chooseViewController;

    // TODO: 09.04.2018 KILL after testing
    private static List<Class<?>> sourceClasses;
    private static Class<?> destinationClass;
    private static List<Class<?>> destinationListClasses;

    ChooseView(Navigator navigator, Set<MavenArtifact> sources, MavenArtifact destination, Set<String> repos) throws FailedReadResourceException, DependencyLoaderException {
        this.chooseViewController = new ChooseViewController(sources, destination, repos);
        setSizeFull();

        Image label = getLabel();
        label.setWidth(40, Unit.PERCENTAGE);

        HorizontalLayout listsLayout = createListsLayout();
        listsLayout.setWidth(70, Unit.PERCENTAGE);
        HorizontalLayout navigateButtons = createNavigateButtons(navigator);

        addComponent(label);
        addComponent(listsLayout);
        addComponent(navigateButtons);
        setComponentAlignment(label, Alignment.TOP_CENTER);
        setComponentAlignment(listsLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(navigateButtons, Alignment.BOTTOM_CENTER);
        setExpandRatio(label, 0.3f);
        setExpandRatio(listsLayout, 0.4f);
        setExpandRatio(navigateButtons, 0.3f);
    }

    private HorizontalLayout createListsLayout() throws DependencyLoaderException {
        HorizontalLayout layout = new HorizontalLayout();
        ListSelect<Class<?>> sourceList = new ListSelect<>("Select source class");
        sourceList.setWidth(100, Unit.PERCENTAGE);
        ListSelect<Class<?>> destinationList = new ListSelect<>("Select destination class");
        destinationList.setWidth(100, Unit.PERCENTAGE);
        sourceList.setItems(chooseViewController.getSourceArtifactClassesList()
                .stream()
                .filter(clazz -> !clazz.isPrimitive())
                .filter(clazz -> !clazz.isInterface()));
        destinationList.setItems(chooseViewController.getDestinationArtifactClassesList()
                .stream()
                .filter(clazz -> !clazz.isPrimitive())
                .filter(clazz -> !clazz.isInterface()));

        layout.addComponent(sourceList, 0);
        layout.addComponent(destinationList, 1);
        layout.setComponentAlignment(sourceList, Alignment.MIDDLE_RIGHT);
        layout.setComponentAlignment(destinationList, Alignment.MIDDLE_LEFT);
        sourceList.addSelectionListener(chooseViewController::setSourceItemSelected);
        destinationList.addSelectionListener(chooseViewController::setDestinationItemSelected);

        return layout;
    }

    @NotNull
    private HorizontalLayout createNavigateButtons(Navigator navigator) {
        Button backButton = createBackButton(navigator);
        Button nextButton = createNextButton(navigator);

        HorizontalLayout nextButtonLayout = new HorizontalLayout();
        nextButtonLayout.setWidth(100, Unit.PERCENTAGE);
        nextButtonLayout.addComponent(nextButton);
        nextButtonLayout.setComponentAlignment(nextButton, Alignment.MIDDLE_RIGHT);

        HorizontalLayout previousButtonLayout = new HorizontalLayout();
        previousButtonLayout.setWidth(100, Unit.PERCENTAGE);
        previousButtonLayout.addComponent(backButton);
        previousButtonLayout.setComponentAlignment(backButton, Alignment.MIDDLE_LEFT);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSizeUndefined();
        footer.setSizeFull();
        footer.addComponent(previousButtonLayout);
        footer.setComponentAlignment(previousButtonLayout, Alignment.BOTTOM_LEFT);
        footer.addComponent(nextButtonLayout);
        footer.setComponentAlignment(nextButtonLayout, Alignment.BOTTOM_RIGHT);
        footer.setStyleName("custom-margin");
        return footer;
    }

    @NotNull
    private Button createBackButton(Navigator navigator) {
        Button backButton = new Button("Back", event -> navigator.navigateTo(MainUI.TITLE_VIEW));
        backButton.setWidth(10, Unit.PICAS);
        return backButton;
    }

    @NotNull
    private Button createNextButton(Navigator navigator) {
        Button nextWindowButton = new Button("Next", event -> {
            try {
                //todo почему-то загрузка классов на chooseView, а должна была пройти на TitleView
                sourceClasses = chooseViewController.getSourceClasses();
                destinationClass = chooseViewController.getDestinationClass();
                destinationListClasses = chooseViewController.getDestinationArtifactClassesList();

                // TODO: 09.04.2018 Kill after Testing

                if (chooseViewController.getSourceClasses() == null || chooseViewController.getDestinationClass() == null) {
                    Notifications.ERROR_NOTIFICATION.show("Please, choose classes");
                } else {
                    navigator.addView(MainUI.MAPPING_VIEW, new MappingView(
                            navigator,
                            chooseViewController.getSourceClasses(),
                            chooseViewController.getDestinationClass(),
                            chooseViewController.getDestinationArtifactClassesList(),
                            false
                    ));
                    navigator.navigateTo(MainUI.MAPPING_VIEW);
                }
            } catch (DependencyLoaderException e) {
                e.printStackTrace();
                Notifications.ERROR_NOTIFICATION.show(e.getMessage());
            }
        });
        nextWindowButton.setWidth(10, Unit.PICAS);
        return nextWindowButton;
    }

    @NotNull
    private Image getLabel() {
        String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        return new Image("", new FileResource(new File(basePath +
                "/resources/orc-logo.png")));
    }

    // TODO: 09.04.2018 Kill after Testing
    public static List<Class<?>> getSourceClasses() {
        return sourceClasses;
    }

    // TODO: 09.04.2018 Kill after Testing
    public static Class<?> getDestinationClass() {
        return destinationClass;
    }

    public static List<Class<?>> getDestinationArtifactListClasses() {
        return destinationListClasses;
    }
}
