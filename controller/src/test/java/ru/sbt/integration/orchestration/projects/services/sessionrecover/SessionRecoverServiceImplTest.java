package ru.sbt.integration.orchestration.projects.services.sessionrecover;

import com.sbt.bm.rdm.api.CibOrganization;
import org.apache.maven.model.Organization;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.MappingViewController;
import ru.sbt.integration.orchestration.mapper.generator.MappingGenerator;
import ru.sbt.integration.orchestration.mapper.generator.XmlFileMappingGenerator;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.SessionRecoverService;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.SessionRecoverServiceImpl;
import ru.sbt.integration.orchestration.projects.teststruct.DestinationClass;
import ru.sbt.integration.orchestration.projects.teststruct.OneMoreSourceClass;
import ru.sbt.integration.orchestration.projects.teststruct.SourceClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionRecoverServiceImplTest {

    @Test
    public void recoverSessionTest() throws Exception {
        List<Class<?>> sourceClasses = new ArrayList<>();
        sourceClasses.add(Organization.class);
        Class destinationClass = CibOrganization.class;
        String xmlDozer =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<mappings xmlns=\"http://dozer.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://dozer.sourceforge.net http://dozer.sourceforge.net/schema/beanmapping.xsd\">\n" +
                        "    <mapping date-format=\"yyyy-MM-dd'T'HH:mm s.SSSXXX\" wildcard=\"false\">\n" +
                        "        <class-a>org.apache.maven.model.Organization</class-a>\n" +
                        "        <class-b>com.sbt.bm.rdm.api.CibOrganization</class-b>\n" +
                        "        <field>\n" +
                        "            <a>url</a>\n" +
                        "            <b>organization.segment</b>\n" +
                        "        </field>\n" +
                        "        <field>\n" +
                        "            <a>name</a>\n" +
                        "            <b>organization.clientCategory</b>\n" +
                        "        </field>\n" +
                        "    </mapping>\n" +
                        "</mappings>\n";


        SessionRecoverService sessionRecoverService = new SessionRecoverServiceImpl();
        MappingViewController mappingViewController = sessionRecoverService.recoverSession(sourceClasses, destinationClass, Collections.EMPTY_LIST, xmlDozer);
        Assert.assertEquals(2, mappingViewController.getModel().getMappingPairs().size());
        Assert.assertEquals(CibOrganization.class.getDeclaredField("organization").getType().getDeclaredField("segment"),
                mappingViewController.getModel().getMappingPairs().stream().findFirst().get().getDestination().getField());

        MappingGenerator mappingGenerator = new XmlFileMappingGenerator(mappingViewController.getModel());
        mappingGenerator.generate();
        Assert.assertEquals(xmlDozer, mappingGenerator.getGeneratedCode().replaceAll("\r", ""));
    }

    @Test
    public void recoverSessionMapTest() throws Exception {
        List<Class<?>> sourceClasses = new ArrayList<>();
        sourceClasses.add(SourceClass.class);
        Class destinationClass = DestinationClass.class;
        String xmlDozer =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<mappings xmlns=\"http://dozer.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://dozer.sourceforge.net http://dozer.sourceforge.net/schema/beanmapping.xsd\">\n" +
                        "    <mapping date-format=\"yyyy-MM-dd'T'HH:mm s.SSSXXX\" wildcard=\"false\">\n" +
                        "        <class-a>ru.sbt.integration.orchestration.projects.teststruct.SourceClass</class-a>\n" +
                        "        <class-b>ru.sbt.integration.orchestration.projects.teststruct.DestinationClass</class-b>\n" +
                        "        <field>\n" +
                        "            <a>parentArrayList1[0]</a>\n" +
                        "            <b>parentHashMap2</b>\n" +
                        "            <a-hint>java.util.HashMap</a-hint>\n" +
                        "        </field>\n" +
                        "    </mapping>\n" +
                        "</mappings>\n";


        SessionRecoverService sessionRecoverService = new SessionRecoverServiceImpl();
        MappingViewController mappingViewController = sessionRecoverService.recoverSession(sourceClasses, destinationClass, Collections.EMPTY_LIST, xmlDozer);

        MappingGenerator mappingGenerator = new XmlFileMappingGenerator(mappingViewController.getModel());
        mappingGenerator.generate();
        Assert.assertEquals(xmlDozer, mappingGenerator.getGeneratedCode().replaceAll("\r", ""));
    }

    @Test
    public void recoverSessionListTest() throws Exception {
        List<Class<?>> sourceClasses = new ArrayList<>();
        sourceClasses.add(SourceClass.class);
        Class destinationClass = DestinationClass.class;
        String xmlDozer =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<mappings xmlns=\"http://dozer.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://dozer.sourceforge.net http://dozer.sourceforge.net/schema/beanmapping.xsd\">\n" +
                        "    <mapping date-format=\"yyyy-MM-dd'T'HH:mm s.SSSXXX\" wildcard=\"false\">\n" +
                        "        <class-a>ru.sbt.integration.orchestration.projects.teststruct.SourceClass</class-a>\n" +
                        "        <class-b>ru.sbt.integration.orchestration.projects.teststruct.DestinationClass</class-b>\n" +
                        "        <field>\n" +
                        "            <a>parentString1</a>\n" +
                        "            <b>strings2[0]</b>\n" +
                        "            <b-hint>java.lang.String</b-hint>\n" +
                        "        </field>\n" +
                        "        <field>\n" +
                        "            <a>parentString2</a>\n" +
                        "            <b>strings2[1]</b>\n" +
                        "            <b-hint>java.lang.String</b-hint>\n" +
                        "        </field>\n" +
                        "    </mapping>\n" +
                        "</mappings>\n";


        SessionRecoverService sessionRecoverService = new SessionRecoverServiceImpl();
        MappingViewController mappingViewController = sessionRecoverService.recoverSession(sourceClasses, destinationClass, Collections.EMPTY_LIST, xmlDozer);

        MappingGenerator mappingGenerator = new XmlFileMappingGenerator(mappingViewController.getModel());
        mappingGenerator.generate();
        Assert.assertEquals(xmlDozer, mappingGenerator.getGeneratedCode().replaceAll("\r", ""));
    }

    @Test
    public void recoverSessionSourceArtifactListTest() throws Exception {
        List<Class<?>> sourceClasses = new ArrayList<>();
        sourceClasses.add(SourceClass.class);
        sourceClasses.add(OneMoreSourceClass.class);
        Class destinationClass = DestinationClass.class;
        String xmlDozer =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<mappings xmlns=\"http://dozer.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://dozer.sourceforge.net http://dozer.sourceforge.net/schema/beanmapping.xsd\">\n" +
                        "    <mapping date-format=\"yyyy-MM-dd'T'HH:mm s.SSSXXX\" wildcard=\"false\">\n" +
                        "        <class-a>ru.sbt.integration.orchestration.projects.teststruct.SourceClass</class-a>\n" +
                        "        <class-b>ru.sbt.integration.orchestration.projects.teststruct.DestinationClass</class-b>\n" +
                        "        <field>\n" +
                        "            <a>parentString1</a>\n" +
                        "            <b>strings2[0]</b>\n" +
                        "            <b-hint>java.lang.String</b-hint>\n" +
                        "        </field>\n" +
                        "        <field>\n" +
                        "            <a>parentString2</a>\n" +
                        "            <b>strings2[1]</b>\n" +
                        "            <b-hint>java.lang.String</b-hint>\n" +
                        "        </field>\n" +
                        "    </mapping>\n" +
                        "    <mapping date-format=\"yyyy-MM-dd'T'HH:mm s.SSSXXX\" wildcard=\"false\">\n" +
                        "        <class-a>ru.sbt.integration.orchestration.projects.teststruct.OneMoreSourceClass</class-a>\n" +
                        "        <class-b>ru.sbt.integration.orchestration.projects.teststruct.DestinationClass</class-b>\n" +
                        "        <field>\n" +
                        "            <a>stringField</a>\n" +
                        "            <b>strings2[2]</b>\n" +
                        "            <b-hint>java.lang.String</b-hint>\n" +
                        "        </field>\n" +
                        "    </mapping>\n" +
                        "</mappings>\n";


        SessionRecoverService sessionRecoverService = new SessionRecoverServiceImpl();
        MappingViewController mappingViewController = sessionRecoverService.recoverSession(sourceClasses, destinationClass, Collections.EMPTY_LIST, xmlDozer);

        MappingGenerator mappingGenerator = new XmlFileMappingGenerator(mappingViewController.getModel());
        mappingGenerator.generate();
        Assert.assertEquals(xmlDozer, mappingGenerator.getGeneratedCode().replaceAll("\r", ""));
    }
}
