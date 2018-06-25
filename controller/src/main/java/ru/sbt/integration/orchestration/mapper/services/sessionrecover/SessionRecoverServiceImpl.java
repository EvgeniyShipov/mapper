package ru.sbt.integration.orchestration.mapper.services.sessionrecover;

import ru.sbt.integration.orchestration.mapper.MappingViewController;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions.SessionRecoverException;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.sax.DozerXMLHandler;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.sax.ReadXMLFileSAX;

import java.util.List;

public class SessionRecoverServiceImpl implements SessionRecoverService {

    @Override
    public MappingViewController recoverSession(List<Class<?>> sourceClass, Class destinationClass,
                                                List<Class<?>> destinationArtifactListClasses, String xml) throws SessionRecoverException {
        if (sourceClass == null || destinationClass == null || sourceClass.isEmpty()) {
            throw new SessionRecoverException((sourceClass == null ? "sourceClass is null\n" : "\n") +
                    (destinationClass == null ? "destinationClass is null\n" : "\n") +
                    (sourceClass.isEmpty() ? "sourceClass is empty\n" : "\n"));
        }


        DozerXMLHandler handler = new DozerXMLHandler(sourceClass, destinationClass, destinationArtifactListClasses);
        ReadXMLFileSAX.parseXML(handler, xml);
        MultipleMappingModel model = handler.getModel();
        return new MappingViewController(model);
    }
}
