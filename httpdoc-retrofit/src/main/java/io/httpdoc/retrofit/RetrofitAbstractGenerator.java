package io.httpdoc.retrofit;

import io.httpdoc.core.*;
import io.httpdoc.core.annotation.HDAnnotation;
import io.httpdoc.core.annotation.HDAnnotationConstant;
import io.httpdoc.core.appender.FileAppender;
import io.httpdoc.core.fragment.*;
import io.httpdoc.core.kit.StringKit;
import io.httpdoc.core.provider.Provider;
import io.httpdoc.core.type.HDClass;
import io.httpdoc.core.type.HDType;
import okhttp3.HttpUrl;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.httpdoc.core.Parameter.*;

/**
 * Retrofit Client 生成器
 *
 * @author 杨昌沛 646742615@qq.com
 * @date 2018-04-27 15:59
 **/
public abstract class RetrofitAbstractGenerator implements Generator {
    protected final String prefix;
    protected final String suffix;
    protected final Set<Class<? extends Converter.Factory>> converterFactories = new LinkedHashSet<>();

    protected RetrofitAbstractGenerator() {
        this("", "");
    }

    protected RetrofitAbstractGenerator(String prefix, String suffix) {
        this(prefix, suffix, Collections.<Class<? extends Converter.Factory>>emptyList());
    }

    protected RetrofitAbstractGenerator(Collection<Class<? extends Converter.Factory>> converterFactories) {
        this("", "", converterFactories);
    }

    protected RetrofitAbstractGenerator(String prefix, String suffix, Collection<Class<? extends Converter.Factory>> converterFactories) {
        if (prefix == null || suffix == null || converterFactories == null) throw new NullPointerException();
        this.prefix = prefix.trim();
        this.suffix = suffix.trim();
        this.converterFactories.addAll(converterFactories);
    }

    @Override
    public void generate(Generation generation) throws IOException {
        Document document = generation.getDocument();
        String directory = generation.getDirectory();
        String pkg = generation.getPkg();
        Provider provider = generation.getProvider();

        generate(document, directory, pkg);

        Map<String, Schema> schemas = document.getSchemas();
        if (schemas != null) generate(directory, pkg, provider, schemas);

        Set<Controller> controllers = document.getControllers();
        if (controllers != null) generate(document, directory, pkg, provider, controllers);
    }

    protected void generate(Document document, String directory, String pkg) throws IOException {
        String comment = "Retrofit API Constants\nGenerated By Httpdoc\nGenerated At " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String name = "RetrofitAPI";
        FileAppender appender = new FileAppender(directory + "/" + name + ".java");
        ClassFragment interfase = new ClassFragment();
        interfase.setPkg(pkg);
        interfase.setCommentFragment(new CommentFragment(comment));
        interfase.setClazz(new HDClass(HDClass.Category.INTERFACE, pkg + "." + name));
        interfase.getFieldFragments().add(new FieldFragment(0, HDType.valueOf(String.class), "PROTOCOL", "\"" + document.getProtocol() + "\""));
        interfase.getFieldFragments().add(new FieldFragment(0, HDType.valueOf(String.class), "HOSTNAME", "\"" + document.getHostname() + "\""));
        interfase.getFieldFragments().add(new FieldFragment(0, HDType.valueOf(Integer.class), "PORT", String.valueOf(document.getPort())));
        interfase.joinTo(appender, Preference.DEFAULT);
        appender.close();
    }

    protected void generate(Document document, String directory, String pkg, Provider provider, Set<Controller> controllers) throws IOException {
        String comment = "Generated By Httpdoc\nGenerated At " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (Controller controller : controllers) {
            String name = controller.getName();
            FileAppender appender = new FileAppender(directory + "/" + name + ".java");
            ClassFragment interfase = new ClassFragment();
            interfase.setPkg(pkg);
            interfase.setCommentFragment(new CommentFragment(controller.getDescription() != null ? controller.getDescription() + "\n" + comment : comment));
            interfase.setClazz(new HDClass(HDClass.Category.INTERFACE, pkg + "." + name));
            {
                FieldFragment url = new FieldFragment(0);
                url.setType(HDType.valueOf(HttpUrl.class));
                url.setName("URL");
                StringBuilder sentence = new StringBuilder();
                sentence.append("new HttpUrl.Builder()").append('\n');
                if (document.getProtocol() != null) sentence.append("        .scheme(PROTOCOL)").append('\n');
                if (document.getHostname() != null) sentence.append("        .host(HOSTNAME)").append('\n');
                if (document.getPort() != null) sentence.append("        .port(PORT)").append('\n');
                sentence.append("        .build()");
                url.setAssignmentFragment(new AssignmentFragment(sentence, new LinkedHashSet<>(Arrays.asList(HttpUrl.class.getName(), "static " + pkg + "." + "RetrofitAPI.*"))));
                interfase.getFieldFragments().add(url);
            }

            {
                FieldFragment instance = new FieldFragment(0);
                instance.setType(interfase.getClazz());
                instance.setName("INSTANCE");
                StringBuilder sentence = new StringBuilder();
                sentence.append("new Retrofit.Builder()").append('\n');
                sentence.append("        .baseUrl(URL)").append('\n');
                Set<String> imports = new LinkedHashSet<>();
                imports.add(Retrofit.class.getName());
                for (Class<? extends Converter.Factory> converterFactory : converterFactories) {
                    sentence.append("        .addConverterFactory(")
                            .append(converterFactory.getSimpleName())
                            .append(".create())")
                            .append('\n');
                    imports.add(converterFactory.getName());
                }
                for (Class<? extends CallAdapter.Factory> callAdapterFactory : getCallAdapterFactories()) {
                    sentence.append("        .addCallAdapterFactory(")
                            .append(callAdapterFactory.getSimpleName())
                            .append(".create())")
                            .append('\n');
                    imports.add(callAdapterFactory.getName());
                }
                sentence.append("        .build()").append('\n');
                sentence.append("        .create(").append(name).append(".class").append(")");
                instance.setAssignmentFragment(new AssignmentFragment(sentence, imports));
                interfase.getFieldFragments().add(instance);
            }

            List<Operation> operations = controller.getOperations();
            if (operations != null) generate(pkg, provider, interfase, document, controller, operations);

            interfase.joinTo(appender, Preference.DEFAULT);
            appender.close();
        }
    }

    protected void generate(String pkg, Provider provider, ClassFragment interfase, Document document, Controller controller, List<Operation> operations) {
        for (Operation operation : operations) generate(pkg, provider, interfase, document, controller, operation);
    }

    protected abstract void generate(String pkg, Provider provider, ClassFragment interfase, Document document, Controller controller, Operation operation);

    protected abstract Set<Class<? extends CallAdapter.Factory>> getCallAdapterFactories();

    protected String name(String name) {
        if (prefix.isEmpty()) return name + suffix;
        else return prefix + name.substring(0, 1).toUpperCase() + name.substring(1) + suffix;
    }

    protected void describe(Operation operation, MethodFragment method, List<Parameter> parameters) {
        StringBuilder description = new StringBuilder(operation.getDescription() != null ? operation.getDescription() : "");
        for (int i = 0; parameters != null && i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (parameter.getDescription() == null) continue;
            description.append('\n').append("@param ").append(parameter.getName()).append(" ").append(parameter.getDescription());
        }
        method.setCommentFragment(new CommentFragment(description.toString()));
    }

    protected void generate(String pkg, Provider provider, MethodFragment method, List<Parameter> parameters) {
        boolean multipart = false;
        int bodies = 0;
        for (int i = 0; parameters != null && i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            multipart = param.getType().isFile();
            bodies += param.getScope().equals(HTTP_PARAM_SCOPE_BODY) ? 1 : 0;
        }
        if (multipart || bodies > 1) {
            HDAnnotation header = new HDAnnotation(Multipart.class);
            method.getAnnotations().add(header);
        }
        for (int i = 0; parameters != null && i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            ParameterFragment parameter = new ParameterFragment();
            parameter.setName(StringKit.isBlank(param.getName()) ? "arg" + i : param.getName());
            annotate(param, parameter, multipart || bodies > 1);
            HDType type = param.getType().toType(pkg, provider);
            parameter.setType(type);
            method.getParameterFragments().add(parameter);
        }
    }

    protected void annotate(Parameter parameter, ParameterFragment fragment, boolean multipart) {
        switch (parameter.getScope()) {
            case HTTP_PARAM_SCOPE_HEADER: {
                HDAnnotation header = new HDAnnotation(Header.class);
                if (parameter.getName() != null) header.getProperties().put("value", HDAnnotationConstant.valuesOf(parameter.getName()));
                fragment.getAnnotations().add(header);
                break;
            }
            case HTTP_PARAM_SCOPE_PATH: {
                HDAnnotation path = new HDAnnotation(Path.class);
                if (parameter.getName() != null) path.getProperties().put("value", HDAnnotationConstant.valuesOf(parameter.getName()));
                fragment.getAnnotations().add(path);
                break;
            }
            case HTTP_PARAM_SCOPE_QUERY: {
                HDAnnotation query = new HDAnnotation(Query.class);
                if (parameter.getName() != null) query.getProperties().put("value", HDAnnotationConstant.valuesOf(parameter.getName()));
                fragment.getAnnotations().add(query);
                break;
            }
            case HTTP_PARAM_SCOPE_BODY: {
                if (parameter.getType().isFile()) {
                    if (parameter.getType().getCategory() == Category.DICTIONARY) {
                        HDAnnotation map = new HDAnnotation(PartMap.class);
                        fragment.getAnnotations().add(map);
                    } else {
                        HDAnnotation part = new HDAnnotation(Part.class);
                        if (parameter.getName() != null) part.getProperties().put("value", HDAnnotationConstant.valuesOf(parameter.getName()));
                        fragment.getAnnotations().add(part);
                    }
                } else {
                    if (multipart) {
                        HDAnnotation part = new HDAnnotation(Part.class);
                        if (parameter.getName() != null) part.getProperties().put("value", HDAnnotationConstant.valuesOf(parameter.getName()));
                        fragment.getAnnotations().add(part);
                    } else {
                        HDAnnotation body = new HDAnnotation(Body.class);
                        fragment.getAnnotations().add(body);
                    }
                }
                break;
            }
        }
    }

    protected String path(String... segments) {
        StringBuilder path = new StringBuilder();
        for (String segment : segments) {
            if (segment == null) continue;
            path.append("/").append(segment);
        }
        return path.toString().replaceAll("/+", "/");
    }

    protected void annotate(Document document, Controller controller, Operation operation, MethodFragment fragment) {
        String path = path(document.getContext(), controller.getPath(), operation.getPath());
        switch (operation.getMethod()) {
            case "HEAD": {
                HDAnnotation get = new HDAnnotation(HEAD.class);
                get.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(get);
                break;
            }
            case "OPTIONS": {
                HDAnnotation get = new HDAnnotation(OPTIONS.class);
                get.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(get);
                break;
            }
            case "GET": {
                HDAnnotation get = new HDAnnotation(GET.class);
                get.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(get);
                break;
            }
            case "POST": {
                HDAnnotation post = new HDAnnotation(POST.class);
                post.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(post);
                break;
            }
            case "PUT": {
                HDAnnotation put = new HDAnnotation(PUT.class);
                put.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(put);
                break;
            }
            case "DELETE": {
                HDAnnotation get = new HDAnnotation(DELETE.class);
                get.getProperties().put("value", HDAnnotationConstant.valuesOf(path));
                fragment.getAnnotations().add(get);
                break;
            }
        }
    }

    protected void generate(String directory, String pkg, Provider provider, Map<String, Schema> schemas) throws IOException {
        String comment = "Generated By Httpdoc\nGenerated At " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (Schema schema : schemas.values()) {
            String name = schema.getName();
            FileAppender appender = new FileAppender(directory + "/" + name + ".java");
            ClassFragment clazz = new ClassFragment();
            clazz.setPkg(pkg);
            clazz.setCommentFragment(new CommentFragment(schema.getDescription() != null ? schema.getDescription() + "\n" + comment : comment));
            switch (schema.getCategory()) {
                case ENUM:
                    clazz.setClazz(new HDClass(HDClass.Category.ENUM, pkg + "." + name));
                    Set<Constant> constants = schema.getConstants();
                    for (Constant constant : (constants != null ? constants : Collections.<Constant>emptySet())) {
                        ConstantFragment con = new ConstantFragment(new CommentFragment(constant.getDescription()), constant.getName());
                        clazz.getConstantFragments().add(con);
                    }
                    break;
                case OBJECT:
                    clazz.setClazz(new HDClass(HDClass.Category.CLASS, pkg + "." + name));
                    clazz.setSuperclass(schema.getSuperclass() != null && schema.getSuperclass().getCategory() == Category.OBJECT ? new HDClass(pkg + "." + schema.getSuperclass().getName()) : null);
                    Map<String, Property> properties = schema.getProperties();
                    for (Map.Entry<String, Property> entry : (properties != null ? properties.entrySet() : Collections.<Map.Entry<String, Property>>emptySet())) {
                        Property property = entry.getValue();
                        HDType type = property.getType().toType(pkg, provider);
                        FieldFragment field = new FieldFragment();
                        field.setName(entry.getKey());
                        field.setType(type);
                        field.setCommentFragment(new CommentFragment(property.getDescription()));
                        clazz.getFieldFragments().add(field);

                        GetterMethodFragment getter = new GetterMethodFragment(type, entry.getKey());
                        clazz.getMethodFragments().add(getter);

                        SetterMethodFragment setter = new SetterMethodFragment(type, entry.getKey());
                        clazz.getMethodFragments().add(setter);
                    }
                    break;
            }
            clazz.joinTo(appender, Preference.DEFAULT);
            appender.close();
        }
    }

}
