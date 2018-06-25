package ru.sbt.integration.orchestration.mapper.mapping;

import ru.sbt.integration.orchestration.mapper.model.FieldNode;

import java.util.Objects;

/**
 * Created by sbt-shipov-ev on 15.01.2018.
 */
public class MappingPair {

    private final FieldNode source;
    private final FieldNode destination;
    private String sourceDescription;
    private String destinationDescription;

    public MappingPair(FieldNode source, FieldNode destination) {
        this.source = source;
        this.destination = destination;
    }

    public FieldNode getSource() {
        return source;
    }

    public FieldNode getDestination() {
        return destination;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    public String getDestinationDescription() {
        return destinationDescription;
    }

    public void setDestinationDescription(String destinationDescription) {
        this.destinationDescription = destinationDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappingPair that = (MappingPair) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }
}
