package com.github.alanger.shiroext.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.alanger.shiroext.util.CollectionUtils;

class HeaderValueHolder {

    private final List<Object> values = new LinkedList<>();

    void setValue(Object value) {
        this.values.clear();
        if (value != null) {
            this.values.add(value);
        }
    }

    void addValue(Object value) {
        this.values.add(value);
    }

    void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    void addValueArray(Object values) {
        CollectionUtils.mergeArrayIntoCollection(values, this.values);
    }

    List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

}
