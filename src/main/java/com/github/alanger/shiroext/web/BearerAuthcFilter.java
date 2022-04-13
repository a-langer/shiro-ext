package com.github.alanger.shiroext.web;

public class BearerAuthcFilter extends BasicAuthcFilter {

    public static final String BEARER = "Bearer";

    public BearerAuthcFilter() {
        setAuthcScheme(BEARER);
        setAuthzScheme(BEARER);
    }

    @Override
    protected String[] getPrincipalsAndCredentials(String scheme, String token) {
        return new String[] { token, token };
    }

}
