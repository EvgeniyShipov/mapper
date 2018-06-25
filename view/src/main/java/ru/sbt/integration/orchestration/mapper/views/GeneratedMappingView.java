package ru.sbt.integration.orchestration.mapper.views;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.mapper.MappingViewController;
import ru.sbt.integration.orchestration.mapper.generator.JavaFileMappingGenerator;
import ru.sbt.integration.orchestration.mapper.generator.JavaFileMappingGenerator.MappingStyle;
import ru.sbt.integration.orchestration.mapper.generator.MappingGenerator;
import ru.sbt.integration.orchestration.mapper.generator.XmlFileMappingGenerator;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.SessionRecoverServiceImpl;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions.SessionRecoverException;
import ru.sbt.integration.orchestration.mapper.utils.Notifications;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static ru.sbt.integration.orchestration.mapper.MainUI.MAPPING_VIEW;

/**
 * Окно отображающее сгенерированный маппинг в формате XML или java
 */
public class GeneratedMappingView extends VerticalLayout implements View {
    private MappingGenerator generator;
    private String fileName;

    GeneratedMappingView(MultipleMappingModel model, Navigator navigator) {
        setSizeFull();
        generator = new XmlFileMappingGenerator(model);
        generateMapping();

        Label label = new Label("<h1>Generated mapping code<h1>", ContentMode.HTML);
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        TextArea area = createTextArea();
        area.setWidth(70, Unit.PERCENTAGE);
        area.setRows(20);
        RadioButtonGroup<String> radioFileChooser = createRadioButtonGroup(model, area);

        mainLayout.addComponent(area);
        mainLayout.addComponent(radioFileChooser);
        mainLayout.setExpandRatio(area, 3);
        mainLayout.setExpandRatio(radioFileChooser, 1);
        mainLayout.setComponentAlignment(area, Alignment.MIDDLE_RIGHT);
        mainLayout.setComponentAlignment(radioFileChooser, Alignment.TOP_LEFT);

        HorizontalLayout navigateButtons = createNavigateButtons(navigator);

        Button recoveryButton = recoveryButton(navigator, model);

        addComponent(label);
        addComponent(mainLayout);
        addComponent(recoveryButton);
        addComponent(navigateButtons);
        setComponentAlignment(label, Alignment.TOP_CENTER);
        setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
        setComponentAlignment(recoveryButton, Alignment.BOTTOM_CENTER);
        setComponentAlignment(navigateButtons, Alignment.BOTTOM_CENTER);
        setExpandRatio(mainLayout, 2);
        setExpandRatio(label, 1);
        setExpandRatio(navigateButtons, 1);
    }

    @NotNull
    private HorizontalLayout createNavigateButtons(Navigator navigator) {
        Button backButton = createBackButton(navigator);
        Button downloadButton = downloadButton();

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSizeFull();
        buttons.addComponent(backButton);
        buttons.addComponent(downloadButton);
        buttons.setComponentAlignment(backButton, Alignment.BOTTOM_LEFT);
        buttons.setComponentAlignment(downloadButton, Alignment.BOTTOM_RIGHT);
        return buttons;
    }

    @NotNull
    private RadioButtonGroup<String> createRadioButtonGroup(MultipleMappingModel models, TextArea area) {
        RadioButtonGroup<String> radioFileChooser = new RadioButtonGroup<>("File format");
        radioFileChooser.setItems("Java 8", "Java 7", "XML");
        radioFileChooser.setValue("XML");
        radioFileChooser.addValueChangeListener(event -> {
            if (event.getValue().equals("Java 8")) {
                generator = new JavaFileMappingGenerator(models, MappingStyle.OPTIONAL);
                fileName = "Mapping.java";
            } else if (event.getValue().equals("Java 7")) {
                generator = new JavaFileMappingGenerator(models, MappingStyle.PLAIN_JAVA);
                fileName = "Mapping.java";
            } else if (event.getValue().equals("XML")) {
                generator = new XmlFileMappingGenerator(models);
                fileName = "Mapping.xml";
            }
            generateMapping();
            area.setValue(generator.getGeneratedCode());
        });
        return radioFileChooser;
    }

    private void generateMapping() {
        try {
            generator.generate();
        } catch (Exception e) {
            e.printStackTrace();
            Notifications.ERROR_NOTIFICATION.show("Mapping was failed", e.getMessage());
        }
    }

    @NotNull
    private TextArea createTextArea() {
        TextArea area = new TextArea();
        area.setValue(generator.getGeneratedCode());
        return area;
    }

    private Button createBackButton(Navigator navigator) {
        Button backButton = new Button("Back", event -> navigator.navigateTo(MAPPING_VIEW));
        backButton.setWidth(10, Unit.PICAS);
        return backButton;
    }

    private Button downloadButton() {
        Button downloadButton = new Button("Download file");
        downloadButton.setWidth(15, Unit.PICAS);
        StreamResource resource = new StreamResource(this::getStream, fileName);
        FileDownloader downloader = new FileDownloader(resource);
        downloader.extend(downloadButton);
        return downloadButton;
    }

    private InputStream getStream() {
        return new ByteArrayInputStream(generator.getGeneratedCode().getBytes());
    }

    // TODO: 09.04.2018 KILL after testing
    private Button recoveryButton(Navigator navigator, MultipleMappingModel model) {
        Button button = new Button("Recovery", event -> {
            generator = new XmlFileMappingGenerator(model);
            generateMapping();
            MappingViewController mappingViewController;
            try {
                mappingViewController = new SessionRecoverServiceImpl().recoverSession(
                        ChooseView.getSourceClasses(),
                        ChooseView.getDestinationClass(),
                        ChooseView.getDestinationArtifactListClasses(),
                        generator.getGeneratedCode());
            } catch (SessionRecoverException e) {
                Notifications.ERROR_NOTIFICATION.show("Не удалось восстановить сессию -\n" + e.getMessage());
                mappingViewController = new MappingViewController(ChooseView.getSourceClasses(), ChooseView.getDestinationClass(), ChooseView.getDestinationArtifactListClasses());
            }
            navigator.addView(MAPPING_VIEW, new MappingView(navigator, mappingViewController, false));
            navigator.navigateTo(MAPPING_VIEW);
        });
        button.setWidth(10, Unit.PICAS);
        return button;
    }
}