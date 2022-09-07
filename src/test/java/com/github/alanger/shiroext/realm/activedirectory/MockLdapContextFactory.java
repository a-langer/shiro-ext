package com.github.alanger.shiroext.realm.activedirectory;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;

import org.zapodot.junit.ldap.EmbeddedLdapRule;

public class MockLdapContextFactory extends ActiveDirectoryLdapContextFactory {

    public final EmbeddedLdapRule embeddedLdapRule;

    public MockLdapContextFactory(EmbeddedLdapRule embeddedLdapRule) {
        this.embeddedLdapRule = embeddedLdapRule;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected LdapContext createLdapContext(Hashtable env) throws NamingException {
        return new LdapContextWrapper(embeddedLdapRule.dirContext());
    }

    @Override
    public LdapContext getLdapContext(Object principal, Object credentials)
            throws NamingException, IllegalStateException {
        return new LdapContextWrapper(embeddedLdapRule.dirContext());
    }

}

class LdapContextWrapper implements LdapContext {
    final DirContext dirContext;

    public LdapContextWrapper(DirContext dirContext) {
        this.dirContext = dirContext;
    }

    @Override
    public void bind(Name arg0, Object arg1, Attributes arg2) throws NamingException {
        this.dirContext.bind(arg0, arg1, arg2);
    }

    @Override
    public void bind(String arg0, Object arg1, Attributes arg2) throws NamingException {
        this.dirContext.bind(arg0, arg1, arg2);
    }

    @Override
    public DirContext createSubcontext(Name arg0, Attributes arg1) throws NamingException {
        return this.dirContext.createSubcontext(arg0, arg1);
    }

    @Override
    public DirContext createSubcontext(String arg0, Attributes arg1) throws NamingException {
        return this.dirContext.createSubcontext(arg0, arg1);
    }

    @Override
    public Attributes getAttributes(Name arg0) throws NamingException {
        return this.dirContext.getAttributes(arg0);
    }

    @Override
    public Attributes getAttributes(String arg0) throws NamingException {
        return this.dirContext.getAttributes(arg0);
    }

    @Override
    public Attributes getAttributes(Name arg0, String[] arg1) throws NamingException {
        return this.dirContext.getAttributes(arg0, arg1);
    }

    @Override
    public Attributes getAttributes(String arg0, String[] arg1) throws NamingException {
        return this.dirContext.getAttributes(arg0, arg1);
    }

    @Override
    public DirContext getSchema(Name arg0) throws NamingException {
        return this.dirContext.getSchema(arg0);
    }

    @Override
    public DirContext getSchema(String arg0) throws NamingException {
        return this.dirContext.getSchema(arg0);
    }

    @Override
    public DirContext getSchemaClassDefinition(Name arg0) throws NamingException {
        return this.dirContext.getSchemaClassDefinition(arg0);
    }

    @Override
    public DirContext getSchemaClassDefinition(String arg0) throws NamingException {
        return this.dirContext.getSchemaClassDefinition(arg0);
    }

    @Override
    public void modifyAttributes(Name arg0, ModificationItem[] arg1) throws NamingException {
        this.dirContext.modifyAttributes(arg0, arg1);
    }

    @Override
    public void modifyAttributes(String arg0, ModificationItem[] arg1) throws NamingException {
        this.dirContext.modifyAttributes(arg0, arg1);
    }

    @Override
    public void modifyAttributes(Name arg0, int arg1, Attributes arg2) throws NamingException {
        this.dirContext.modifyAttributes(arg0, arg1, arg2);
    }

    @Override
    public void modifyAttributes(String arg0, int arg1, Attributes arg2) throws NamingException {
        this.dirContext.modifyAttributes(arg0, arg1, arg2);
    }

    @Override
    public void rebind(Name arg0, Object arg1, Attributes arg2) throws NamingException {
        this.dirContext.rebind(arg0, arg1, arg2);
    }

    @Override
    public void rebind(String arg0, Object arg1, Attributes arg2) throws NamingException {
        this.dirContext.rebind(arg0, arg1, arg2);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name arg0, Attributes arg1) throws NamingException {
        return this.dirContext.search(arg0, arg1);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String arg0, Attributes arg1) throws NamingException {
        return this.dirContext.search(arg0, arg1);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name arg0, Attributes arg1, String[] arg2) throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String arg0, Attributes arg1, String[] arg2) throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name arg0, String arg1, SearchControls arg2) throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String arg0, String arg1, SearchControls arg2)
            throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2);
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name arg0, String arg1, Object[] arg2, SearchControls arg3)
            throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2, arg3);
    }

    @Override
    public NamingEnumeration<SearchResult> search(String arg0, String arg1, Object[] arg2, SearchControls arg3)
            throws NamingException {
        return this.dirContext.search(arg0, arg1, arg2, arg3);
    }

    @Override
    public Object addToEnvironment(String arg0, Object arg1) throws NamingException {
        return this.dirContext.addToEnvironment(arg0, arg1);
    }

    @Override
    public void bind(Name arg0, Object arg1) throws NamingException {
        this.dirContext.bind(arg0, arg1);
    }

    @Override
    public void bind(String arg0, Object arg1) throws NamingException {
        this.dirContext.bind(arg0, arg1);
    }

    @Override
    public void close() throws NamingException {
        // this.dirContext.close(); // java.lang.AbstractMethodError
    }

    @Override
    public Name composeName(Name arg0, Name arg1) throws NamingException {
        return this.dirContext.composeName(arg0, arg1);
    }

    @Override
    public String composeName(String arg0, String arg1) throws NamingException {
        return this.dirContext.composeName(arg0, arg1);
    }

    @Override
    public Context createSubcontext(Name arg0) throws NamingException {
        return this.dirContext.createSubcontext(arg0);
    }

    @Override
    public Context createSubcontext(String arg0) throws NamingException {
        return this.dirContext.createSubcontext(arg0);
    }

    @Override
    public void destroySubcontext(Name arg0) throws NamingException {
        this.dirContext.destroySubcontext(arg0);
    }

    @Override
    public void destroySubcontext(String arg0) throws NamingException {
        this.dirContext.destroySubcontext(arg0);
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return this.dirContext.getEnvironment();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return this.dirContext.getNameInNamespace();
    }

    @Override
    public NameParser getNameParser(Name arg0) throws NamingException {
        return this.dirContext.getNameParser(arg0);
    }

    @Override
    public NameParser getNameParser(String arg0) throws NamingException {
        return this.dirContext.getNameParser(arg0);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name arg0) throws NamingException {
        return this.dirContext.list(arg0);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String arg0) throws NamingException {
        return this.dirContext.list(arg0);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name arg0) throws NamingException {
        return this.dirContext.listBindings(arg0);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String arg0) throws NamingException {
        return this.dirContext.listBindings(arg0);
    }

    @Override
    public Object lookup(Name arg0) throws NamingException {
        return this.dirContext.lookup(arg0);
    }

    @Override
    public Object lookup(String arg0) throws NamingException {
        return this.dirContext.lookup(arg0);
    }

    @Override
    public Object lookupLink(Name arg0) throws NamingException {
        return this.dirContext.lookupLink(arg0);
    }

    @Override
    public Object lookupLink(String arg0) throws NamingException {
        return this.dirContext.lookupLink(arg0);
    }

    @Override
    public void rebind(Name arg0, Object arg1) throws NamingException {
        this.dirContext.rebind(arg0, arg1);
    }

    @Override
    public void rebind(String arg0, Object arg1) throws NamingException {
        this.dirContext.rebind(arg0, arg1);
    }

    @Override
    public Object removeFromEnvironment(String arg0) throws NamingException {
        return this.dirContext.removeFromEnvironment(arg0);
    }

    @Override
    public void rename(Name arg0, Name arg1) throws NamingException {
        this.dirContext.rename(arg0, arg1);
    }

    @Override
    public void rename(String arg0, String arg1) throws NamingException {
        this.dirContext.rename(arg0, arg1);
    }

    @Override
    public void unbind(Name arg0) throws NamingException {
        this.dirContext.unbind(arg0);
    }

    @Override
    public void unbind(String arg0) throws NamingException {
        this.dirContext.unbind(arg0);
    }

    @Override
    public ExtendedResponse extendedOperation(ExtendedRequest arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Control[] getConnectControls() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Control[] getRequestControls() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Control[] getResponseControls() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LdapContext newInstance(Control[] arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reconnect(Control[] arg0) throws NamingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRequestControls(Control[] arg0) throws NamingException {
        // TODO Auto-generated method stub

    }

}
