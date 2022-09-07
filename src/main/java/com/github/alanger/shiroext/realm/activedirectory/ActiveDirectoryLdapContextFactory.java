package com.github.alanger.shiroext.realm.activedirectory;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.shiro.realm.ldap.JndiLdapContextFactory;

public class ActiveDirectoryLdapContextFactory extends JndiLdapContextFactory {

    protected String principalSuffix = null;

    public void setPrincipalSuffix(String principalSuffix) {
        this.principalSuffix = principalSuffix;
    }

    public String getPrincipalSuffix() {
        return principalSuffix;
    }

    private boolean isSystemPrincipal(Object principal) {
        return principal != null && (principal.equals(getSystemUsername()) && getSystemUsername().contains("@"));
    }

    @Override
    public LdapContext getLdapContext(Object principal, Object credentials)
            throws NamingException, IllegalStateException {
        if (!isSystemPrincipal(principal) && getPrincipalSuffix() != null) {
            principal += getPrincipalSuffix();
        }
        return super.getLdapContext(principal, credentials);
    }

}
