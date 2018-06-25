package ru.sbt.integration.orchestration.mapper.generator;

import com.sbt.bm.rdm.api.CibOrganization;
import com.sbt.bm.rdm.model.common.party.OrganizationCibAdditional;
import com.sbt.bm.ucp.common.model.dictionary.LegalClassification;
import com.sbt.bm.ucp.corp.model.party.organization.FinancialInstituteRequisites;
import com.sbt.bm.ucp.corp.model.party.organization.Organization;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XmlFileMappingGeneratorTest {
    @Test
    public void test() {
        List<String> stringList = new ArrayList<>();
        stringList.add("example.xml");
        Mapper mapper = new DozerBeanMapper(stringList);

        Organization org = new Organization();
        org.setLegalClassificationType(new LegalClassification());
        org.getLegalClassificationType().setDescription("TEST_SWIFT");
        org.setFinancialInstituteRequisites(new FinancialInstituteRequisites());
        org.getFinancialInstituteRequisites().setSwiftCode("TEST_SWIFT");

        CibOrganization cib = CibOrganization.builder().withCibAdditional(new OrganizationCibAdditional()).build();
        mapper.map(org, cib);

        System.out.println("End mapping");
    }

    @Test
    public void testGenerator() throws Exception{
        Field fieldSource1 = FieldUtils.getAllFields(Organization.class)[2].getType().getDeclaredFields()[0];
        Field fieldSource2 = FieldUtils.getAllFields(Organization.class)[2].getType().getDeclaredFields()[1];
        Field fieldDestination1 = FieldUtils.getAllFields(CibOrganization.class)[2].getType().getDeclaredFields()[2];
        Field fieldDestination2 = FieldUtils.getAllFields(CibOrganization.class)[2].getType().getDeclaredFields()[1];

        MultipleMappingModel model = new MultipleMappingModel(Organization.class, CibOrganization.class, Collections.EMPTY_LIST);
        model.addMapping(Organization.class, fieldSource1, fieldDestination1);
        model.addMapping(Organization.class, fieldSource2, fieldDestination2);

        MappingGenerator generator1 = new XmlFileMappingGenerator(model);
        generator1.generate();
        System.out.println(generator1.getGeneratedCode());
    }
}