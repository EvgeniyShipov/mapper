package ru.sbt.integration.orchestration.mapper.utils;

import ru.sbt.integration.orchestration.mapper.exception.EmptyMappingFieldsException;
import ru.sbt.integration.orchestration.mapper.exception.UnsupportedMappingFormatException;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils.loadChildClass;

public class MappingUtils {

    private static Set<Class<?>> trivialTypes = new HashSet<>();
    //из каждого класса данного сета можно осуществить маппинг в любой другой класс данного сета
    private static final Set<Class<?>> mappableTypes = new HashSet<>();
    private static final Set<Class<?>> collectionTypes = new HashSet<>();
    private static final Set<Class<?>> dateTypes = new HashSet<>();

    private MappingUtils() {
    }

    static {
        trivialTypes.add(int[].class);
        trivialTypes.add(Integer.class);
        trivialTypes.add(Integer[].class);
        trivialTypes.add(char[].class);
        trivialTypes.add(Character.class);
        trivialTypes.add(Character[].class);
        trivialTypes.add(byte[].class);
        trivialTypes.add(Byte.class);
        trivialTypes.add(Byte[].class);
        trivialTypes.add(long[].class);
        trivialTypes.add(Long.class);
        trivialTypes.add(Long[].class);
        trivialTypes.add(short[].class);
        trivialTypes.add(Short.class);
        trivialTypes.add(Short[].class);
        trivialTypes.add(double[].class);
        trivialTypes.add(Double.class);
        trivialTypes.add(Double[].class);
        trivialTypes.add(float[].class);
        trivialTypes.add(Float.class);
        trivialTypes.add(Float[].class);
        trivialTypes.add(boolean.class);
        trivialTypes.add(Boolean.class);
        trivialTypes.add(Boolean[].class);
        trivialTypes.add(String.class);
        trivialTypes.add(String[].class);

        trivialTypes.add(Calendar.class);
        trivialTypes.add(Calendar[].class);
        trivialTypes.add(Date.class);
        trivialTypes.add(Date[].class);

        trivialTypes.add(BigInteger.class);
        trivialTypes.add(BigInteger[].class);

        trivialTypes.add(BigDecimal.class);
        trivialTypes.add(BigDecimal[].class);

        trivialTypes.add(Class.class);
        trivialTypes.add(Method.class);
        trivialTypes.add(Field.class);

        mappableTypes.add(int.class);
        mappableTypes.add(Integer.class);
        mappableTypes.add(double.class);
        mappableTypes.add(Double.class);
        mappableTypes.add(float.class);
        mappableTypes.add(Float.class);
        mappableTypes.add(byte.class);
        mappableTypes.add(Byte.class);
        mappableTypes.add(long.class);
        mappableTypes.add(Long.class);
        mappableTypes.add(short.class);
        mappableTypes.add(Short.class);
        mappableTypes.add(char.class);
        mappableTypes.add(Character.class);
        mappableTypes.add(String.class);
        mappableTypes.add(Object.class);

        collectionTypes.add(List.class);
        collectionTypes.add(Map.class);
        collectionTypes.add(Set.class);

        dateTypes.add(Date.class);
        dateTypes.add(Calendar.class);
    }

    public static Set<Class<?>> getTrivialTypes() {
        return new HashSet<>(trivialTypes);
    }

    public static Set<Class<?>> getMappableTypes() {
        return new HashSet<>(mappableTypes);
    }

    public static Set<Class<?>> getCollectionTypes() {
        return new HashSet<>(collectionTypes);
    }

    public static boolean isTrivialType(FieldNode node) {
        return isTrivialType(node.getType());
    }

    public static boolean isCollectionType(FieldNode node) {
        return collectionTypes.contains(node.getType());
    }

    public static boolean isTrivialType(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || trivialTypes.contains(type);
    }

    private static boolean isDateType(Class<?> type) {
        return dateTypes.stream().filter(clazz -> clazz.isAssignableFrom(type)).findFirst().orElse(null) != null;
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private static boolean isGenericTypesEquals(FieldNode sourceNode, FieldNode destinationNode) {
        return Optional.of(sourceNode)
                .map(FieldNode::getField)
                .map(Field::getGenericType)
                .map(Object::toString)
                .orElse("")
                .equals(Optional.of(destinationNode)
                        .map(FieldNode::getField)
                        .map(Field::getGenericType)
                        .map(Object::toString).orElse(""));
    }

    public static void checkMappingErrors(FieldNode sourceNode, FieldNode destinationNode) {
        checkSourceAndDestFieldsSelected(sourceNode, destinationNode);
        checkAddingToCollection(sourceNode, destinationNode);

        if (Calendar.class.isAssignableFrom(sourceNode.getType())) {

            if (String.class.isAssignableFrom(destinationNode.getType()) ||
                    isDateType(destinationNode.getType())) {
                return;
            } else throw new UnsupportedMappingFormatException("Mapping formats cannot be assignable");
        }

        if (Date.class.isAssignableFrom(sourceNode.getType())) {
            if (String.class.isAssignableFrom(destinationNode.getType()) || isDateType(destinationNode.getType())) {
                return;
            } else throw new UnsupportedMappingFormatException("Mapping formats cannot be assignable");
        }

        if (sourceNode.getType() == String.class) {
            if (isDateType(destinationNode.getType()) || mappableTypes.contains(destinationNode.getType())) {
                return;
            } else throw new UnsupportedMappingFormatException("Mapping formats cannot be assignable");
        }
        checkMappingErrors(sourceNode.getType(),
                destinationNode.getClass() == CollectionNode.class && sourceNode.getClass() != CollectionNode.class ?
                        loadChildClass((CollectionNode) destinationNode) :
                        destinationNode.getType()
        );
    }

    private static void checkSourceAndDestFieldsSelected(FieldNode sourceNode, FieldNode destinationNode) {
        if (sourceNode == null) {
            throw new EmptyMappingFieldsException("Source field is null. Choose it");
        }
        if (destinationNode == null) {
            throw new EmptyMappingFieldsException("Destination field is null. Choose it");
        }
    }

    private static void checkAddingToCollection(FieldNode sourceNode, FieldNode destinationNode) {
        if ((sourceNode instanceof CollectionNode && destinationNode instanceof CollectionNode &&
                !isGenericTypesEquals(sourceNode, destinationNode))) {
            throw new UnsupportedMappingFormatException("Mapping formats cannot be assignable");
        }
    }

    //todo возможно есть исключения когда нельзя мапить тривиальный тип из одного в другой
    private static void checkMappingErrors(Class<?> sourceClass, Class<?> destinationClass) {
        if (!isTypeNamesEqual(sourceClass, destinationClass) &&
                !isMappableTypes(sourceClass, destinationClass) &&
                !isAssignable(destinationClass, sourceClass)) {
            throw new UnsupportedMappingFormatException("Mapping formats cannot be assignable");
        }
    }

    private static boolean isMappableTypes(Class<?> sourceClass, Class<?> destinationClass) {
        return (mappableTypes.contains(sourceClass)
                && mappableTypes.contains(destinationClass));
    }

    private static boolean isAssignable(Class<?> sourceClass, Class<?> destinationClass) {
        return sourceClass.isAssignableFrom(destinationClass);
    }

    private static boolean isTypeNamesEqual(Class<?> sourceClass, Class<?> destinationClass) {
        return sourceClass.getTypeName().equals(destinationClass.getTypeName());
    }
}