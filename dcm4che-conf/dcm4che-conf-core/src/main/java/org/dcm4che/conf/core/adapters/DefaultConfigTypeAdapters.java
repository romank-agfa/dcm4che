/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che.conf.core.adapters;

import org.dcm4che.conf.core.AnnotatedConfigurableProperty;
import org.dcm4che.conf.core.BeanVitalizer;
import org.dcm4che.conf.core.validation.ValidationException;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationUnserializableException;
import org.dcm4che3.conf.api.generic.ConfigurableProperty;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman K
 */
public class DefaultConfigTypeAdapters {

    /**
     * Gets a child node using the name of the provided property, and then looks up the proper adapter and runs it against this child node
     *
     * @param configNode
     * @param property
     * @param vitalizer
     * @return
     * @throws org.dcm4che3.conf.api.ConfigurationException
     */
    static Object delegateGetChildFromConfigNode(Map<String, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        // determine node name and get the property
        String nodeName = property.getAnnotatedName();
        Object node = configNode.get(nodeName);

        // lookup adapter and run it on the property
        ConfigTypeAdapter adapter = vitalizer.lookupTypeAdapter(property.getType());
        return adapter.fromConfigNode(node, property, vitalizer);
    }

    static void delegateChildToConfigNode(Object object, Map<String, Object> parentNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
        String nodeName = property.getAnnotatedName();
        ConfigTypeAdapter adapter = vitalizer.lookupTypeAdapter(property.getType());
        parentNode.put(nodeName, adapter.toConfigNode(object, property, vitalizer));
    }

    static Type getTypeForGenericsParameter(AnnotatedConfigurableProperty property, int genericParameterIndex) throws ConfigurationException {
        Type[] actualTypeArguments = ((ParameterizedType) property.getType()).getActualTypeArguments();
        return actualTypeArguments[genericParameterIndex];
    }

    /**
     * Common Read/Write methods for primitives that have same serialized and deserialized representation, and same
     * write method
     */
    public static class PrimitiveTypeAdapter<T> implements ConfigTypeAdapter<T, T> {

        Map<String, Object> metadata = new HashMap<String, Object>();

        /**
         * Assign the type for metadata
         *
         * @param type
         */
        public PrimitiveTypeAdapter(String type) {
            metadata.put("type", type);
        }

        public PrimitiveTypeAdapter() {
        }

        @Override
        public T fromConfigNode(T configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return configNode;
        }

        @Override
        public T toConfigNode(T object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationUnserializableException {
            return object;
        }

        /**
         * Constant metadata
         */
        @Override
        public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return metadata;
        }


        @SuppressWarnings("unchecked")
        @Override
        public T normalize(Object configNode) throws ConfigurationException {
            try {
                if (metadata.get("type").equals("integer")) {

                    if (configNode.getClass().equals(String.class))
                        return (T) Integer.valueOf((String) configNode);
                    else if (configNode.getClass().equals(Integer.class))
                        return (T) configNode;
                    else
                        throw new ClassCastException();
                } else if (metadata.get("type").equals("boolean")) {
                    if (configNode.getClass().equals(String.class))
                        return (T) Boolean.valueOf((String) configNode);
                    else if (configNode.getClass().equals(Boolean.class))
                        return (T) configNode;
                    else
                        throw new ClassCastException();

                } else if (metadata.get("type").equals("number")) {
                    if (configNode.getClass().equals(String.class))
                        return (T) Double.valueOf((String) configNode);
                    else if (configNode.getClass().equals(Double.class) ||
                            configNode.getClass().equals(Float.class))
                        return (T) configNode;
                    else
                        throw new ClassCastException();
                } else return (T) configNode;
            } catch (Exception e) {
                throw new ConfigurationException("Cannot parse node " + configNode, e);
            }
        }
    }

    /**
     * Common Read/Write methods for String representation
     */
    public abstract static class CommonAbstractTypeAdapter<T> implements ConfigTypeAdapter<T, String> {
        protected Map<String, Object> metadata = new HashMap<String, Object>();

        public CommonAbstractTypeAdapter(String type) {
            metadata.put("type", type);
        }

        @Override
        public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            return metadata;
        }

        @Override
        public String normalize(Object configNode) throws ConfigurationException {
            return (String) configNode;
        }
    }

    private static Object getDefaultIfNull(Object configNode, AnnotatedConfigurableProperty property) throws ConfigurationException {
        if (configNode == null) {
            configNode = property.getAnnotation(ConfigurableProperty.class).defaultValue();
            if (configNode.equals(""))
                throw new ValidationException("Property " + property.getAnnotatedName() + " must have a value");
        }
        return configNode;
    }


    /**
     * Enum - string
     */
    public static class EnumTypeAdapter extends CommonAbstractTypeAdapter<Enum<?>> {


        //TODO: implement getSchema for Enums!
        public EnumTypeAdapter() {
            super("enum");
        }


        @Override
        public Enum<?> fromConfigNode(String configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {
            if (configNode == null)
                return null;
            try {
                Method method = ((Class) property.getType()).getMethod("valueOf", String.class);
                return (Enum<?>) method.invoke(null, configNode);
            } catch (Exception x) {
                throw new ConfigurationException("Deserialization of Enum failed! field:" + property, x);
            }
        }

        @Override
        public String toConfigNode(Enum<?> object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationUnserializableException {
            return (object == null ? null : object.name());
        }
    }

    public static Map<Class, ConfigTypeAdapter> defaultTypeAdapters;

    static {
        defaultTypeAdapters = new HashMap<Class, ConfigTypeAdapter>();

        defaultTypeAdapters.put(String.class, new PrimitiveTypeAdapter("string"));

        ConfigTypeAdapter integerAdapter = new PrimitiveTypeAdapter("integer");
        defaultTypeAdapters.put(int.class, integerAdapter);
        defaultTypeAdapters.put(Integer.class, integerAdapter);

        ConfigTypeAdapter booleanAdapter = new PrimitiveTypeAdapter("boolean");
        defaultTypeAdapters.put(Boolean.class, booleanAdapter);
        defaultTypeAdapters.put(boolean.class, booleanAdapter);

        ConfigTypeAdapter doubleAdapter =  new PrimitiveTypeAdapter("number");
        defaultTypeAdapters.put(double.class, doubleAdapter);
        defaultTypeAdapters.put(float.class, doubleAdapter);
        defaultTypeAdapters.put(Double.class, doubleAdapter);
        defaultTypeAdapters.put(Float.class, doubleAdapter);

        defaultTypeAdapters.put(Map.class, new MapTypeAdapter());
        defaultTypeAdapters.put(Set.class, new SetTypeAdapter());
        defaultTypeAdapters.put(Enum.class, new EnumTypeAdapter());

    }

    public static ConfigTypeAdapter get(Class clazz) {
        return defaultTypeAdapters.get(clazz);
    }

}
