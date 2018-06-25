package ru.sbt.integration.orchestration.mapper.mapping;

import com.sbt.bm.rdm.api.CibOrganization;
import com.sbt.bm.ucp.corp.model.party.organization.Organization;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.generator.JavaFileMappingGenerator;
import ru.sbt.integration.orchestration.mapper.generator.JavaFileMappingGenerator.MappingStyle;
import ru.sbt.integration.orchestration.mapper.generator.MappingGenerator;

import java.lang.reflect.Field;
import java.util.Collections;

public class  JavaFileMappingGeneratorTest {

    @Test
    public void generatorTest() throws Exception {
        Field fieldSource1 = FieldUtils.getAllFields(Organization.class)[2].getType().getDeclaredFields()[0];
        Field fieldSource2 = FieldUtils.getAllFields(Organization.class)[2].getType().getDeclaredFields()[1];
        Field fieldDestination1 = FieldUtils.getAllFields(CibOrganization.class)[2].getType().getDeclaredFields()[2];
        Field fieldDestination2 = FieldUtils.getAllFields(CibOrganization.class)[2].getType().getDeclaredFields()[1];

        MultipleMappingModel model = new MultipleMappingModel(Organization.class, CibOrganization.class, Collections.EMPTY_LIST);
        model.addMapping(Organization.class, fieldSource1, fieldDestination1);
        model.addMapping(Organization.class, fieldSource2, fieldDestination2);

        MappingGenerator generator = new JavaFileMappingGenerator(model, MappingStyle.OPTIONAL);
        generator.generate();
        System.out.println(generator.getGeneratedCode());
    }
}