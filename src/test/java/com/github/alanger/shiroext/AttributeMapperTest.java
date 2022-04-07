package com.github.alanger.shiroext;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AttributeMapperTest {

    AttributeMapper mapper;
    HashMap<String, String> map;

    public AttributeMapperTest() {
        mapper = new AttributeMapper();
        map = new HashMap<>();
        map.put("key1", "value1");
    }

    private void assertAll() {
        assertEquals("value1", mapper.get("key1"));
        assertEquals("value1", mapper.getAttributes().get("key1"));
        assertEquals("value1", mapper.getAttribute("key1"));
        assertEquals("value1", mapper.getProperty("key1"));
    }

    @Test
    public void test01_Put() throws Throwable {
        mapper.put("key1", "value1");
        assertAll();
    }

    @Test
    public void test02_AttributesPut() throws Throwable {
        mapper.getAttributes().put("key1", "value1");
        assertAll();
    }

    @Test
    public void test03_PutAll() throws Throwable {
        mapper.put("key1", "value2");
        mapper.putAll(map);
        assertAll();
    }

    @Test
    public void test04_AttributesPutAll() throws Throwable {
        mapper.put("key1", "value2");
        mapper.getAttributes().putAll(map);
        assertAll();
    }

}
