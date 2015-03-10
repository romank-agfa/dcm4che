/*
 * **** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2014
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */
package org.dcm4che3.conf.core.normalization;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.core.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.BeanVitalizer;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.util.ConfigIterators;
import org.dcm4che3.conf.core.util.NodeTraverser;

import java.util.*;

/**
 *
 */
public class DefaultsAndNullFilterDecorator extends DelegatingConfiguration {

    private boolean persistDefaults;
    private NodeTraverser nodeTraverser;
    private BeanVitalizer dummyVitalizer = new BeanVitalizer();

    public DefaultsAndNullFilterDecorator(Configuration delegate, boolean persistDefaults, NodeTraverser nodeTraverser) {
        super(delegate);
        this.persistDefaults = persistDefaults;
        this.nodeTraverser = nodeTraverser;
    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        NodeTraverser.EntryFilter filterDefaults = new NodeTraverser.NoopFilter() {
            @Override
            public void applyFilter(Map<String, Object> containerNode, AnnotatedConfigurableProperty property) throws ConfigurationException {

                // if the value for a property equals to default, filter it out
                if (property.getAnnotation(ConfigurableProperty.class).defaultValue().equals(String.valueOf(containerNode.get(property.getAnnotatedName())))
                        || containerNode.get(property.getAnnotatedName()) == null) {
                    containerNode.remove(property.getAnnotatedName());
                }
            }
        };

        // filter out defaults
        if (configurableClass != null && !persistDefaults)
            nodeTraverser.traverseTree(configNode,configurableClass,filterDefaults);

        super.persistNode(path, configNode, configurableClass);
    }


    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {

        NodeTraverser.EntryFilter applyDefaults = new NodeTraverser.NoopFilter() {
            @Override
            public void applyFilter(Map<String, Object> containerNode, AnnotatedConfigurableProperty property) throws ConfigurationException {

                // if no value for this property, see if there is default and set it
                if (!containerNode.containsKey(property.getAnnotatedName())) {
                    String defaultValue = property.getAnnotation(ConfigurableProperty.class).defaultValue();
                    if (!defaultValue.equals(ConfigurableProperty.NO_DEFAULT_VALUE)) {
                        Object normalized = dummyVitalizer.lookupDefaultTypeAdapter(property.getRawClass()).normalize(defaultValue, property, dummyVitalizer);
                        containerNode.put(property.getAnnotatedName(), normalized);
                    } else {
                        containerNode.put(property.getAnnotatedName(), null);
                    }
                }
            }
        };

        // fill in default values for properties that are null and have defaults
        Map<String, Object> node = (Map<String, Object>) super.getConfigurationNode(path, configurableClass);
        if (configurableClass != null && node != null)
            nodeTraverser.traverseTree(node, configurableClass, applyDefaults);
        return node;
    }


    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {
        return super.search(liteXPathExpression);
    }
}
