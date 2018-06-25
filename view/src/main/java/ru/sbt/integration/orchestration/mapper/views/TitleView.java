package ru.sbt.integration.orchestration.mapper.views;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;
import ru.sbt.integration.orchestration.mapper.MainUI;
import ru.sbt.integration.orchestration.mapper.TitleViewController;
import ru.sbt.integration.orchestration.mapper.utils.Notifications;
import ru.sbt.integration.orchestration.exception.FailedReadResourceException;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Окно выбора артефактов
 */
public class TitleView extends VerticalLayout implements View {
    private final TitleViewController titleViewController = new TitleViewController();
    private final TextArea repositoriesArea = new TextArea();
    private final FormLayout sourceForm = new FormLayout();
    private final FormLayout destinationForm = new FormLayout();

    public TitleView(Navigator navigator) {
        setSizeFull();

        Image label = getLabel();
        label.setWidth(40, Unit.PERCENTAGE);

        HorizontalLayout mainLayout = createFormsLayout();
        HorizontalLayout navigateButtons = createNavigateButtons(navigator);
        configureRepositoriesArea(repositoriesArea);

        addComponent(label, 0);
        addComponent(mainLayout, 1);
        addComponent(repositoriesArea, 2);
        addComponent(navigateButtons, 3);
        setExpandRatio(label, 0.25f);
        setExpandRatio(mainLayout, 0.35f);
        setExpandRatio(repositoriesArea, 0.30f);
        setExpandRatio(navigateButtons, 0.1f);
        setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(repositoriesArea, Alignment.MIDDLE_CENTER);
        setComponentAlignment(label, Alignment.TOP_CENTER);
    }

    private HorizontalLayout createFormsLayout() {
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth(100, Unit.PERCENTAGE);
        Panel sourcePanel = new Panel("Source artifact");
        Panel destinationPanel = new Panel("Destination artifact");

        HorizontalLayout sourceLayout = new HorizontalLayout();
        sourceLayout.setWidth(100, Unit.PERCENTAGE);
        sourcePanel.setWidth(40, Unit.PERCENTAGE);
        sourceLayout.setMargin(true);
        configureForm(sourceForm);
        sourceLayout.addComponent(sourceForm);
        sourcePanel.setContent(sourceLayout);

        HorizontalLayout addButton = new HorizontalLayout();
        Button button = new Button("+", event -> {
            if (getArtifact(sourceForm).isPresent()) {
                Notifications.ACTION_NOTIFICATION.show(String.format("Artifact %s success added", ((TextField) sourceForm.getComponent(1)).getValue()));
                titleViewController.addSourceArtifact(getArtifact(sourceForm).get());
                sourceForm.forEach(field -> ((TextField) field).setValue(""));
            } else
                Notifications.ACTION_NOTIFICATION.show("Please fill the fields");
        });
        addButton.addComponent(button);

        HorizontalLayout destinationLayout = new HorizontalLayout();
        destinationLayout.setMargin(true);
        destinationLayout.setWidth(100, Unit.PERCENTAGE);
        destinationPanel.setWidth(40, Unit.PERCENTAGE);
        configureForm(destinationForm);
        destinationLayout.addComponent(destinationForm);
        destinationPanel.setContent(destinationLayout);

        mainLayout.addComponent(sourcePanel);
        mainLayout.addComponent(addButton);
        mainLayout.addComponent(destinationPanel);
        mainLayout.setExpandRatio(sourcePanel, 0.485f);
        mainLayout.setExpandRatio(addButton, 0.03f);
        mainLayout.setExpandRatio(destinationPanel, 0.485f);
        mainLayout.setComponentAlignment(sourcePanel, Alignment.MIDDLE_RIGHT);
        mainLayout.setComponentAlignment(addButton, Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(destinationPanel, Alignment.MIDDLE_LEFT);

        return mainLayout;
    }

    private HorizontalLayout createNavigateButtons(Navigator navigator) {
        Button backButton = createNextWindowButton(navigator);

        HorizontalLayout nextButtonLayout = new HorizontalLayout();
        nextButtonLayout.setWidth(100, Unit.PERCENTAGE);
        nextButtonLayout.addComponent(backButton);
        nextButtonLayout.setComponentAlignment(backButton, Alignment.MIDDLE_RIGHT);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSizeFull();
        footer.addComponent(nextButtonLayout);
        footer.setComponentAlignment(nextButtonLayout, Alignment.BOTTOM_RIGHT);
        footer.setStyleName("custom-margin");
        return footer;
    }

    @NotNull
    private Button createNextWindowButton(Navigator navigator) {
        Button nextWindowButton = new Button("Next", event -> {
            try {
                getArtifacts();
                navigator.addView(MainUI.CHOOSE_VIEW, new ChooseView(navigator,
                        titleViewController.getSourceArtifacts(),
                        titleViewController.getDestinationArtifact(),
                        titleViewController.getRepositories()));
                navigator.navigateTo(MainUI.CHOOSE_VIEW);
            }catch (FailedReadResourceException e) {
                titleViewController.clear();
                Notifications.ERROR_NOTIFICATION.show(e.getMessage());
            }
            catch (DependencyLoaderException e) {
                titleViewController.clear();
                Notifications.ERROR_NOTIFICATION.show("Ошибка загрузки артефакта:", e.getMessage());
            }
        });
        nextWindowButton.setWidth(10, Unit.PICAS);
        addComponent(nextWindowButton);
        return nextWindowButton;
    }

    @NotNull
    private Image getLabel() {
        String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        return new Image("", new FileResource(new File(basePath +
                "/resources/orc-logo.png")));
    }

    private void getArtifacts() {
        getArtifact(sourceForm).ifPresent(titleViewController::addSourceArtifact);
        getArtifact(destinationForm).ifPresent(titleViewController::setDestinationArtifact);
        titleViewController.setRepositories(getRepositories());
    }

    private Optional<MavenArtifact> getArtifact(FormLayout form) {
        String groupID = ((TextField) form.getComponent(0)).getValue();
        String artifactID = ((TextField) form.getComponent(1)).getValue();
        String version = ((TextField) form.getComponent(2)).getValue();
        if (groupID.isEmpty() || artifactID.isEmpty() || version.isEmpty())
            return Optional.empty();
        return Optional.of(new MavenArtifact(groupID, artifactID, version));
    }

    private void configureForm(FormLayout form) {
        form.setMargin(false);
        TextField groupID = new TextField("Group ID", "com.sbt.pprb.dto");
//        groupID.setPlaceholder("Group ID");
        TextField artifactID = new TextField("Artifact ID", "creditcardstatement-dto");
//        artifactID.setPlaceholder("Artifact ID");
        TextField version = new TextField("Version", "1.0.2");
//        version.setPlaceholder("Version");
//        TextField groupID = new TextField("Group ID", "com.sbt.appcore");
//        TextField artifactID = new TextField("Artifact ID", "service-api");
//        TextField version = new TextField("Version", "7.1_4.27.1_5.3.29");
        groupID.setWidth(100, Unit.PERCENTAGE);
        artifactID.setWidth(100, Unit.PERCENTAGE);
        version.setWidth(100, Unit.PERCENTAGE);
        form.addComponent(groupID, 0);
        form.addComponent(artifactID, 1);
        form.addComponent(version, 2);
    }

    private void configureRepositoriesArea(TextArea area) {
        area.setCaption("Repositories");
        area.setWidth(42, Unit.PERCENTAGE);
        area.setHeight(80, Unit.PERCENTAGE);
        area.setValue("http://sbtnexus.ca.sbrf.ru:8081/nexus/content/repositories/INTLAB_release");
    }

    private Set<String> getRepositories() {
        return new HashSet<>(Arrays.asList(repositoriesArea.getValue().split("\\n")));
    }
}
