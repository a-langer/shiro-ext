package com.github.alanger.shiroext;

import java.util.Map;
import java.util.Properties;

public class AttributeMapper extends Properties {

    public Map<Object, Object> getAttributes() {
        return this;
    }

    public void setAttributes(Map<Object, Object> attributes) {
        putAll(attributes);
    }

    public void setAttribute(String key, Object value) {
        put(key, value);
    }

    public Object getAttribute(String key) {
        return get(key);
    }
}
