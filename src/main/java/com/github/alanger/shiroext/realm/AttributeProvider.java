package com.github.alanger.shiroext.realm;

import java.util.Map;

public interface AttributeProvider {
    Map<String, Object> getAttributesForUser(String username);
}
