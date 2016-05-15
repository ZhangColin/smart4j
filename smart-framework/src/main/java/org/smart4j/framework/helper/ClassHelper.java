package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Controller;
import org.smart4j.framework.annotation.Service;
import org.smart4j.framework.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassHelper {
    private static final Set<Class<?>> CLASS_SET;

    static {
        String basePackage = ConfigHelper.getAppBasePackage();
        CLASS_SET = ClassUtil.getClassSet(basePackage);
    }

    public static Set<Class<?>> getClassSet() {
        return CLASS_SET;
    }

    public static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
        return CLASS_SET.stream()
                .filter(cls -> cls.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }

    public static Set<Class<?>> getServiceClassSet() {
        return getClassSetByAnnotation(Service.class);
    }

    public static Set<Class<?>> getControllerClassSet() {
        return getClassSetByAnnotation(Controller.class);
    }

    public static Set<Class<?>> getBeanClassSet() {
        return CLASS_SET.stream()
                .filter(cls -> cls.isAnnotationPresent(Controller.class) ||
                        cls.isAnnotationPresent(Service.class))
                .collect(Collectors.toSet());
    }

    public static Set<Class<?>> getClassSetBySuper(Class<?> superClass){
        return CLASS_SET.stream()
                .filter(cls -> superClass.isAssignableFrom(cls) && !superClass.equals(cls))
                .collect(Collectors.toSet());
    }
}

