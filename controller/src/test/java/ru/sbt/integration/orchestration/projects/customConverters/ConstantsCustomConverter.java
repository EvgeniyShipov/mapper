package ru.sbt.integration.orchestration.projects.customConverters;

import org.dozer.ConfigurableCustomConverter;

public class ConstantsCustomConverter implements ConfigurableCustomConverter {
    private String parameter;
    @Override
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
        return parameter;
    }
}
