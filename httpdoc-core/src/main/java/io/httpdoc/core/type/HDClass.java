package io.httpdoc.core.type;

import io.httpdoc.core.Importable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.httpdoc.core.type.HDClass.Category.*;

/**
 * 类型
 *
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-05-02 12:18
 **/
public class HDClass extends HDType {
    private final Category category;
    private final String name;
    private final HDClass componentType;
    private HDTypeVariable[] typeParameters;

    public HDClass(String name) {
        if (name == null) throw new NullPointerException();
        this.name = name;
        this.componentType = name.endsWith("[]") ? new HDClass(name.substring(0, name.length() - 2)) : null;
        this.category = this.componentType != null ? ARRAY : Category.CLASS;
    }

    public HDClass(HDClass componentType) {
        if (componentType == null) throw new NullPointerException();
        this.category = ARRAY;
        this.name = componentType.getName() + "[]";
        this.componentType = componentType;
    }

    public HDClass(Category category, String name) {
        if (category == null || name == null) throw new NullPointerException();
        this.name = name;
        this.componentType = name.endsWith("[]") ? new HDClass(category, name.substring(0, name.length() - 2)) : null;
        this.category = this.componentType != null ? ARRAY : category;
    }

    public HDClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        } else if (clazz.isArray()) {
            category = ARRAY;
            name = (componentType = new HDClass(clazz.getComponentType())).getName() + "[]";
        } else {
            category = clazz.isInterface() ? INTERFACE : clazz.isAnnotation() ? ANNOTATION : clazz.isEnum() ? ENUM : CLASS;
            name = clazz.getName();
            componentType = null;
        }
    }

    @Override
    public void importTo(Map<Importable, List<String>> imports) {
        if (imports.containsKey(this)) return;
        else imports.put(this, Collections.<String>emptyList());
        if (componentType != null) componentType.importTo(imports);
        else imports.put(this, Collections.singletonList(name));
    }

    public String getSimpleName() {
        return name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
    }

    @Override
    public CharSequence getFormatName() {
        return componentType != null ? componentType.getFormatName() + "[]" : getSimpleName();
    }

    @Override
    public int length() {
        return getFormatName().length();
    }

    @Override
    public char charAt(int index) {
        return getFormatName().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return getFormatName().subSequence(start, end);
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public HDClass getComponentType() {
        return componentType;
    }

    public HDTypeVariable[] getTypeParameters() {
        return typeParameters;
    }

    void setTypeParameters(HDTypeVariable[] typeParameters) {
        this.typeParameters = typeParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HDClass hdClass = (HDClass) o;

        if (name != null ? !name.equals(hdClass.name) : hdClass.name != null) return false;
        return componentType != null ? componentType.equals(hdClass.componentType) : hdClass.componentType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (componentType != null ? componentType.hashCode() : 0);
        return result;
    }

    public enum Category {
        CLASS("class"), INTERFACE("interface"), ANNOTATION("@interface"), ENUM("enum"), ARRAY("array");

        public final String name;

        Category(String name) {
            this.name = name;
        }

    }

}