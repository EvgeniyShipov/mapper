package ru.sbt.integration.orchestration.mapper.mapping;

import ru.sbt.integration.orchestration.mapper.model.CollectionChildNode;
import ru.sbt.integration.orchestration.mapper.model.CollectionNode;
import ru.sbt.integration.orchestration.mapper.model.FieldNode;

import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.LIST;
import static ru.sbt.integration.orchestration.mapper.model.CollectionNode.CollectionType.MAP;

/**
 * Class for mapping purposes
 * It shouldn't be public, may be better make it with package visibility
 */
public class MappingObject {
    private final FieldNode instance;

    public MappingObject(FieldNode instance) {
        this.instance = instance;
    }

    public String name() {
        return type().getSimpleName().toLowerCase();
    }

    private Class<?> type() {
        return instance.getField().getDeclaringClass();
    }

    public String getter() {
        if (instance instanceof CollectionChildNode)
            return "get";
        if (instance instanceof CollectionNode && ((CollectionNode) instance).getCollectionType() == MAP) {
            return instance.getGetter() == null ? "get" : instance.getGetter().getName();
        }
        return instance.getGetter().getName();
    }

    public String setter() {
        if (instance instanceof CollectionNode && ((CollectionNode) instance).getCollectionType() == LIST)
            return instance.getSetter() == null ? "add" : instance.getGetter().getName();
        if (instance instanceof CollectionNode && ((CollectionNode) instance).getCollectionType() == MAP)
            return instance.getSetter() == null ? "put" : instance.getGetter().getName();
        return instance.getSetter().getName();
    }
}
