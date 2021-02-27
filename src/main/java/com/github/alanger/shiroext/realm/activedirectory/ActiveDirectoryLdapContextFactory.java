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

    @Override
    public LdapContext getLdapContext(Object principal, Object credentials)
            throws NamingException, IllegalStateException {
        if (principal != null && principalSuffix != null) {
            principal += principalSuffix;
        }
        return super.getLdapContext(principal, credentials);
    }

}
