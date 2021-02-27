package com.github.alanger.shiroext.realm.activedirectory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mvn test -DargLine="-Durl=ldap://corp.company.com:389 -Dusername=userLogin -Dpassword=userPassword"
 * -Dsurefire.skipAfterFailureCount=1
 * https://github.com/apache/shiro/blob/master/core/src/test/groovy/org/apache/shiro/realm/AuthenticatingRealmIntegrationTest.groovy
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActiveDirectoryRealmTest extends ActiveDirectoryRealm {

    static Logger logger = LoggerFactory.getLogger(ActiveDirectoryRealmTest.class);

    String NAME = System.getProperty("name", "CORP");
    String URL = System.getProperty("url", null);
    String USERNAME = System.getProperty("username", null);
    String PASSWORD = System.getProperty("password", null);
    String SYSTEM_USERNAME = System.getProperty("systemUsername", USERNAME);
    String SYSTEM_PASSWORD = System.getProperty("systemPassword", PASSWORD);

    String server = URL.replaceAll(".*://|:.*", ""); // Server name from URL
    String dn = "DC=" + server.replaceAll("\\.", ",DC="); // DN from server name

    String SEARCH_BASE = System.getProperty("searchBase", dn);
    String SEARCH_FILTER = System.getProperty("searchFilter",
            "(&(objectCategory=person)(objectClass=user)(sAMAccountName={0}))");
    String PRINCIPAL_SUFFIX = System.getProperty("principalSuffix", "@" + server.toUpperCase());
    String ROLE_BASE = System.getProperty("roleBase", dn);
    String ROLE_SEARCH = System.getProperty("roleSearch", "(&(objectClass=group)(member={0}))");

    String GROUP = System.getProperty("group", null); // Group the user is a member of
    String GROUP_NESTED = System.getProperty("groupNested", null); // Group nested in another group
    String COMMON_ROLE = System.getProperty("commonRole", "Common_Realm_Group"); // Name of coomon group

    public ActiveDirectoryRealmTest() {
        setName(NAME);
        setUrl(URL);
        setSystemUsername(SYSTEM_USERNAME);
        setSystemPassword(SYSTEM_PASSWORD);
        setSearchBase(SEARCH_BASE);
        setSearchFilter(SEARCH_FILTER);
        setPrincipalSuffix(PRINCIPAL_SUFFIX);
        setAuthenticationCachingEnabled(true);
        setRoleBase(ROLE_BASE);
        setRoleSearch(ROLE_SEARCH);
        onInit();
    }

    public static String trimDomain(String username) {
        // CORP.COMPANY.COM\login
        if (username != null && username.indexOf("\\") != -1) {
            String[] names = username.split("\\\\");
            username = names.length == 2 ? names[1] : null;
        }
        return username;
    }

    @BeforeClass
    public static void before() throws Throwable {
        logger.debug("#### Test running for user : {}", System.getProperty("username"));
    }

    @Test
    public void test01_Authentication() throws Throwable {
        AuthenticationToken token = new UsernamePasswordToken(USERNAME, PASSWORD);
        assertNotNull(doGetAuthenticationInfo(token));
    }

    @Test
    public void test02_Role() throws Throwable {
        Set<String> roles = getRoleNamesForUser(USERNAME, getLdapContextFactory().getSystemLdapContext());
        assertTrue(!roles.isEmpty());
        if (GROUP != null)
            assertTrue(roles.contains(GROUP));
        if (GROUP_NESTED != null)
            assertFalse(roles.contains(GROUP_NESTED));
        assertFalse(roles.contains(COMMON_ROLE));
    }

    @Test
    public void test03_RoleNested() throws Throwable {
        setRoleNested(true);
        setCommonRole(COMMON_ROLE);
        Set<String> roles = getRoleNamesForUser(USERNAME, getLdapContextFactory().getSystemLdapContext());
        assertTrue(!roles.isEmpty());
        if (GROUP != null)
            assertTrue(roles.contains(GROUP));
        if (GROUP_NESTED != null)
            assertTrue(roles.contains(GROUP_NESTED));
        assertTrue(roles.contains(COMMON_ROLE));
    }

    @Test
    public void test04_LdapContextFactory() throws Throwable {
        setRoleNested(true);
        setCommonRole(COMMON_ROLE);
        // contextFactory = org.apache.shiro.realm.ldap.JndiLdapContextFactory
        // contextFactory.url = ldaps://ad.domain.com:636
        // contextFactory.systemUsername = shiro@domain.com
        // contextFactory.systemPassword = password
        JndiLdapContextFactory contextFactory = new JndiLdapContextFactory();
        contextFactory.setUrl(URL);
        contextFactory.setSystemUsername(trimDomain(SYSTEM_USERNAME + getPrincipalSuffix()));
        contextFactory.setSystemPassword(SYSTEM_PASSWORD);
        contextFactory.getSystemLdapContext();
        contextFactory.getLdapContext((Object) (trimDomain(USERNAME + getPrincipalSuffix())), (Object) PASSWORD);

        setLdapContextFactory(contextFactory);

        AuthenticationToken token = new UsernamePasswordToken(USERNAME + getPrincipalSuffix(), PASSWORD);
        doGetAuthenticationInfo(token);

        Set<String> roles = getRoleNamesForUser(USERNAME, getLdapContextFactory().getSystemLdapContext());
        assertTrue(!roles.isEmpty());
        if (GROUP != null)
            assertTrue(roles.contains(GROUP));
        if (GROUP_NESTED != null)
            assertTrue(roles.contains(GROUP_NESTED));
        assertTrue(roles.contains(COMMON_ROLE));
    }

    @Test
    public void test05_RealmNamed() throws Throwable {
        setNamed(true);
        // without domain name
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        // with right domain name
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        // with bad domain name
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        // domain name changed to bad
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
    }

    @Test
    public void test06_RealmNotNamed() throws Throwable {
        setNamed(false);
        // without domain name
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        // with right domain name
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        // with bad domain name
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
    }

}
