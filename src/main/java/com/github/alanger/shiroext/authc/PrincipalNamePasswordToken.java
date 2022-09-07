package com.github.alanger.shiroext.authc;

import com.github.alanger.shiroext.realm.IPrincipalName;

import org.apache.shiro.authc.UsernamePasswordToken;

public class PrincipalNamePasswordToken extends UsernamePasswordToken implements IPrincipalName {

    private String principalNameAttribute;

    public PrincipalNamePasswordToken(String username, char[] password) {
        super(username, password);
    }

    public PrincipalNamePasswordToken(String username, String password) {
        super(username, password);
    }

    public PrincipalNamePasswordToken(String username, char[] password, String host) {
        super(username, password, host);
    }

    public PrincipalNamePasswordToken(String username, String password, String host) {
        super(username, password, host);
    }

    public PrincipalNamePasswordToken(String username, char[] password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public PrincipalNamePasswordToken(String username, String password, boolean rememberMe) {
        super(username, password, rememberMe);
    }

    public PrincipalNamePasswordToken(String username, char[] password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    public PrincipalNamePasswordToken(String username, String password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    @Override
    public String getPrincipalNameAttribute() {
        return principalNameAttribute;
    }

    @Override
    public void setPrincipalNameAttribute(String principalNameAttribute) {
        this.principalNameAttribute = principalNameAttribute;
    }

}
