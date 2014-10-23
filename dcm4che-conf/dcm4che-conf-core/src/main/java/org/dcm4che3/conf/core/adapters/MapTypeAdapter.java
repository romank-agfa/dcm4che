package org.dcm4che3.conf.core.adapters;

import org.dcm4che3.conf.core.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.BeanVitalizer;
import org.dcm4che3.conf.api.ConfigurationException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map<br>
 * 
 * Key type must have String as serialized representation and must not use field when serializing/deserializing!
 * 
 */

public class MapTypeAdapter<K, V> implements ConfigTypeAdapter<Map<K, V>, Map<String,Object>> {


    @Override
    public Map<K, V> fromConfigNode(Map<String, Object> configNode, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        if (configNode == null) return null;

        Type valueType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 1);
        Type keyType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 0);
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valueType);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyType);

        Map<K, V> map = new HashMap<K, V>();

        for (Entry<String, Object> e : configNode.entrySet()) {
            map.put(keyAdapter.fromConfigNode(e.getKey(), new AnnotatedConfigurableProperty(keyType), vitalizer),
                    valueAdapter.fromConfigNode(e.getValue(), new AnnotatedConfigurableProperty(valueType), vitalizer));

        }

        return map;
    }

    @Override
    public Map<String, Object> toConfigNode(Map<K, V> object, AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        if (object == null) return null;

        Type valueType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 1);
        Type keyType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 0);
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valueType);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyType);

        Map<String,Object> configNode = new HashMap<String,Object>();

        for (Entry<K, V> e : object.entrySet()) {
            configNode.put(keyAdapter.toConfigNode(e.getKey(), new AnnotatedConfigurableProperty(keyType), vitalizer),
            valueAdapter.toConfigNode(e.getValue(), new AnnotatedConfigurableProperty(valueType), vitalizer));
        }

        return configNode;
    }

    @Override
    public Map<String, Object> getSchema(AnnotatedConfigurableProperty property, BeanVitalizer vitalizer) throws ConfigurationException {

        Map<String, Object> metadata =  new HashMap<String, Object>();
        Map<String, Object> keyMetadata =  new HashMap<String, Object>();
        Map<String, Object> valueMetadata =  new HashMap<String, Object>();
        Map<String, Object> valueMetadataWrapper =  new HashMap<String, Object>();

        metadata.put("type", "object");
        metadata.put("class", "Map");

        // get adapters
        Type valueType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 1);
        Type keyType = DefaultConfigTypeAdapters.getTypeForGenericsParameter(property, 0);
        ConfigTypeAdapter<V, Object> valueAdapter = (ConfigTypeAdapter<V, Object>) vitalizer.lookupTypeAdapter(valueType);
        ConfigTypeAdapter<K, String> keyAdapter = (ConfigTypeAdapter<K, String>) vitalizer.lookupTypeAdapter(keyType);

        // fill in key and value metadata
        keyMetadata.putAll(keyAdapter.getSchema(new AnnotatedConfigurableProperty(keyType), vitalizer));
        metadata.put("mapkey", keyMetadata);
        
        valueMetadata.putAll(valueAdapter.getSchema(new AnnotatedConfigurableProperty(valueType), vitalizer));
        valueMetadataWrapper.put("*", valueMetadata);
        metadata.put("properties", valueMetadataWrapper);

        return metadata;
    }

    @Override
    public Map<String, Object> normalize(Object configNode) throws ConfigurationException {
        return (Map<String, Object>) configNode;
    }
}