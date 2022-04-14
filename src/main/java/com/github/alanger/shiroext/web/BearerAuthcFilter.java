package com.github.alanger.shiroext.web;

import com.github.alanger.shiroext.authc.PrincipalNamePasswordToken;
import com.github.alanger.shiroext.realm.IPrincipalName;

import org.apache.shiro.authc.AuthenticationToken;

public class BearerAuthcFilter extends BasicAuthcFilter implements IPrincipalName {

    public static final String BEARER = "Bearer";

    protected String principalNameAttribute;

    public BearerAuthcFilter() {
        setAuthcScheme(BEARER);
        setAuthzScheme(BEARER);
    }

    @Override
    public String getPrincipalNameAttribute() {
        return principalNameAttribute;
    }

    @Override
    public void setPrincipalNameAttribute(String principalNameAttribute) {
        this.principalNameAttribute = principalNameAttribute;
    }

    @Override
    protected String[] getPrincipalsAndCredentials(String scheme, String token) {
        return new String[] { token, token };
    }

    @Override
    protected AuthenticationToken createToken(String username, String password,
            boolean rememberMe, String host) {
        PrincipalNamePasswordToken token = new PrincipalNamePasswordToken(username, password, rememberMe, host);
        token.setPrincipalNameAttribute(getPrincipalNameAttribute());
        return token;
    }

}
