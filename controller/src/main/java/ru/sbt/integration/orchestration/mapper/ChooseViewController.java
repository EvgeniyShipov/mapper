package ru.sbt.integration.orchestration.mapper;

import com.vaadin.event.selection.MultiSelectionEvent;
import ru.sbt.integration.orchestration.classloader.ArtifactClassLoader;
import ru.sbt.integration.orchestration.classloader.ArtifactClassLoaderImpl;
import ru.sbt.integration.orchestration.dependency.MavenArtifact;
import ru.sbt.integration.orchestration.exception.DependencyLoaderException;
import ru.sbt.integration.orchestration.exception.FailedReadResourceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sbt-shipov-ev on 21.09.2017.
 */
public class ChooseViewController {
    private final ArtifactClassLoader loader;
    private final Set<MavenArtifact> sources;
    private final MavenArtifact destination;
    private final Set<String> repos;

    public ChooseViewController(Set<MavenArtifact> sources, MavenArtifact destination, Set<String> repos) throws FailedReadResourceException {
        this.sources = sources;
        this.destination = destination;
        this.repos = repos;
        loader = ArtifactClassLoaderImpl.getInstance();
    }

    private final List<Class<?>> sourceClasses = new ArrayList<>();
    private Class<?> destinationClass;

    public List<Class<?>> getSourceClasses() {
        return sourceClasses;
    }

    public Class<?> getDestinationClass() {
        return destinationClass;
    }

    public void setSourceItemSelected(MultiSelectionEvent<Class<?>> event) {
        sourceClasses.clear();
        sourceClasses.addAll(event.getAllSelectedItems());
    }

    public void setDestinationItemSelected(MultiSelectionEvent<Class<?>> event) {
        destinationClass = event.getFirstSelectedItem().orElse(null);
    }

    public List<Class<?>> getSourceArtifactClassesList() throws DependencyLoaderException {
        return getArtifactClassesList(sources);
    }

    public List<Class<?>> getDestinationArtifactClassesList() throws DependencyLoaderException {
        Set<MavenArtifact> artifacts = new HashSet<>();
        artifacts.add(destination);
        return getArtifactClassesList(artifacts);
    }

    private List<Class<?>> getArtifactClassesList(Set<MavenArtifact> artifacts) throws DependencyLoaderException {
        try {
            return loader.getClasses(artifacts, repos);
        } catch (IOException e) {
            e.printStackTrace();
            //todo можно откинуть уведомление
            return new ArrayList<>();
        }
    }

    public Set<MavenArtifact> getSources() {
        return sources;
    }

    public MavenArtifact getDestination() {
        return destination;
    }

    public Set<String> getRepos() {
        return repos;
    }
}
