package ru.sbt.integration.orchestration.mapper.utils;

import com.google.common.base.Strings;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType;

import java.util.Arrays;

public class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Reflections getReflections(Class rootClass) {
        ClassLoader classLoader = rootClass.getClassLoader();

        return new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoader)).addClassLoader(classLoader));

    }

    /**
     * Обрезает пакеты у сигнатур если они есть    *
     * Пример: "java.utils.List<java.lang.Integer, java.lang.String>" -> "List<Integer, String>"
     * "List<Integer, String>" -> "List<Integer, String>"
     */
    public static String cutPackages(String signature) {
        return Strings.isNullOrEmpty(signature) ? "" : Arrays.stream(
                signature.split("[\\. <]"))
                .filter(s -> s.length() > 0 && Character.isUpperCase(s.charAt(0)))
                .reduce((s1, s2) -> s1.endsWith(",") ? s1 + s2 : s1 + "<" + s2)
                .map(s -> s.replace("$", "."))
                .orElse("");
    }

    /**
     * Получает сигнатуру и если в ней есть дженерики, то обрезает их
     * Пример: "java.utils.Map<java.lang.Integer, java.lang.String>" -> "ava.utils.Map";
     * "java.utils.List<java.lang.String>" -> "java.utils.List"
     * "java.utils.List" -> "java.utils.List"
     */
    public static String getClassSignatureWithoutGenerics(String fullGenericSignature) {
        if (fullGenericSignature != null && fullGenericSignature.contains("<") && fullGenericSignature.endsWith(">")) {
            return fullGenericSignature.substring(0, fullGenericSignature.indexOf("<"));
        }
        return fullGenericSignature;
    }

    public static String getFullChildGenericSignature(CollectionNode node) {
        return node == null ? null : getFullChildGenericSignature(node.getField().getGenericType().getTypeName(), node.getCollectionType());
    }

    /**
     * Получает сигнатуру child-а с пакетом
     * Пример: "java.utils.Map<java.lang.Integer, java.lang.String>" -> "java.lang.String";
     * "java.utils.List<java.lang.String>" -> "java.lang.String"
     * "java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.Object>>" -> "java.util.HashMap<java.lang.String, java.lang.Object>"
     */
    public static String getFullChildGenericSignature(String genericTypeSignature, CollectionType collectionType) {
        if (genericTypeSignature != null) {
            String genericTypeSignatureWithoutWhitespaces = genericTypeSignature.replace(" ", "");
            if (genericTypeSignatureWithoutWhitespaces.contains(collectionType.getSplitFilter()) && genericTypeSignatureWithoutWhitespaces.endsWith(">")) {

                int startIndex = genericTypeSignatureWithoutWhitespaces.indexOf(collectionType.getSplitFilter());
                String signature = genericTypeSignatureWithoutWhitespaces.substring(startIndex + 1, genericTypeSignatureWithoutWhitespaces.length());
                return signature.length() > 2 ? signature.substring(0, signature.length() - 1) : signature;
            }
        }
        return "java.lang.Object";
    }

    public static Class<?> loadChildClass(CollectionNode node) {
        return (loadChildClass(getFullChildGenericSignature(node), node.getRootClass().getClassLoader()));
    }

    public static Class<?> loadChildClass(String typeSignature, ClassLoader classLoader) {
        try {
            return classLoader == null ? null : classLoader.loadClass(getClassSignatureWithoutGenerics(typeSignature));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Object.class;
        }
    }
}
