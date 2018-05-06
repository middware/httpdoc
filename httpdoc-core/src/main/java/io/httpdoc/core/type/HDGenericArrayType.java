package io.httpdoc.core.type;

import io.httpdoc.core.Importable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 泛型数组类型
 *
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-05-04 14:57
 **/
public class HDGenericArrayType extends HDType {
    private HDType genericComponentType;

    HDGenericArrayType() {
    }

    public HDGenericArrayType(HDType genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    @Override
    public CharSequence getFormatName() {
        return genericComponentType.getFormatName() + "[]";
    }

    @Override
    public void importTo(Map<Importable, List<String>> imports) {
        if (imports.containsKey(this)) return;
        else imports.put(this, Collections.<String>emptyList());
        genericComponentType.importTo(imports);
    }

    public HDType getGenericComponentType() {
        return genericComponentType;
    }

    void setGenericComponentType(HDType genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HDGenericArrayType that = (HDGenericArrayType) o;

        return genericComponentType != null ? genericComponentType.equals(that.genericComponentType) : that.genericComponentType == null;
    }

    @Override
    public int hashCode() {
        return genericComponentType != null ? genericComponentType.hashCode() : 0;
    }
}