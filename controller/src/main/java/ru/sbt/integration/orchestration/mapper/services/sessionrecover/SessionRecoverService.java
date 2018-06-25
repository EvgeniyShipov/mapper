package ru.sbt.integration.orchestration.mapper.services.sessionrecover;

import ru.sbt.integration.orchestration.mapper.MappingViewController;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions.SessionRecoverException;

import java.util.List;

public interface SessionRecoverService {

    MappingViewController recoverSession(List<Class<?>> sourceClass, Class destinationClass, List<Class<?>> destinationArtifactListClasses, String xml) throws SessionRecoverException;
}
