package org.dcm4che3.conf.core.refindexing;

import org.apache.commons.jxpath.JXPathContext;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.conf.core.storage.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.util.PathPattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefIndexingTest {

    private HashMap<String, Path> uuidToSimplePathCache;
    private Configuration configuration;
    private DefaultBeanVitalizer vitalizer;

    @Before
    public void init() {
        configuration = new InMemoryConfiguration();
        uuidToSimplePathCache = new HashMap<String, Path>();
        configuration = new ReferenceIndexingDecorator(configuration, uuidToSimplePathCache);
        vitalizer = new DefaultBeanVitalizer();
    }

    @Test
    public void testSimpleChanges() {


        SampleBigConf sampleBigConf = new SampleBigConf();

        sampleBigConf.child1 = new SampleReferableConfClass("UUID1");
        sampleBigConf.child1.setMyName("Romeo");

        SampleReferableConfClass uuid2obj = new SampleReferableConfClass("UUID2");
        uuid2obj.setMyName("Juliet");

        sampleBigConf.childList.add(uuid2obj);
        sampleBigConf.childList.add(new SampleReferableConfClass("UUID3"));

        sampleBigConf.childMap.put("first", new SampleReferableConfClass("UUID4"));
        sampleBigConf.childMap.put("second", new SampleReferableConfClass("UUID5"));

        configuration.persistNode("/confRoot/bigConf1", vitalizer.createConfigNodeFromInstance(sampleBigConf), null);

        // check if uuids are indexed
        Assert.assertEquals(5, uuidToSimplePathCache.size());

        PathPattern pathPattern = new PathPattern(Configuration.REFERENCE_BY_UUID_PATTERN);

        Assert.assertEquals("Romeo", ((Map) configuration.getConfigurationNode(pathPattern.set("uuid", "UUID1").path(), null)).get("myName"));

        //TODO
        System.out.println(configuration.getConfigurationNode(pathPattern.set("uuid", "UUID2").path(), null));


    }

    public void testExistingUUID() {
        //TODO
    }

    public static class Integers {
        public List<Integer> getNumbers() {
            return Arrays.asList(1, 2, 3);
        }
    }


    @Test
    public void testJX() {


        Integers ints = new Integers();


        JXPathContext context = JXPathContext.newContext(ints);
        Integer thirdInt = (Integer) context.getValue("numbers[3]");
    }

}
