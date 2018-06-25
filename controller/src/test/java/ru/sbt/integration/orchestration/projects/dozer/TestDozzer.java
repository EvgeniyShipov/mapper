package ru.sbt.integration.orchestration.projects.dozer;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Test;
import ru.sbt.integration.orchestration.projects.teststruct.DestinationClass;
import ru.sbt.integration.orchestration.projects.teststruct.InnerSourceClass;
import ru.sbt.integration.orchestration.projects.teststruct.OneMoreSourceClass;
import ru.sbt.integration.orchestration.projects.teststruct.SourceClass;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class TestDozzer {

    private DozerBeanMapper initMapper(String xml) {
        DozerBeanMapper mapper = new DozerBeanMapper();
        List<String> mappersList = new ArrayList<>();
        mappersList.add(xml);
        mapper.setMappingFiles(mappersList);

        return mapper;
    }

    @Test
    public void SimpleFieldTest() {
        DozerBeanMapper mapper = initMapper("testSimpleDozer.xml");

        SourceClass a = new SourceClass();
        a.parentString1 = "asdqw";
        DestinationClass b = new DestinationClass();
        mapper.map(a, b);
        Assert.assertEquals(a.parentString1, b.innerDestinationClass.innerString2);
    }

    @Test
    public void SimpleMapTest() {
        DozerBeanMapper mapper = initMapper("testSimpleMapDozer.xml");

        SourceClass a = new SourceClass();
        a.parentHashMap1 = new HashMap<>();
        a.parentHashMap1.put("1", "один");
        DestinationClass b = new DestinationClass();
        mapper.map(a, b);
        Assert.assertEquals("один", b.innerDestinationClass.innerString2);
    }

    @Test
    public void ObjectMapTest() {
        DozerBeanMapper mapper = initMapper("testObjectMapDozer.xml");

        SourceClass a = new SourceClass();
        a.parentHashMap1 = new HashMap<>();
        InnerSourceClass innerSourceClass = new InnerSourceClass();
        innerSourceClass.innerString1 = "inner112";
        a.parentHashMap1.put("1", innerSourceClass);
        DestinationClass b = new DestinationClass();
        mapper.map(a, b);
        Assert.assertEquals("inner112", b.innerDestinationClass.innerString2);
    }

    @Test
    public void WrappingTest() {
        DozerBeanMapper mapper = initMapper("testWrapping.xml");

        SourceClass a = new SourceClass();
        a.parentHashMap1 = new HashMap<>();
        DestinationClass b = new DestinationClass();
        a.parentString1 = "1";
        mapper.map(a, b);
        Assert.assertEquals(1, b.aByte);
        Assert.assertEquals(1, b.anInt);
        Assert.assertEquals(1, b.aShort);
        Assert.assertEquals(true, b.aBoolean);
        Assert.assertEquals(Double.valueOf(1.0D), Double.valueOf(b.aDouble));
        Assert.assertEquals(Float.valueOf(1.0F), Float.valueOf(b.aFloat));
        Assert.assertEquals(1, b.aLong);
        Assert.assertEquals(49, b.aChar);

        Assert.assertEquals(Byte.valueOf("1"), b.aByte1);
        Assert.assertEquals(Integer.valueOf(1), b.anInteger1);
        Assert.assertEquals(Short.valueOf("1"), b.aShort1);
        Assert.assertEquals(true, b.aBoolean1);
        Assert.assertEquals(Double.valueOf(1.0D), Double.valueOf(b.aDouble1));
        Assert.assertEquals(Float.valueOf(1.0F), Float.valueOf(b.aFloat1));
        Assert.assertEquals(Long.valueOf(1L), b.aLong1);
        Assert.assertEquals(Character.valueOf((char) 49), b.aChar1);

        Assert.assertEquals(BigInteger.valueOf(1L), b.bigInteger);
        Assert.assertEquals(BigDecimal.valueOf(1L), b.bigDecimal);
    }

    @Test
    public void DefaultValueTest() {
        DozerBeanMapper mapper = initMapper("testDefaultValue.xml");

        SourceClass a = new SourceClass();
        a.parentHashMap1 = new HashMap<>();
        DestinationClass b = new DestinationClass();
        a.parentString1 = "";
        Assert.assertNull(b.innerDestinationClass.innerString2);
        mapper.map(a, b);
        Assert.assertNull(b.innerDestinationClass.innerString2);
    }

    @Test
    public void MultipleSourceClassTest() {
        DozerBeanMapper mapper = initMapper("testMultipleClass.xml");

        SourceClass a = new SourceClass();
        OneMoreSourceClass a1 = new OneMoreSourceClass();
        DestinationClass b = new DestinationClass();
        a.parentString1 = "asd";
        a1.stringField = "qwe";
        mapper.map(a, b);
        mapper.map(a1, b);
        Assert.assertEquals("qwe", b.getParentString1());
        Assert.assertEquals("asd", b.getParentString2());
    }

    @Test
    public void ListMappingTest() {
        DozerBeanMapper mapper = initMapper("testListMapping.xml");

        SourceClass a = new SourceClass();
        a.getParentArrayList11().add("1");
        DestinationClass b = new DestinationClass();
        mapper.map(a, b);
        Assert.assertEquals(Integer.valueOf(1), b.integers2.get(0));
    }

    @Test
    public void ConstatntMappingTest() {
        DozerBeanMapper mapper = initMapper("testConstantMapping.xml");

        SourceClass a = new SourceClass();
        DestinationClass b = new DestinationClass();
        mapper.map(a, b);
        Assert.assertEquals("CONSTANT_VALUE", b.getParentString1());
        System.out.println("");
    }


    @Test
    public void CalendarToStringTest() {
        DozerBeanMapper mapper = initMapper("testCalendarMapping.xml");

        SourceClass a = new SourceClass();
        DestinationClass b = new DestinationClass();
        a.setCalendar(new GregorianCalendar());
        a.setDate(new Date());
        a.setParentString1("2018-07-28T18:28 9.619+03:00");
        mapper.map(a, b);
        Assert.assertEquals("Sat Jul 28 18:28:09 MSK 2018", b.getDate2().toString());
        Assert.assertNotNull(b.getCalendar());
        Assert.assertNotNull(b.getDate());
        System.out.println(b.getDate().toString());
        System.out.println(b.getCalendar().toString());

    }
    @Test
    public void FullTest() {
        DozerBeanMapper mapper = new DozerBeanMapper();
        List<String> mappersList = new ArrayList<>();
        mappersList.add("testMapping.xml");
        mapper.setMappingFiles(mappersList);

        PodamFactoryImpl podamFactory = new PodamFactoryImpl();
        SourceClass sourceClass = podamFactory.manufacturePojo(SourceClass.class);
        DestinationClass destinationClass = new DestinationClass();
        mapper.map(sourceClass, destinationClass);

        Assert.assertEquals(sourceClass.getParentString1(), destinationClass.getParentString1());
        Assert.assertEquals(sourceClass.getInnerClass().getParentString2(), destinationClass.getParentString2());
        Assert.assertEquals(sourceClass.getParentString2(), destinationClass.getInnerClass().getParentString2());
        Assert.assertEquals(sourceClass.getParentArrayList11().get(0), destinationClass.getString3());
        Assert.assertEquals(sourceClass.getClassesList().get(0).getParentString1(), destinationClass.getString4());
        Assert.assertEquals(sourceClass.getString3(), destinationClass.getStrings2().get(0));
        Assert.assertEquals(sourceClass.getString4(), destinationClass.getClassesList().get(0).getParentString1());
        Assert.assertEquals(sourceClass.getClassesList().get(1).getParentString1(), destinationClass.getClassesList().get(1).getParentString1());
    }
}