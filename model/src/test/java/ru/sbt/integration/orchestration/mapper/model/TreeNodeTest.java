package ru.sbt.integration.orchestration.mapper.model;

import com.sbt.bm.rdm.api.CibOrganization;
import com.sbt.bm.ucp.corp.model.party.organization.Organization;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TreeNodeTest {
    private static TreeModel modelSource1;
    private static TreeModel modelDest;

    @BeforeClass
    public static void init() {
        modelSource1 = new FieldTreeModel(Organization.class);
        modelDest = new FieldTreeModel(CibOrganization.class);
    }

    @Test
    public void testObjectModel() {
        Field fieldSource = FieldUtils.getAllFields(Organization.class)[1];
        FieldNode searchedNodeSource = modelSource1.getNode(fieldSource);
        FieldNode searched = modelSource1.getNode("oldCode");
        Assert.assertNotNull(searchedNodeSource);
        Assert.assertNotNull(searched);
        Assert.assertEquals(fieldSource, searchedNodeSource.getField());

        Field fieldDestination = FieldUtils.getAllFields(CibOrganization.class)[2].getType().getDeclaredFields()[3];
        FieldNode searchedNodeDest = modelDest.getNode(fieldDestination);
        Assert.assertNotNull(searchedNodeDest);
        Assert.assertEquals(fieldDestination, searchedNodeDest.getField());

        Method methodSource = searchedNodeSource.getGetter();
        Method methodDest = searchedNodeDest.getSetter();
        Assert.assertNotNull(methodSource);
        Assert.assertNotNull(methodDest);

        System.out.println("end");
    }
}