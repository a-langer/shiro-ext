package com.github.alanger.shiroext.realm;

import java.util.Map;

public interface IAttributeProvider {
    Map<String, Object> getAttributesForUser(String username);
}
