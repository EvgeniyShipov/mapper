package ru.sbt.integration.orchestration.mapper.generator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.sbt.integration.orchestration.mapper.mapping.MappingPair;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;
import ru.sbt.integration.orchestration.mapper.model.TreeModel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.LIST;
import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.MAP;

public class XmlFileMappingGenerator extends AbstractMappingGenerator {
    private Document doc;
    private final StringWriter writer = new StringWriter();
    private final MappingGeneratorContext context = new MappingGeneratorContext();
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm s.SSSXXX";
    private static final String WILDCARD_SETTING = "false";

    private static final String SOURCE_HINT_FIELD = "a-hint";
    private static final String DESTINATION_HINT_FIELD = "b-hint";

    public XmlFileMappingGenerator(MultipleMappingModel model) {
        super(model);
    }

    @Override
    public void generate() throws Exception {
        writer.getBuffer().setLength(0);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();

        generateRootTag();
        for (TreeModel treeModel : getSourceList()) {
//            TODO: FIX
            if (isMappingContains(treeModel))
                generateMappingTags(treeModel);
        }
        configureXML();
    }

    @Override
    public String getGeneratedCode() {
        return writer.toString();
    }

    private void generateRootTag() {
        Element mappingsTextElement = doc.createElement("mappings");
        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://dozer.sourceforge.net");
        Attr xsi = doc.createAttribute("xmlns:xsi");
        xsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
        Attr schemaLocation = doc.createAttribute("xsi:schemaLocation");
        schemaLocation.setValue("http://dozer.sourceforge.net " +
                "http://dozer.sourceforge.net/schema/beanmapping.xsd");

        mappingsTextElement.setAttributeNode(xmlns);
        mappingsTextElement.setAttributeNode(xsi);
        mappingsTextElement.setAttributeNode(schemaLocation);
        doc.appendChild(mappingsTextElement);
    }

    private boolean isMappingContains(TreeModel sourceElement) {
        for (MappingPair pair : getModel().getMappingPairs()) {
            if (classesEqual(pair.getSource(), sourceElement))
                return true;
        }
        return false;
    }

    private void generateMappingTags(TreeModel sourceModel) {
        Element mappingElement = doc.createElement("mapping");
        //todo добавить изменение формата даты.
        mappingElement.setAttribute("date-format", DEFAULT_DATE_FORMAT);
        mappingElement.setAttribute("wildcard", WILDCARD_SETTING);
        doc.getDocumentElement().appendChild(mappingElement);

        Element sourceClassElement = doc.createElement("class-a");
        sourceClassElement.appendChild(doc.createTextNode(sourceModel.getCanonicalObjectName()));
        mappingElement.appendChild(sourceClassElement);

        Element destinationClassElement = doc.createElement("class-b");
        destinationClassElement.appendChild(doc.createTextNode(getDestination().getCanonicalObjectName()));
        mappingElement.appendChild(destinationClassElement);

        generateFieldsTag(mappingElement, sourceModel);
    }

    private void generateFieldsTag(Element root, TreeModel source) {
        getModel().getMappingPairs().forEach(pair -> {
            if (classesEqual(pair.getSource(), source)) {
                generateFieldsMappingTag(pair.getSource(), pair.getDestination(), root);
            }
        });
    }

    private void configureXML() throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
    }

    private void generateFieldsMappingTag(FieldNode sourceNode, FieldNode destinationNode, Element root) {
        Element fieldElement = doc.createElement("field");

        Element sourceElement = doc.createElement("a");
        Element destinationElement = doc.createElement("b");

        fieldElement.appendChild(sourceElement);
        fieldElement.appendChild(destinationElement);

        setSourceField(sourceElement, sourceNode, fieldElement);
        String destinationName = getDestinationFieldName(sourceNode, destinationNode);
        setDestinationField(destinationElement, destinationNode, fieldElement, destinationName);


        root.appendChild(fieldElement);
    }

    private String getDestinationFieldName(FieldNode sourceNode, FieldNode destinationNode) {
        String destinationName = createBranchName(destinationNode);
        if (destinationNode.getClass() == CollectionNode.class && sourceNode.getClass() != CollectionNode.class &&
                CollectionNode.CollectionType.LIST.equals(((CollectionNode) destinationNode).getCollectionType())) {
            destinationName = destinationName + String.format("[%s]",
                    context.getNextIndexForList(destinationNode.getName()));
        }
        return destinationName;
    }

    private void setDestinationField(Element destinationElement, FieldNode destinationNode, Element fieldElement, String destinationName) {
        if (destinationNode instanceof CollectionChildNode) {
            if (MAP.equals(((CollectionChildNode) destinationNode).getCollectionType())) {
                destinationElement.setAttribute("key", destinationNode.getName());
            }
            addHintField(destinationNode, fieldElement, false);
        }
        destinationElement.appendChild(doc.createTextNode(destinationName));
    }

    private void setSourceField(Element sourceElement, FieldNode sourceNode, Element fieldElement) {
        String sourceName = createBranchName(sourceNode);
        if (sourceNode instanceof CollectionChildNode) {
            if (MAP.equals(((CollectionChildNode) sourceNode).getCollectionType())) {
                sourceElement.setAttribute("key", sourceNode.getName());
            }
            addHintField(sourceNode, fieldElement, true);
        }
        sourceElement.appendChild(doc.createTextNode(sourceName));
    }

    private void addHintField(FieldNode fieldNode, Element fieldElement, boolean isSource) {
        Element sourceHint = doc.createElement(isSource ? SOURCE_HINT_FIELD : DESTINATION_HINT_FIELD);
        sourceHint.appendChild(doc.createTextNode(fieldNode.getType().getName()));
        fieldElement.appendChild(sourceHint);
    }

    private String createBranchName(FieldNode fieldNode) {
        StringBuilder branchName = new StringBuilder();
        List<FieldNode> branch = getBranch(fieldNode);
        for (FieldNode node : branch) {
            if (node instanceof CollectionChildNode) {
                if (CollectionNode.CollectionType.LIST.equals(((CollectionChildNode) node).getCollectionType())) {
                    branchName.append("[").append(node.getName()).append("]");
                }
            } else {
                if (branch.indexOf(node) != 0) branchName.append(".");
                branchName.append(node.getName());
            }
        }
        return branchName.toString();
    }

    private boolean classesEqual(FieldNode node, TreeModel sourceModel) {
        if (getRoot(node).getField().getDeclaringClass().equals(sourceModel.getObject())) {
            return true;
        }
        return getRoot(node).getField().getDeclaringClass().equals(sourceModel.getObject().getGenericSuperclass());
    }

    private class MappingGeneratorContext {
        private MappingGeneratorContext() {
        }

        private Map<String, Integer> listIndexCounters = new HashMap<>();
        private Map<String, Integer> mappingsToMaps = new HashMap<>();

        private Integer getNextIndexForList(String listFieldName) {
            Integer index = listIndexCounters.get(listFieldName) == null ? 0 : listIndexCounters.get(listFieldName);
            listIndexCounters.put(listFieldName, index + 1);
            return index;
        }
    }
}
