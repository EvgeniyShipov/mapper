package ru.sbt.integration.orchestration.mapper.generator;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.mapper.mapping.MappingObject;
import ru.sbt.integration.orchestration.mapper.mapping.MappingPair;
import ru.sbt.integration.orchestration.mapper.mapping.MultipleMappingModel;
import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;

import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.LIST;

/**
 * Class for generating java code from Tree class presentation
 */
public class JavaFileMappingGenerator extends AbstractMappingGenerator {

    private final static String MAPPING_METHOD_NAME_PREFIX = "map";
    private final static String FROM = "from";
    private final static String TO = "to";

    private final StringBuilder generatedCode = new StringBuilder();
    private MappingStyle mappingStyle;

    public JavaFileMappingGenerator(MultipleMappingModel mappingModel, MappingStyle mappingStyle) {
        super(mappingModel);
        this.mappingStyle = mappingStyle;
    }

    @Override
    public void generate() {
        generateJavaFile();
    }

    @Override
    public String getGeneratedCode() {
        return generatedCode.toString();
    }

    /**
     * Method for generating a JavaFile containing a single class with single map method
     */
    private void generateJavaFile() {
        Set<MethodSpec> methodsSet = new HashSet<>();

        getModel().getMappingPairs().forEach(pair -> {
            Class<?> sourceClass = getRoot(pair.getSource()).getField().getDeclaringClass();
            methodsSet.add(generateMethodBody(sourceClass));
        });

        TypeSpec mappingClass = TypeSpec.classBuilder("Mapping")
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodsSet)
                .build();

        JavaFile javaFile = JavaFile
                .builder("ru.sbt.integration.orchestration.mapping", mappingClass)
                .build();

        generatedCode.setLength(0);
        generatedCode.append(javaFile.toString());
    }

    /**
     * Method for generating java code for every mapping goal
     */
    private MethodSpec generateMethodBody(Class<?> sourceClass) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(MAPPING_METHOD_NAME_PREFIX + sourceClass.getSimpleName() +
                TO + getDestination().getObject().getSimpleName())
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(sourceClass, FROM)
                .addParameter(getDestination().getObject(), TO);

        getModel().getMappingPairs().stream()
                .filter(pair -> pair.getSource().isChildOf(sourceClass))
                .forEach(pair -> methodBuilder.addCode(generateMappingBlock(pair)));

        return methodBuilder.build();
    }

    /**
     * generates mapping code block for given nodes
     * you need to firstly call generateDeclarations(sourceNode, destinationNode) then
     * generateSetterCallsBranch(destinationNode) and only then generateGetterCallsBranch(sourceNode)
     */
    @NotNull
    private CodeBlock generateMappingBlock(MappingPair pair) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.add(generateSetterCallsBranch(getBranch(pair.getDestination())));
        codeBuilder.add(generateGetterCallsBranch(getBranch(pair.getSource())));
        return codeBuilder.build();
    }

    /**
     * returns branch of get method calls for specific object presented in java code.
     * It looks like:
     * Client client = new Client();
     * client.getInfo().getName().getPassword()
     */
    @NotNull
    private CodeBlock generateGetterCallsBranch(List<FieldNode> sourceBranch) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        for (FieldNode node : sourceBranch) {
            MappingObject instance = new MappingObject(node);
            if (node.getParent() == null)
                codeBuilder.add(mappingStyle.getSourceStartString(), FROM, instance.getter());
            else if (node instanceof CollectionNode)
                codeBuilder.add(mappingStyle.getSourceString(), instance.getter(), node.getName());
            else
                codeBuilder.add(mappingStyle.getSourceString(), instance.getter(), "");
        }
        return codeBuilder
                .add(mappingStyle.getSourceEndString())
                .addStatement("")
                .build();
    }

    /**
     * returns branch of get and set method calls for specific object presented in java code.
     * For example, if you need to call getSetter on some specific field of class,
     * firstly you should call all getters of all its root fields and then call getSetter.
     * It looks like:
     * Organization org = new Organization();
     * org.getInfo().getName().setPassword(client.getInfo().getName().getPassword())
     * where argument in getSetter - is getField you get from calls of multiple getters on source object
     */
    @NotNull
    private CodeBlock generateSetterCallsBranch(List<FieldNode> destinationBranch) {
        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        for (FieldNode node : destinationBranch) {
            MappingObject instance = new MappingObject(node);
            if (node.getParent() == null) {
                codeBuilder.add(mappingStyle.getDestinationStartString(), TO);
            }
            if (destinationBranch.indexOf(node) == destinationBranch.size() - 1) {
                if (node instanceof CollectionChildNode) {
                    if (((CollectionChildNode) node).getCollectionType() == LIST) {
                        codeBuilder.add(mappingStyle.getDestinationString(), instance.setter(), "");
                    } else {
                        codeBuilder.add(mappingStyle.getDestinationString(), instance.setter(), node.getName() + ", ");
                    }
                } else {
                    codeBuilder.add(mappingStyle.getDestinationString(), instance.setter(), "");
                }
            } else if (node instanceof CollectionNode) {
                codeBuilder.add(mappingStyle.getDestinationEndString(), instance.getter(), node.getName());
            } else {
                codeBuilder.add(mappingStyle.getDestinationEndString(), instance.getter(), "");
            }
        }

        return codeBuilder.build();
    }

    public enum MappingStyle {

        PLAIN_JAVA {
            @Override
            String getDestinationStartString() {
                return "$L";
            }

            @Override
            String getDestinationString() {
                return ".$L($L";
            }

            @Override
            String getDestinationEndString() {
                return ".$L($L)";
            }

            @Override
            String getSourceStartString() {
                return "$L.$L()";
            }

            @Override
            String getSourceString() {
                return ".$L($L)";
            }

            @Override
            String getSourceEndString() {
                return ")";
            }
        },

        OPTIONAL {
            @Override
            String getDestinationStartString() {
                return "Optional.ofNullable($L)";
            }

            @Override
            String getDestinationString() {
                return "\n\t.ifPresent(field -> field.$L($L\n\t\t";
            }

            @Override
            String getDestinationEndString() {
                return "\n\t.map(val -> val.$L($L))";
            }

            @Override
            String getSourceStartString() {
                return "Optional.ofNullable($L)\n\t\t\t.map(val -> val.$L())";
            }

            @Override
            String getSourceString() {
                return "\n\t\t\t.map(val -> val.$L($L))";
            }

            @Override
            String getSourceEndString() {
                return "\n\t\t\t.orElse(null)))";
            }
        };

        abstract String getDestinationStartString();

        abstract String getDestinationString();

        abstract String getDestinationEndString();

        abstract String getSourceStartString();

        abstract String getSourceString();

        abstract String getSourceEndString();
    }
}
