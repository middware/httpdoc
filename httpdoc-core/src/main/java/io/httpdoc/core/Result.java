package io.httpdoc.core;

/**
 * 资源操作符结果类型
 *
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-04-12 13:42
 **/
public class Result extends Definition {
    private static final long serialVersionUID = -228264825667769977L;

    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
