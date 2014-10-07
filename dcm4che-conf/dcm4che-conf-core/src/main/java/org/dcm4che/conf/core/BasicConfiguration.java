package org.dcm4che.conf.core;

import org.dcm4che.conf.core.util.ConfigPathUtil;
import org.dcm4che3.conf.api.ConfigurationException;

import java.util.Map;

/**
 * Created by aprvf on 29/09/2014.
 */

public class BasicConfiguration implements Configuration {


    private Configuration storageBackend;

    private Map<String, Object> configurationRoot = null;


    @Override
    public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
        if (configurationRoot==null)
            configurationRoot = storageBackend.getConfigurationRoot();
        return configurationRoot;
    }

    @Override
    public Object getConfigurationNode(String path) throws ConfigurationException {
        return ConfigPathUtil.getNode(getConfigurationRoot(), path);
    }

    @Override
    public void persistNode(String path, Object configNode, Class configurableClass) throws ConfigurationException {
        storageBackend.persistNode(path, configNode, configurableClass);
    }


    @Override
    public void refreshNode(String path) throws ConfigurationException {
        ConfigPathUtil.replaceNode(getConfigurationRoot(),path,storageBackend.getConfigurationNode(path));
    }

    @Override
    public void removeNode(String path) throws ConfigurationException {
        storageBackend.removeNode(path);
        ConfigPathUtil.removeNode(getConfigurationRoot(), path);
    }


    @Override
    public java.util.Iterator search(String liteXPathExpression) throws ConfigurationException {
        return ConfigPathUtil.search(getConfigurationRoot(), liteXPathExpression);
    }


}
