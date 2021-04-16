package com.github.alanger.shiroext.realm.activedirectory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

/**
 * mvn test -DargLine="-Durl=ldap://corp.company.com:389 -Dusername=userLogin -Dpassword=userPassword" -Dgroup=MyGroup
 * -Dsurefire.skipAfterFailureCount=1
 * https://github.com/apache/shiro/blob/master/core/src/test/groovy/org/apache/shiro/realm/AuthenticatingRealmIntegrationTest.groovy
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActiveDirectoryRealmTest extends ActiveDirectoryRealm {

    static Logger log = LoggerFactory.getLogger(ActiveDirectoryRealmTest.class);

    String NAME = System.getProperty("name", "CORP");
    String URL = System.getProperty("url", null);
    String USERNAME = System.getProperty("username", null);
    String PASSWORD = System.getProperty("password", null);
    String SYSTEM_USERNAME = System.getProperty("systemUsername", USERNAME);
    String SYSTEM_PASSWORD = System.getProperty("systemPassword", PASSWORD);

    String server = URL != null ? URL.replaceAll(".*://|:.*", "") : "CORP.COMPANY.COM"; // Server name from URL
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

    @Rule
    public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder.newInstance()
            .usingBindCredentials(SYSTEM_PASSWORD).usingDomainDsn("DC=CORP,DC=COMPANY,DC=COM")
            .importingLdifs("example.ldif").build();

    public ActiveDirectoryRealmTest() {
        setName(NAME);
        if (URL != null && SYSTEM_USERNAME != null && SYSTEM_PASSWORD != null) {
            setUrl(URL);
            setSystemUsername(SYSTEM_USERNAME);
            setSystemPassword(SYSTEM_PASSWORD);
        } else {
            USERNAME = "langer";
            PASSWORD = "password";
            GROUP = "Developers";
            GROUP_NESTED = null; // Embedded ldap not supported group nested
            MockLdapContextFactory contextFactory = new MockLdapContextFactory(embeddedLdapRule);
            setLdapContextFactory(contextFactory);
        }
        setSearchBase(SEARCH_BASE);
        setSearchFilter(SEARCH_FILTER);
        setPrincipalSuffix(PRINCIPAL_SUFFIX);
        setAuthenticationCachingEnabled(true);
        setRoleBase(ROLE_BASE);
        setRoleSearch(ROLE_SEARCH);
        onInit();
    }

    private static String trimDomain(String username) {
        // CORP.COMPANY.COM\login
        if (username != null && username.indexOf("\\") != -1) {
            String[] names = username.split("\\\\");
            username = names.length == 2 ? names[1] : null;
        }
        return username;
    }

    @Test
    public void test00_LdapContextFactory() throws Throwable {
        if (URL != null && SYSTEM_USERNAME != null && SYSTEM_PASSWORD != null) {
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
            assertFalse(roles.isEmpty());
            assertTrue(roles.contains(COMMON_ROLE));
            assertTrue(GROUP != null ? roles.contains(GROUP) : true);
            assertTrue(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : true);
            setCommonRole(null);
        }
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
        assertFalse(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        assertFalse(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : false);
    }

    @Test
    public void test03_RoleNested() throws Throwable {
        setRoleNested(true);
        setCommonRole(COMMON_ROLE);
        Set<String> roles = getRoleNamesForUser(USERNAME, getLdapContextFactory().getSystemLdapContext());
        assertTrue(!roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        assertTrue(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : true);
    }

    @Test
    public void test04_RoleBlackOrWhiteList() throws Throwable {
        Set<String> r = getRoleNamesForUser(USERNAME, getLdapContextFactory().getSystemLdapContext());
        Set<String> roles = new LinkedHashSet<>(r);

        setRoleBlackList(GROUP + "|other_role1");
        filterRoleBlackOrWhite(roles);
        assertFalse(roles.isEmpty());
        assertFalse(roles.contains(COMMON_ROLE));
        assertFalse(GROUP != null ? roles.contains(GROUP) : false);

        roles = new LinkedHashSet<>(r);
        setRoleBlackList("other_role1|other_role2");
        filterRoleBlackOrWhite(roles);
        assertFalse(roles.isEmpty());
        assertFalse(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        setRoleBlackList(null);

        roles = new LinkedHashSet<>(r);
        setRoleWhiteList(GROUP + "|other_role1");
        filterRoleBlackOrWhite(roles);
        assertFalse(roles.isEmpty());
        assertFalse(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);

        roles = new LinkedHashSet<>(r);
        setRoleWhiteList("other_role1|other_role2");
        filterRoleBlackOrWhite(roles);
        assertTrue(roles.isEmpty());
        assertFalse(roles.contains(COMMON_ROLE));
        assertFalse(GROUP != null ? roles.contains(GROUP) : false);
        setRoleWhiteList(null);
    }

    @Test
    public void test05_RealmNotNamed() throws Throwable {
        setNamed(false);
        // without domain name
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        // with right domain name
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        // with bad domain name
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
    }

    @Test
    public void test06_RealmNotNamedUserBlackList() throws Throwable {
        setNamed(false);

        setUserBlackList(USERNAME + "|other_user");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));

        setUserBlackList("other_user1|other_user2");
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setUserBlackList(null);
    }

    @Test
    public void test07_RealmNotNamedUserWhiteList() throws Throwable {
        setNamed(false);

        setUserWhiteList(USERNAME + "|other_user");
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));

        setUserWhiteList("other_user1|other_user2");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setUserBlackList(null);
    }

    @Test
    public void test08_RealmNamed() throws Throwable {
        setNamed(true);

        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        setName(NAME);
    }

    @Test
    public void test09_RealmNamedUserBlackList() throws Throwable {
        setNamed(true);

        setUserBlackList(USERNAME + "|other_user");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        setName(NAME);

        setUserBlackList("other_user1|other_user2");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        setName(NAME);
        setUserBlackList(null);
    }

    @Test
    public void test10_RealmNamedUserWhiteList() throws Throwable {
        setNamed(true);

        setUserWhiteList(USERNAME + "|other_user");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNotNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        setName(NAME);

        setUserWhiteList("other_user1|other_user2");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken("OTHER_DOMAIN" + "\\" + USERNAME, PASSWORD)));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(new UsernamePasswordToken(NAME + "\\" + USERNAME, PASSWORD)));
        setName(NAME);
        setUserWhiteList(null);
    }

}
