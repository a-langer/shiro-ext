package com.github.alanger.shiroext.realm.jdbc;

import static com.github.alanger.shiroext.realm.RealmUtils.asList;
import static com.github.alanger.shiroext.realm.RealmUtils.filterBlackOrWhite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alanger.shiroext.realm.ICommonPermission;
import com.github.alanger.shiroext.realm.ICommonRole;
import com.github.alanger.shiroext.realm.IFilterPermission;
import com.github.alanger.shiroext.realm.IFilterRole;
import com.github.alanger.shiroext.realm.IPrincipalName;

public class JdbcRealmName extends JdbcRealm
        implements ICommonPermission, ICommonRole, IPrincipalName, IFilterRole, IFilterPermission {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String commonRole = null;
    protected String commonPermission = null;
    protected String principalNameAttribute;
    private String roleWhiteList;
    private String roleBlackList;
    private String permissionWhiteList;
    private String permissionBlackList;
    protected String principalNameQuery;
    protected boolean skipIfNullAttribute = false;
    protected boolean findByPassword = false;

    protected AuthenticationToken getAuthenticationToken(AuthenticationToken token, String nameAttribute)
            throws AuthenticationException {

        if (logger.isTraceEnabled())
            logger.trace("token: {}, nameAttribute: {}, findByPassword: {}", token, nameAttribute, findByPassword);

        UsernamePasswordToken upToken;
        if (token instanceof UsernamePasswordToken) {
            upToken = (UsernamePasswordToken) token;
        } else {
            upToken = new UsernamePasswordToken(token.getPrincipal().toString(), (char[]) token.getCredentials());
        }
        String username = upToken.getUsername();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement(principalNameQuery);
            ps.setString(1, username);

            String password = new String((char[]) upToken.getCredentials());
            if (findByPassword)
                ps.setString(2, password);

            rs = ps.executeQuery();

            if (logger.isTraceEnabled())
                logger.trace("principalNameQuery: {}, {}", principalNameQuery, username);

            boolean foundResult = false;

            while (rs.next()) {

                if (foundResult) {
                    throw new AuthenticationException(
                            "More than one row of principal found for user [" + username
                                    + "]. Principal name must be unique.");
                }

                String principalName = nameAttribute != null ? rs.getString(nameAttribute) : rs.getString(1);
                if (logger.isTraceEnabled()) {
                    logger.trace("Got principal [{}] by attribute [{}] and username [{}]", principalName, nameAttribute,
                            username);
                }

                if (principalName != null) {
                    upToken = new UsernamePasswordToken(principalName, upToken.getPassword(),
                            upToken.isRememberMe(), upToken.getHost());
                }

                foundResult = true;
            }
        } catch (SQLException e) {
            final String message = "There was a SQL error while getting principal name of user [" + username + "]";
            if (logger.isErrorEnabled()) {
                logger.error(message, e);
            }
            throw new AuthenticationException(message, e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(conn);
        }
        return upToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String nameAttribute = null;
        if (token instanceof IPrincipalName) {
            nameAttribute = ((IPrincipalName) token).getPrincipalNameAttribute();
        }
        if (nameAttribute == null) {
            nameAttribute = getPrincipalNameAttribute();
        }

        if (logger.isTraceEnabled())
            logger.trace("Find principal name - skipIfNullAttribute: {}, nameAttribute: {}", skipIfNullAttribute,
                    nameAttribute);

        if (principalNameQuery != null && !(skipIfNullAttribute && nameAttribute == null)) {
            token = getAuthenticationToken(token, nameAttribute);
        }

        if (!(token instanceof UsernamePasswordToken)) {
            token = new UsernamePasswordToken(token.getPrincipal().toString(), (char[]) token.getCredentials());
        }
        return findAuthenticationInfo(token);
    }

    protected AuthenticationInfo findAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        Connection conn = null;
        SimpleAuthenticationInfo info = null;
        try {
            conn = dataSource.getConnection();

            String srcPassword = new String((char[]) upToken.getCredentials());
            String password = null;
            String salt = null;
            switch (saltStyle) {
                case NO_SALT:
                    password = getPasswordForUser(conn, username, srcPassword)[0];
                    break;
                case CRYPT:
                    // TODO: separate password and hash from getPasswordForUser[0]
                    throw new ConfigurationException("Not implemented yet");
                // break;
                case COLUMN:
                    String[] queryResults = getPasswordForUser(conn, username, srcPassword);
                    password = queryResults[0];
                    salt = queryResults[1];
                    break;
                case EXTERNAL:
                    password = getPasswordForUser(conn, username, srcPassword)[0];
                    salt = getSaltForUser(username);
            }

            if (password == null) {
                throw new UnknownAccountException("No account found for user [" + username + "]");
            }

            info = new SimpleAuthenticationInfo(username, password.toCharArray(), getName());

            if (salt != null) {
                if (saltStyle == SaltStyle.COLUMN && saltIsBase64Encoded) {
                    info.setCredentialsSalt(ByteSource.Util.bytes(Base64.decode(salt)));
                } else {
                    info.setCredentialsSalt(ByteSource.Util.bytes(salt));
                }
            }

        } catch (SQLException e) {
            final String message = "There was a SQL error while authenticating user [" + username + "]";
            if (logger.isErrorEnabled()) {
                logger.error(message, e);
            }

            // Rethrow any SQL errors as an authentication exception
            throw new AuthenticationException(message, e);
        } finally {
            JdbcUtils.closeConnection(conn);
        }

        return info;
    }

    private String[] getPasswordForUser(Connection conn, String username, String password) throws SQLException {

        String[] result;
        boolean returningSeparatedSalt = false;
        switch (saltStyle) {
            case NO_SALT:
            case CRYPT:
            case EXTERNAL:
                result = new String[1];
                break;
            default:
                result = new String[2];
                returningSeparatedSalt = true;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (logger.isTraceEnabled())
                logger.trace("authenticationQuery: {}, username: {}", authenticationQuery, username);

            ps = conn.prepareStatement(authenticationQuery);
            ps.setString(1, username);
            if (findByPassword)
                ps.setString(2, password);

            // Execute query
            rs = ps.executeQuery();

            // Loop over results - although we are only expecting one result, since
            // usernames should be unique
            boolean foundResult = false;
            while (rs.next()) {

                // Check to ensure only one row is processed
                if (foundResult) {
                    throw new AuthenticationException(
                            "More than one user row found for user [" + username + "]. Usernames must be unique.");
                }

                result[0] = rs.getString(1);
                if (returningSeparatedSalt) {
                    result[1] = rs.getString(2);
                }

                foundResult = true;
            }
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
        }

        return result;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = (SimpleAuthorizationInfo) super.doGetAuthorizationInfo(principals);
        info.addRoles(asList(commonRole));
        filterBlackOrWhite(info.getRoles(), roleWhiteList, roleBlackList);
        info.addStringPermissions(asList(commonPermission));
        filterBlackOrWhite(info.getStringPermissions(), permissionWhiteList, permissionBlackList);
        return info;
    }

    @Override
    public String getCommonRole() {
        return commonRole;
    }

    @Override
    public void setCommonRole(String commonRole) {
        this.commonRole = commonRole;
    }

    @Override
    public String getCommonPermission() {
        return commonPermission;
    }

    @Override
    public void setCommonPermission(String commonPermission) {
        this.commonPermission = commonPermission;
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
    public String getRoleWhiteList() {
        return roleWhiteList;
    }

    @Override
    public void setRoleWhiteList(String roleWhiteList) {
        this.roleWhiteList = roleWhiteList;
    }

    @Override
    public String getRoleBlackList() {
        return roleBlackList;
    }

    @Override
    public void setRoleBlackList(String roleBlackList) {
        this.roleBlackList = roleBlackList;
    }

    @Override
    public String getPermissionWhiteList() {
        return permissionWhiteList;
    }

    @Override
    public void setPermissionWhiteList(String permissionWhiteList) {
        this.permissionWhiteList = permissionWhiteList;
    }

    @Override
    public String getPermissionBlackList() {
        return permissionBlackList;
    }

    @Override
    public void setPermissionBlackList(String permissionBlackList) {
        this.permissionBlackList = permissionBlackList;
    }

    public String getPrincipalNameQuery() {
        return principalNameQuery;
    }

    public void setPrincipalNameQuery(String principalNameQuery) {
        this.principalNameQuery = principalNameQuery;
    }

    public boolean isSkipIfNullAttribute() {
        return skipIfNullAttribute;
    }

    public void setSkipIfNullAttribute(boolean skipIfNullAttribute) {
        this.skipIfNullAttribute = skipIfNullAttribute;
    }

    public boolean isFindByPassword() {
        return findByPassword;
    }

    public void setFindByPassword(boolean findByPassword) {
        this.findByPassword = findByPassword;
    }

}