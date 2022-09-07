package com.github.alanger.shiroext.realm.jdbc;

import static com.github.alanger.shiroext.realm.RealmUtils.asList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.alanger.shiroext.realm.ICommonPermission;
import com.github.alanger.shiroext.realm.ICommonRole;
import com.github.alanger.shiroext.realm.IPrincipalName;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcRealmName extends JdbcRealm implements ICommonPermission, ICommonRole, IPrincipalName {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String commonRole = null;
    protected String commonPermission = null;
    protected String principalNameAttribute;
    protected String principalNameQuery;
    protected boolean skipIfNullAttribute = false;

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

    protected AuthenticationToken getAuthenticationToken(AuthenticationToken token, String nameAttribute)
            throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement(principalNameQuery);
            ps.setString(1, username);

            rs = ps.executeQuery();

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

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String nameAttribute = null;
        if (token instanceof IPrincipalName) {
            nameAttribute = ((IPrincipalName) token).getPrincipalNameAttribute();
        }
        if (nameAttribute == null) {
            nameAttribute = getPrincipalNameAttribute();
        }

        if (principalNameQuery != null && !(skipIfNullAttribute && nameAttribute == null)) {
            token = getAuthenticationToken(token, nameAttribute);
        }
        return super.doGetAuthenticationInfo(token);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = (SimpleAuthorizationInfo) super.doGetAuthorizationInfo(principals);
        info.addRoles(asList(commonRole));
        info.addStringPermissions(asList(commonPermission));
        return info;
    }

}