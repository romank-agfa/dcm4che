package org.dcm4che.conf.core;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigurableProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Roman K
 */
public class AnnotatedConfigurableProperty {
    private Map<Type, Annotation> annotations;
    private Type type;
    private String name;

    public AnnotatedConfigurableProperty() {
    }

    public AnnotatedConfigurableProperty(Type componentType) {
        setType(componentType);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAnnotation(Class<T> annotationType) {
        return (T) annotations.get(annotationType);
    }

    public void setAnnotations(Map<Type, Annotation> annotations) {
        this.annotations = annotations;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getAnnotatedName() throws ConfigurationException {

        String name = getAnnotation(ConfigurableProperty.class).name();
        if (!name.equals("")) return name;
        name = this.name;
        if (name != null) return name;
        throw new ConfigurationException("Property name not specified");

    }

    public void setName(String name) {
        this.name = name;
    }
}
