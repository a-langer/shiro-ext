package com.github.alanger.shiroext;

import java.util.Map;
import java.util.Properties;

public class AttributeMapper extends Properties {

    public Map<Object, Object> getAttributes() {
        return this;
    }

    public Map<Object, Object> getAttr() {
        return this;
    }

    public void setAttributes(Map<Object, Object> attributes) {
        super.putAll(attributes);
    }

    public void setAttrs(Map<Object, Object> attributes) {
        setAttributes(attributes);
    }

    public void setAttribute(String key, Object value) {
        super.put(key, value);
    }

    public void setAttr(String key, Object value) {
        setAttribute(key, value);
    }

    public Object getAttribute(String key) {
        return super.get(key);
    }

    public Object getAttr(String key) {
        return getAttribute(key);
    }
}
