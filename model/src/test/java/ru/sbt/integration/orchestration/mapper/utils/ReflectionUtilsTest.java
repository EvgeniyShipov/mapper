package ru.sbt.integration.orchestration.mapper.utils;

import org.junit.Assert;
import org.junit.Test;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;

import static ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils.*;

public class ReflectionUtilsTest {

    @Test
    public void testGetFullChildGenericSignature() {
        Assert.assertEquals("java.util.ArrayList<java.util.HashMap<java.lang.String,java.lang.Object>>",
                getFullChildGenericSignature("java.util.HashMap<java.lang.String, java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>>", CollectionNode.CollectionType.MAP));

        Assert.assertEquals("java.util.HashMap<java.lang.String,java.lang.Object>",
                getFullChildGenericSignature("java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>", CollectionNode.CollectionType.LIST));

        Assert.assertEquals("java.lang.String", getFullChildGenericSignature("java.utils.List<java.lang.String>", CollectionNode.CollectionType.LIST));

        Assert.assertEquals("java.lang.String", getFullChildGenericSignature("java.utils.Map<java.lang.Integer, java.lang.String>", CollectionNode.CollectionType.MAP));
    }

    @Test
    public void testCutPackages() {
        Assert.assertEquals("List<String>", cutPackages("java.utils.List<java.lang.String>"));
        Assert.assertEquals("List<String>", cutPackages(cutPackages("java.utils.List<java.lang.String>")));

        Assert.assertEquals("Map<Integer,String>", cutPackages("java.utils.Map<java.lang.Integer, java.lang.String>"));
        Assert.assertEquals("Map<Integer,String>", cutPackages(cutPackages("java.utils.Map<java.lang.Integer, java.lang.String>")));

        Assert.assertEquals("ArrayList<HashMap<String,Object>>", cutPackages("java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>"));
        Assert.assertEquals("ArrayList<HashMap<String,Object>>", cutPackages(cutPackages("java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>")));
    }
    @Test
    public void testGetClassSignatureWithoutGenerics() {
        Assert.assertEquals("java.utils.List", getClassSignatureWithoutGenerics("java.utils.List<java.lang.String>"));

        Assert.assertEquals("java.utils.List", getClassSignatureWithoutGenerics("java.utils.List"));

        Assert.assertEquals("java.util.HashMap",
                getClassSignatureWithoutGenerics("java.util.HashMap<java.lang.String, java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>>"));
    }
}
