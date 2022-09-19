package com.github.alanger.shiroext.realm.activedirectory;

import static com.github.alanger.shiroext.realm.RealmUtils.filterBlackOrWhite;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

/**
 * mvn test -DargLine="-Durl=ldap://corp.company.com:389 -Dusername=userLogin
 * -Dpassword=userPassword" -Dgroup=MyGroup
 * -DgroupNested=MyNestedGroup -Dsurefire.skipAfterFailureCount=1
 * https://github.com/apache/shiro/blob/master/core/src/test/groovy/org/apache/shiro/realm/AuthenticatingRealmIntegrationTest.groovy
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActiveDirectoryRealmTest extends ActiveDirectoryRealm {

    static Logger log = LoggerFactory.getLogger(ActiveDirectoryRealmTest.class);

    String NAME = System.getProperty("name", "CORP");
    String URL = System.getProperty("url", null);
    String USERNAME = System.getProperty("username", null);
    String USER_PREFIX = System.getProperty("user_prefix", NAME + ".");
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
        if (URL != null && SYSTEM_USERNAME != null && SYSTEM_PASSWORD != null) {
            setUrl(URL);
            setSystemUsername(SYSTEM_USERNAME);
            setSystemPassword(SYSTEM_PASSWORD);
        } else {
            USERNAME = "langer";
            PASSWORD = "password";
            GROUP = "Developers";
            GROUP_NESTED = null; // Embedded ldap not supported group nested
            setLdapContextFactory(new MockLdapContextFactory(embeddedLdapRule));
        }
        setName(NAME);
        setUserPrefix(USER_PREFIX);
        setSearchBase(SEARCH_BASE);
        setSearchFilter(SEARCH_FILTER);
        setPrincipalSuffix(PRINCIPAL_SUFFIX);
        setAuthenticationCachingEnabled(true);
        setRoleBase(ROLE_BASE);
        setRoleSearch(ROLE_SEARCH);
        setCommonRole(COMMON_ROLE);
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
            LdapContextFactory lcf = getLdapContextFactory();
            setRoleNested(true);

            // # AD ldap context
            // contextFactory =
            // com.github.alanger.shiroext.realm.activedirectory.ActiveDirectoryLdapContextFactory
            // contextFactory.url = ldaps://ad.domain.com:636
            // contextFactory.principalSuffix = @DOMAIN.COM
            // # contextFactory.systemUsername = shiro@SPECIFIED.SUFFIX.COM
            // contextFactory.systemUsername = shiro
            // contextFactory.systemPassword = password
            ActiveDirectoryLdapContextFactory contextFactory = new ActiveDirectoryLdapContextFactory();
            contextFactory.setPrincipalSuffix(getPrincipalSuffix());
            contextFactory.setUrl(URL);
            contextFactory.setSystemUsername(trimDomain(SYSTEM_USERNAME + getPrincipalSuffix()));
            contextFactory.setSystemPassword(SYSTEM_PASSWORD);
            contextFactory.getLdapContext((Object) (trimDomain(USERNAME)), (Object) PASSWORD);
            setLdapContextFactory(contextFactory);

            AuthenticationInfo authc = doGetAuthenticationInfo(USERNAME, PASSWORD);
            assertNotNull(authc);
            AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
            assertNotNull(authz);
            Collection<String> roles = authz.getRoles();
            assertFalse(roles.isEmpty());
            assertTrue(roles.contains(COMMON_ROLE));
            assertTrue(GROUP != null ? roles.contains(GROUP) : true);
            assertTrue(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : true);

            setRoleNested(false);
            setLdapContextFactory(lcf);
        }
    }

    @Test
    public void test01_Authentication() throws Throwable {
        AuthenticationInfo authc = doGetAuthenticationInfo(USERNAME, PASSWORD);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        Collection<String> roles = authz.getRoles();
        assertNotNull(authc);
        assertEquals(USER_PREFIX + USERNAME, (String) getAvailablePrincipal(authc.getPrincipals()));
        assertEquals(getAvailablePrincipal(authc.getPrincipals()), authc.getPrincipals().getPrimaryPrincipal());
        assertTrue(!roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        assertFalse(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : false);
    }

    @Test
    public void test02_Role() throws Throwable {
        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        Collection<String> roles = authz.getRoles();
        assertTrue(!roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        assertFalse(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : false);
    }

    @Test
    public void test03_RoleNested() throws Throwable {
        setRoleNested(true);
        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        Collection<String> roles = authz.getRoles();
        assertTrue(!roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        assertTrue(GROUP_NESTED != null ? roles.contains(GROUP_NESTED) : true);
    }

    @Test
    public void test04_RoleBlackOrWhiteList() throws Throwable {
        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        Collection<String> r = authz.getRoles();
        Collection<String> roles = new LinkedHashSet<>(r);

        setRoleBlackList(GROUP + "|other_role1");
        filterBlackOrWhite(roles, getRoleWhiteList(), getRoleBlackList());
        assertFalse(roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertFalse(GROUP != null ? roles.contains(GROUP) : false);

        roles = new LinkedHashSet<>(r);
        setRoleBlackList("other_role1|other_role2");
        filterBlackOrWhite(roles, getRoleWhiteList(), getRoleBlackList());
        assertFalse(roles.isEmpty());
        assertTrue(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);
        setRoleBlackList(null);

        roles = new LinkedHashSet<>(r);
        setRoleWhiteList(GROUP + "|other_role1");
        filterBlackOrWhite(roles, getRoleWhiteList(), getRoleBlackList());
        assertFalse(GROUP != null ? roles.isEmpty() : false);
        assertFalse(roles.contains(COMMON_ROLE));
        assertTrue(GROUP != null ? roles.contains(GROUP) : true);

        roles = new LinkedHashSet<>(r);
        setRoleWhiteList("other_role1|other_role2");

        filterBlackOrWhite(roles, getRoleWhiteList(), getRoleBlackList());
        assertTrue(roles.isEmpty());
        assertFalse(roles.contains(COMMON_ROLE));
        assertFalse(GROUP != null ? roles.contains(GROUP) : false);
        setRoleWhiteList(null);
    }

    @Test
    public void test05_RealmNotNamed() throws Throwable {
        setNamed(false);
        // without domain name
        assertNotNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        // with right domain name
        assertNotNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        // with bad domain name
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));
    }

    @Test
    public void test06_RealmNotNamedUserBlackList() throws Throwable {
        setNamed(false);

        setUserBlackList(USERNAME + "|other_user");
        assertNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));

        setUserBlackList("other_user1|other_user2");
        assertNotNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        assertNotNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setUserBlackList(null);
    }

    @Test
    public void test07_RealmNotNamedUserWhiteList() throws Throwable {
        setNamed(false);

        setUserWhiteList(USERNAME + "|other_user");
        assertNotNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        assertNotNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));

        setUserWhiteList("other_user1|other_user2");
        assertNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setUserBlackList(null);
    }

    @Test
    public void test08_RealmNamed() throws Throwable {
        setNamed(true);

        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNotNull(authc);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        assertTrue(authz.getRoles().contains(COMMON_ROLE));

        authc = doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        setName("OTHER_REALM");
        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);
        setName(NAME); // Restore default name
    }

    @Test
    public void test09_RealmNamedUserBlackList() throws Throwable {
        setNamed(true);

        setUserBlackList(USERNAME + "|other_user");
        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);
        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);
        authc = doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        setName("OTHER_REALM");
        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);
        setName(NAME);

        setUserBlackList("other_user1|other_user2");
        authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNotNull(authc);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        assertTrue(authz.getRoles().contains(COMMON_ROLE));

        authc = doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        setName("OTHER_REALM");
        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);
        setName(NAME);
        setUserBlackList(null);
    }

    @Test
    public void test10_RealmNamedUserWhiteList() throws Throwable {
        setNamed(true);

        setUserWhiteList(USERNAME + "|other_user");
        AuthenticationInfo authc = doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD);
        assertNull(authc);

        authc = doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD);
        assertNotNull(authc);
        AuthorizationInfo authz = doGetAuthorizationInfo(authc.getPrincipals());
        assertTrue(authz.getRoles().contains(COMMON_ROLE));

        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setName(NAME);

        setUserWhiteList("other_user1|other_user2");
        assertNull(doGetAuthenticationInfo(USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        assertNull(doGetAuthenticationInfo("OTHER_DOMAIN" + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setName("OTHER_REALM");
        assertNull(doGetAuthenticationInfo(NAME + "\\" + USER_PREFIX + USERNAME, PASSWORD));
        setName(NAME);
        setUserWhiteList(null);
    }

    @Test
    public void test11_RealmNamedUserWhiteListWithoutPrefix() throws Throwable {
        String savePrefix = USER_PREFIX;
        USER_PREFIX = "";
        setUserPrefix(null);
        test10_RealmNamedUserWhiteList();
        USER_PREFIX = savePrefix;
        setUserPrefix(USER_PREFIX);
    }

}
