package io.httpdoc.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源操作符
 *
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-04-12 13:35
 **/
public class Operation extends Definition {
    private static final long serialVersionUID = 5078545277567296662L;

    private String name;
    private String method;
    private String path;
    private List<String> produces = new ArrayList<>();
    private List<String> consumes = new ArrayList<>();
    private List<Parameter> parameters = new ArrayList<>();
    private Result result;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public enum HttpMethod {

        GET(false, false),
        HEAD(false, true),
        POST(true, true),
        PUT(true, true),
        PATCH(true, true),
        DELETE(true, false),
        OPTIONS(true, false),
        TRACE(false, false);

        HttpMethod(boolean permitsRequestBody, boolean requiresRequestBody) {
            this.permitsRequestBody = permitsRequestBody;
            this.requiresRequestBody = requiresRequestBody;
        }

        private boolean permitsRequestBody;
        private boolean requiresRequestBody;
        private static final Map<String, HttpMethod> mappings = new HashMap<String, HttpMethod>(8);

        static {
            for (HttpMethod httpMethod : values()) {
                mappings.put(httpMethod.name(), httpMethod);
            }
        }


        public static HttpMethod resolve(String method) {
            return (method != null ? mappings.get(method) : null);
        }

        public boolean matches(String method) {
            return (this == resolve(method));
        }

        /**
         * 是否允许有请求体
         */
        public boolean isPermitsRequestBody() {
            return requiresRequestBody;
        }

        /**
         * 是否要求有请求体
         */
        public boolean isRequiresRequestBody() {
            return requiresRequestBody;
        }


    }

}
