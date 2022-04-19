package com.github.alanger.shiroext.realm.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

import com.github.alanger.shiroext.authc.PrincipalNamePasswordToken;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JdbcRealmNameTest extends JdbcRealmName {

    static String USER_NAME = "admin";
    static String PASSWORD = "admin_password";
    static String API_KEY = "admin_token";
    static String[] USER_ROLES = { "manager", "user" };
    static String[] USER_PERMISSIONS = { "perm1", "perm2" };
    static String COMMON_ROLE = "Common_Realm_Group";
    static String COMMON_PERMISSION = "common_permission";
    static PrincipalNamePasswordToken TOKEN = new PrincipalNamePasswordToken(USER_NAME, PASSWORD.toCharArray());

    public JdbcRealmNameTest() throws Throwable {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();

            stmt.executeUpdate("DROP TABLE IF EXISTS USERS");
            String sql = "CREATE TABLE IF NOT EXISTS USERS ("
                    + "ID IDENTITY"
                    + ", LOGIN VARCHAR(2000) NOT NULL"
                    + ", PASSWORD VARCHAR(2000) NOT NULL"
                    + ", CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ", CONSTRAINT n_unique UNIQUE (LOGIN)"
                    + ")";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO USERS (LOGIN, PASSWORD) "
                    + String.format("VALUES ('%s', '%s')", USER_NAME, PASSWORD);
            stmt.executeUpdate(sql);

            stmt.executeUpdate("DROP TABLE IF EXISTS API_KEY");
            sql = "CREATE TABLE IF NOT EXISTS API_KEY ("
                    + "ID IDENTITY"
                    + ", LOGIN VARCHAR(2000) NOT NULL"
                    + ", TOKEN VARCHAR(2000) NOT NULL"
                    + ", CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO API_KEY (LOGIN, TOKEN) "
                    + String.format("VALUES ('%s', '%s')", USER_NAME, API_KEY);
            stmt.executeUpdate(sql);

            stmt.executeUpdate("DROP TABLE IF EXISTS ROLES");
            sql = "CREATE TABLE IF NOT EXISTS ROLES ("
                    + "ID IDENTITY"
                    + ", LOGIN VARCHAR(2000) NOT NULL"
                    + ", ROLE VARCHAR(2000) NOT NULL"
                    + ", CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(sql);

            for (int i = 0; i < USER_ROLES.length; i++) {
                sql = "INSERT INTO ROLES (LOGIN, ROLE) "
                        + String.format("VALUES ('%s', '%s')", USER_NAME, USER_ROLES[i]);
                stmt.executeUpdate(sql);
            }

            stmt.executeUpdate("DROP TABLE IF EXISTS PERMISSIONS");
            sql = "CREATE TABLE IF NOT EXISTS PERMISSIONS ("
                    + "ID IDENTITY"
                    + ", ROLE VARCHAR(2000) NOT NULL"
                    + ", PERMISSION VARCHAR(2000) NOT NULL"
                    + ", CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            stmt.executeUpdate(sql);

            for (int i = 0; i < USER_PERMISSIONS.length; i++) {
                sql = "INSERT INTO PERMISSIONS (ROLE, PERMISSION) "
                        + String.format("VALUES ('%s', '%s')", USER_ROLES[0], USER_PERMISSIONS[i]);
                stmt.executeUpdate(sql);
            }

            stmt.close();
        }

        setDataSource(dataSource);

        setAuthenticationQuery("SELECT PASSWORD FROM USERS WHERE LOGIN = ?");
        setUserRolesQuery("SELECT ROLE FROM ROLES WHERE LOGIN = ?");
        setPermissionsQuery("SELECT PERMISSION FROM PERMISSIONS WHERE ROLE = ?");
        setPermissionsLookupEnabled(true);

        setCommonRole(COMMON_ROLE);
        setCommonPermission(COMMON_PERMISSION);
    }

    private void assertAuthorizationInfo(PrincipalCollection principals) throws Throwable {
        AuthorizationInfo authz = doGetAuthorizationInfo(principals);

        Collection<String> roles = authz.getRoles();
        assertTrue(roles.containsAll(Arrays.asList(USER_ROLES)));
        assertTrue(roles.contains(COMMON_ROLE));

        Collection<String> perms = authz.getStringPermissions();
        assertTrue(perms.containsAll(Arrays.asList(USER_PERMISSIONS)));
        assertTrue(perms.contains(COMMON_PERMISSION));
    }

    @Test
    public void test01_Authentication() throws Throwable {
        AuthenticationInfo authc = doGetAuthenticationInfo(TOKEN);
        assertNotNull(authc);
        assertEquals(USER_NAME, authc.getPrincipals().getPrimaryPrincipal().toString());
        assertEquals(getAvailablePrincipal(authc.getPrincipals()), authc.getPrincipals().getPrimaryPrincipal());
        assertAuthorizationInfo(authc.getPrincipals());
    }

    @Test
    public void test02_NameAttribute() throws Throwable {
        setPrincipalNameQuery("SELECT LOGIN FROM API_KEY WHERE TOKEN = ?");
        TOKEN.setUsername(API_KEY);
        test01_Authentication();

        setPrincipalNameQuery("SELECT TOKEN, LOGIN FROM API_KEY WHERE TOKEN = ?");
        setPrincipalNameAttribute("LOGIN");
        TOKEN.setUsername(API_KEY);
        test01_Authentication();
    }

    @Test
    public void test03_NameAttributeToken() throws Throwable {
        setPrincipalNameQuery("SELECT LOGIN FROM API_KEY WHERE TOKEN = ?");
        setPrincipalNameAttribute(null);
        TOKEN.setUsername(API_KEY);
        TOKEN.setPrincipalNameAttribute(null);
        test01_Authentication();

        setPrincipalNameQuery("SELECT TOKEN, LOGIN FROM API_KEY WHERE TOKEN = ?");
        setPrincipalNameAttribute(null);
        TOKEN.setUsername(API_KEY);
        TOKEN.setPrincipalNameAttribute("LOGIN");
        test01_Authentication();
    }

    @Test(expected = UnknownAccountException.class)
    public void test04_SkipIfNullNameAttributeToken() throws Throwable {
        setPrincipalNameQuery("SELECT LOGIN FROM API_KEY WHERE TOKEN = ?");
        setPrincipalNameAttribute(null);
        setSkipIfNullAttribute(true);
        TOKEN.setUsername(API_KEY);
        TOKEN.setPrincipalNameAttribute(null);
        test01_Authentication();
    }

}
