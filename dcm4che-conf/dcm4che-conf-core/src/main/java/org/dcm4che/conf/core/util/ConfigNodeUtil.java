package org.dcm4che.conf.core.util;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;

import java.util.*;

public class ConfigNodeUtil {


    public static void replaceNode(Object rootConfigNode, String path, Object replacementConfigNode) {
        JXPathContext.newContext(rootConfigNode).getPointer(path).setValue(replacementConfigNode);
    }

    public static Object getNode(Object rootConfigNode, String path) {
        return JXPathContext.newContext(rootConfigNode).getValue(path);
    }

    public static boolean nodeExists(Map<String, Object> rootConfigNode, String path) {
        try {
            return getNode(rootConfigNode, path) != null;
        } catch (JXPathNotFoundException e) {
            return false;
        }
    }

    public static void removeNode(Map<String, Object> configurationRoot, String path) {
        JXPathContext.newContext(configurationRoot).removePath(path);
    }

    public static Iterator search(Map<String, Object> configurationRoot, String liteXPathExpression) throws IllegalArgumentException {
        return JXPathContext.newContext(configurationRoot).iterate(liteXPathExpression);
    }

    public static boolean validatePath(String path) {
        return true;
    }

    public String[] split(String path) {
        // TODO: support slash as a symbol with escaping?
        return path.split("/");
    }

    public static String escape(String str) {
        // TODO: implement escaping
        return str;
    }
}
