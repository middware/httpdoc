package io.httpdoc.objc.core;

import io.httpdoc.core.Category;
import io.httpdoc.core.Constant;
import io.httpdoc.core.Property;
import io.httpdoc.core.Schema;
import io.httpdoc.core.supplier.Supplier;
import io.httpdoc.core.type.HDType;
import io.httpdoc.objc.ObjC;
import io.httpdoc.objc.foundation.NSArray;
import io.httpdoc.objc.foundation.NSDictionary;
import io.httpdoc.objc.foundation.NSString;
import io.httpdoc.objc.type.ObjCClass;
import io.httpdoc.objc.type.ObjCGenericType;
import io.httpdoc.objc.type.ObjCType;

import java.lang.reflect.Type;
import java.util.*;

public class ObjCSchema extends Schema {
    private static final long serialVersionUID = -9173526411507445705L;
    private final String prefix;
    private final Schema schema;

    public ObjCSchema(String prefix, Schema schema) {
        this.prefix = prefix;
        this.schema = schema;
    }

    public ObjCType toObjCType(Supplier supplier) {
        Category category = this.getCategory();
        switch (category) {
            case BASIC: {
                Type type = supplier.acquire(this);
                return ObjCType.valueOf(type);
            }
            case DICTIONARY: {
                ObjCClass rawType = new ObjCClass(NSDictionary.class);
                ObjCSchema component = (ObjCSchema) this.getComponent();
                ObjCType[] actualTypeArguments = new ObjCType[]{new ObjCClass(NSString.class), component.toObjCType(supplier)};
                return new ObjCGenericType(rawType, actualTypeArguments);
            }
            case ARRAY: {
                ObjCClass rawType = new ObjCClass(NSArray.class);
                ObjCSchema component = (ObjCSchema) this.getComponent();
                if (component.isPrimitive()) component = (ObjCSchema) component.toWrapper();
                ObjCType[] actualTypeArguments = new ObjCType[]{component.toObjCType(supplier)};
                return new ObjCGenericType(rawType, actualTypeArguments);
            }
            case ENUM: {
                String name = this.getName();
                return new ObjCClass(name, ObjC.Kind.TYPEDEF, ObjC.Reference.COPY, name + ".h");
            }
            case OBJECT: {
                String name = this.getName();
                return new ObjCClass(name, ObjC.Kind.CLASS, ObjC.Reference.STRONG, name + ".h");
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean isPart() {
        return schema.isPart();
    }

    @Override
    public boolean isVoid() {
        return schema.isVoid();
    }

    @Override
    public boolean isPrimitive() {
        return schema.isPrimitive();
    }

    @Override
    public Schema toWrapper() {
        if (!isPrimitive()) throw new IllegalStateException();
        else return new ObjCSchema(prefix, Schema.valueOf(Number.class));
    }

    @Override
    public String toName() {
        if (schema.getCategory() == Category.DICTIONARY) return "dictionary";
        else return schema.toName();
    }

    @Override
    public HDType toType(String pkg, boolean pkgForced, Supplier supplier) {
        return schema.toType(pkg, pkgForced, supplier);
    }

    @Override
    public Category getCategory() {
        return schema.getCategory();
    }

    @Override
    public void setCategory(Category category) {
        schema.setCategory(category);
    }

    @Override
    public String getPkg() {
        return schema.getPkg();
    }

    @Override
    public void setPkg(String pkg) {
        schema.setPkg(pkg);
    }

    @Override
    public String getName() {
        return this.getCategory() == Category.ENUM || this.getCategory() == Category.OBJECT ? prefix + schema.getName() : schema.getName();
    }

    @Override
    public void setName(String name) {
        schema.setName(name);
    }

    @Override
    public Schema getSuperclass() {
        Schema superclass = schema.getSuperclass();
        if (superclass == null) return null;
        return new ObjCSchema(prefix, superclass);
    }

    @Override
    public void setSuperclass(Schema superclass) {
        schema.setSuperclass(superclass);
    }

    @Override
    public Map<String, Property> getProperties() {
        Map<String, Property> properties = schema.getProperties();
        if (properties == null) return null;
        Map<String, Property> map = new LinkedHashMap<>();
        for (Map.Entry<String, Property> entry : properties.entrySet()) map.put(entry.getKey(), new ObjCProperty(prefix, entry.getValue()));
        return map;
    }

    @Override
    public void setProperties(Map<String, Property> properties) {
        schema.setProperties(properties);
    }

    @Override
    public Schema getComponent() {
        Schema component = schema.getComponent();
        if (component == null) return null;
        return new ObjCSchema(prefix, component);
    }

    @Override
    public void setComponent(Schema component) {
        schema.setComponent(component);
    }

    @Override
    public Schema getOwner() {
        Schema owner = schema.getOwner();
        if (owner == null) return null;
        return new ObjCSchema(prefix, owner);
    }

    @Override
    public void setOwner(Schema owner) {
        schema.setOwner(owner);
    }

    @Override
    public Set<Constant> getConstants() {
        return schema.getConstants();
    }

    @Override
    public void setConstants(Set<Constant> constants) {
        schema.setConstants(constants);
    }

    @Override
    public Collection<Schema> getDependencies() {
        Collection<Schema> dependencies = schema.getDependencies();
        if (dependencies == null) return null;
        Collection<Schema> collection = new LinkedHashSet<>();
        for (Schema dependency : dependencies) collection.add(new ObjCSchema(prefix, dependency));
        return collection;
    }

    @Override
    public void setDependencies(Collection<Schema> dependencies) {
        schema.setDependencies(dependencies);
    }

    @Override
    public String getSummary() {
        return schema.getSummary();
    }

    @Override
    public void setSummary(String summary) {
        schema.setSummary(summary);
    }

    @Override
    public String getDeprecated() {
        return schema.getDeprecated();
    }

    @Override
    public void setDeprecated(String deprecated) {
        schema.setDeprecated(deprecated);
    }

    @Override
    public int getOrder() {
        return schema.getOrder();
    }

    @Override
    public void setOrder(int order) {
        schema.setOrder(order);
    }

    @Override
    public String getStyle() {
        return super.getStyle();
    }

    @Override
    public void setStyle(String style) {
        super.setStyle(style);
    }

    @Override
    public String getDescription() {
        return schema.getDescription();
    }

    @Override
    public void setDescription(String description) {
        schema.setDescription(description);
    }

}
