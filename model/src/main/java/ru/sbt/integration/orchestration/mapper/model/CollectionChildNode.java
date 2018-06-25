package ru.sbt.integration.orchestration.mapper.model;

import java.util.Objects;

import static ru.sbt.integration.orchestration.mapper.utils.ReflectionUtils.*;

public class CollectionChildNode extends FieldNode {
    private String signature;

    CollectionChildNode(CollectionNode parent) {
        super(parent.getField(), parent.getRootClass());
        this.setParent(parent);

        String typeSignature = getFullChildGenericSignature(parent.getField().getGenericType().getTypeName(),
                parent.getCollectionType());
        this.signature = cutPackages(typeSignature);

        this.setName(parent.getCollectionType().getFirstStringName());
        this.setRequired(Required.ONE);
        this.setType(loadChildClass(typeSignature, this.getRootClass().getClassLoader()));
    }

    CollectionChildNode(CollectionNode parent, String name) {
       this(parent);
       this.setName(name);
    }

    public void setSignature(String signature) throws ClassNotFoundException{
        if (signature == null || Objects.equals(this.signature, signature)) {
            return;
        }
        this.signature = cutPackages(signature);
        this.setType(this.getRootClass().getClassLoader().loadClass(signature));
    }

    public CollectionNode.CollectionType getCollectionType() {
        return ((CollectionNode)this.getParent()).getCollectionType();
    }

    @Override
    public String getSignature() {
        return signature;
    }
}