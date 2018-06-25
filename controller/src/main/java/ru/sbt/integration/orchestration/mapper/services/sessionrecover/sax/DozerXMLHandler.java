package ru.sbt.integration.orchestration.mapper.services.sessionrecover.sax;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.services.sessionrecover.exceptions.SessionRecoverException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnusedDeclaration")
public class DozerXMLHandler extends DefaultHandler {
    private final MultipleMappingModel model;
    private Class<?> sourceClass;
    private final Class<?> destinationClass;
    private FieldNode fieldA;
    private FieldNode fieldB;
    private String fieldAType;
    private String fieldBType;
    private String currentValue;
    private String keyOrIndexA;
    private String keyOrIndexB;

    public DozerXMLHandler(List<Class<?>> sourceClasses, Class<?> destinationClass, List<Class<?>> destinationArtifactListClasses) {
        this.model = new MultipleMappingModel(sourceClasses, destinationClass, destinationArtifactListClasses);
        this.sourceClass = sourceClasses.get(0);
        this.destinationClass = destinationClass;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Tag.getTag(qName).openTag(this, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        this.currentValue = (new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        Tag.getTag(qName).closeTag(this);
    }

    private void clearValue() {
        this.currentValue = null;
    }

    private void resetValues() {
        fieldAType = null;
        fieldBType = null;
        keyOrIndexA = null;
        keyOrIndexB = null;
        fieldA = null;
        fieldB = null;
    }

    //TODO мб рекурсией
    private Field getField(String fieldString, Class clazz) {
        Field field = null;
        for (String currentField : fieldString.split("\\.")) {
            Field[] fields = FieldUtils.getAllFields(clazz);
            for (Field iField : fields) {
                if (iField.getName().endsWith(currentField)) {
                    field = iField;
                    clazz = iField.getType();
                    break;
                }
            }
        }
        return field;
    }

    private enum Tag {
        MAPPING("mapping"),
        MAPPINGS("mappings"),
        FIELD("field") {
            @Override
            protected void closeTag(DozerXMLHandler handler) {
                handler.model.addMapping(handler.fieldA, handler.fieldB);
                handler.resetValues();
                super.closeTag(handler);
            }
        },
        CLASS_A("class-a") {
            @Override
            protected void closeTag(DozerXMLHandler handler) {

                String className = handler.currentValue;
                try {
                    handler.sourceClass = handler.sourceClass.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new SessionRecoverException("ClassNotFound " + className);
                }
                super.closeTag(handler);
            }
        },
        CLASS_B("class-b"),

        FIELD_A("a") {
            @Override
            protected void openTag(DozerXMLHandler handler, Attributes attributes) {
                super.openTag(handler, attributes);
                handler.keyOrIndexA = attributes.getValue("key");
            }

            @Override
            protected void closeTag(DozerXMLHandler handler) {
                String sourcePath = handler.currentValue;
                if (sourcePath.endsWith("]") && sourcePath.contains("[")) {
                    String[] split = sourcePath.split("[\\[\\]]");
                    handler.keyOrIndexA = split[1];
                    sourcePath = split[0];
                }
                Field sourceField = handler.getField(sourcePath, handler.sourceClass);
                handler.fieldA = handler.keyOrIndexA == null ?
                        handler.model.getSourceModel(handler.sourceClass).getNode(sourceField) :
                        handler.getModel().addChildField((CollectionNode) handler.model.getSourceModel(handler.sourceClass).getNode(sourceField), handler.keyOrIndexA);
                super.closeTag(handler);
            }
        },
        FIELD_B("b") {
            @Override
            protected void openTag(DozerXMLHandler handler, Attributes attributes) {
                super.openTag(handler, attributes);
                handler.keyOrIndexB = attributes.getValue("key");
            }

            @Override
            protected void closeTag(DozerXMLHandler handler) {
                String destinationPath = handler.currentValue;
                if (destinationPath.endsWith("]") && destinationPath.contains("[")) {
                    String[] split = destinationPath.split("[\\[\\]]");
                    handler.keyOrIndexB = split[1];
                    destinationPath = split[0];
                }
                Field destinationField = handler.getField(destinationPath, handler.destinationClass);
                handler.fieldB = handler.keyOrIndexB == null ?
                        handler.model.getDestination().getNode(destinationField) :
                        handler.getModel().addChildField((CollectionNode) handler.model.getDestination().getNode(destinationField), handler.keyOrIndexB);
                super.closeTag(handler);
            }
        },
        FIELD_A_HINT("a-hint") {
            @Override
            protected void closeTag(DozerXMLHandler handler) {
                handler.fieldAType = handler.currentValue;
                super.closeTag(handler);
            }
        },
        FIELD_B_HINT("b-hint") {
            @Override
            protected void closeTag(DozerXMLHandler handler) {
                handler.fieldBType = handler.currentValue;
                super.closeTag(handler);
            }
        },
        INVALID_TAG("invalidTag") {
            @Override
            protected void closeTag(DozerXMLHandler handler) {
                throw new SessionRecoverException("Invalid xml format");
            }
        };
        private final String tagName;

        Tag(String tagName) {
            this.tagName = tagName;
        }

        private static Tag getTag(String tag) {
            return Arrays.stream(Tag.values())
                    .filter(value -> Objects.equals(value.tagName, tag))
                    .findFirst()
                    .orElse(INVALID_TAG);
        }

        protected void closeTag(DozerXMLHandler handler) {
            handler.clearValue();
        }

        protected void openTag(DozerXMLHandler handler, Attributes attributes) {
            handler.clearValue();
        }
    }

    public MultipleMappingModel getModel() {
        return model;
    }
}
