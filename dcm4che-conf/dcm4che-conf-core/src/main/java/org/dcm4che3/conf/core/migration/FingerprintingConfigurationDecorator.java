package org.dcm4che3.conf.core.migration;

import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.core.impl.DelegatingConfiguration;
import org.dcm4che3.conf.api.ConfigurationException;

import java.util.Map;

/**
 * @author Roman K
 */
public class FingerprintingConfigurationDecorator extends DelegatingConfiguration {


    public FingerprintingConfigurationDecorator(Configuration delegate) {
        super(delegate);
    }


    @Override
    public Object getConfigurationNode(String path) throws ConfigurationException {

        // TODO: get class info using super.getConfigurationNodeClass(), get fingerprint, and compare with existing class
        return super.getConfigurationNode(path);
    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        // TODO: fingerprint configurableClass and check the one in the backend
        super.persistNode(path, configNode, configurableClass);

    }
}
