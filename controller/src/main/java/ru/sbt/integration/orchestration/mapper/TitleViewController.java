package ru.sbt.integration.orchestration.mapper;

import ru.sbt.integration.orchestration.dependency.MavenArtifact;

import java.util.HashSet;
import java.util.Set;

public class TitleViewController {
    private final Set<MavenArtifact> sourceArtifacts = new HashSet<>();
    private MavenArtifact destinationArtifact;
    private Set<String> repositories;

    public Set<MavenArtifact> getSourceArtifacts() {
        return new HashSet<>(sourceArtifacts);
    }

    public MavenArtifact getDestinationArtifact() {
        return destinationArtifact;
    }

    public Set<String> getRepositories() {
        return repositories;
    }

    public void addSourceArtifact(MavenArtifact sourceArtifact) {
        sourceArtifacts.add(sourceArtifact);
    }

    public void setDestinationArtifact(MavenArtifact destinationArtifact) {
        this.destinationArtifact = destinationArtifact;
    }

    public void setRepositories(Set<String> repositories) {
        this.repositories = repositories;
    }

    public void clear() {
        sourceArtifacts.clear();
        destinationArtifact = null;
        repositories.clear();
    }
}
