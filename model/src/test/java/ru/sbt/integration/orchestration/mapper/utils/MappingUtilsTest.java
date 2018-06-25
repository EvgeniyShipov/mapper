package ru.sbt.integration.orchestration.mapper.utils;

import com.sbt.bm.ucp.corp.model.party.organization.Organization;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.model.FieldTreeModel;
import ru.sbt.integration.orchestration.mapper.model.TreeModel;

import java.lang.reflect.Field;

public class MappingUtilsTest {
    private static TreeModel model;

    @BeforeClass
    public static void init() {
        model = new FieldTreeModel(Organization.class);
    }

    @Test
    public void isAssignable() throws Exception {
        Field field0 = FieldUtils.getAllFields(Integer.class)[0];
        Field field1 = FieldUtils.getAllFields(Integer.class)[3];
        Field field2 = FieldUtils.getAllFields(String.class)[0];
        FieldNode node0 = FieldNode.createNewFieldNode(field0, null);
        FieldNode node1 = FieldNode.createNewFieldNode(field1, null);
        FieldNode node2 = FieldNode.createNewFieldNode(field2, null);
        Assert.assertNotNull(node0);
        Assert.assertNotNull(node1);
        Assert.assertNotNull(node2);
        Assert.assertEquals(true, MappingUtils.isTrivialType(node0));
        Assert.assertEquals(true, MappingUtils.isTrivialType(node1));
        Assert.assertEquals(true, MappingUtils.isTrivialType(node2));
    }

}