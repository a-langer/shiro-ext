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

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

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

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String nameAttribute = null;
        if (upToken instanceof IPrincipalName) {
            nameAttribute = ((IPrincipalName) upToken).getPrincipalNameAttribute();
        }
        if (nameAttribute == null) {
            nameAttribute = getPrincipalNameAttribute();
        }

        logger.severe("1 token: " + upToken + ", " + upToken.getClass() + ", " + String.valueOf(upToken.getPassword()));
        logger.severe("2 nameAttribute: " + nameAttribute + ", principalNameQuery: " + principalNameQuery + ", skipIfNullAttribute: " + this.skipIfNullAttribute);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("finest enabled");
        }

        if (principalNameQuery != null && !(skipIfNullAttribute && nameAttribute == null)) {
            String username = upToken.getUsername();
            logger.finest("3 username : " + username );

            Connection conn = null;
            logger.finest("3.1 username : " + username );
            PreparedStatement ps = null;
            logger.finest("3.2 username : " + username );
            ResultSet rs = null;
            logger.finest("3.3 username : " + username );
            try {
                conn = dataSource.getConnection();
                logger.finest("3.4 conn : " + conn );

                ps = conn.prepareStatement(principalNameQuery);
                logger.finest("3.5 ps : " + ps );
                ps.setString(1, username);
                logger.finest("3.6 ps : " + ps );

                rs = ps.executeQuery();
                logger.finest("3.7 rs : " + rs );

                boolean foundResult = false;

                logger.finest("4 conn : " + conn );
                while (rs.next()) {
                    logger.finest("5 rs : " + rs );

                    if (foundResult) {
                        throw new AuthenticationException(
                                "More than one row of principal found for user [" + username
                                        + "]. Principal name must be unique.");
                    }
                    logger.finest("6 foundResult : " + foundResult );

                    String principalName = nameAttribute != null ? rs.getString(nameAttribute) : rs.getString(1);
                    if (log.isTraceEnabled()) {
                        log.trace("Got principal [{}] by attribute [{}] and name [{}]", principalName, nameAttribute,
                                username);
                    }
                    logger.finest("7 principalName : " + principalName );

                    if (principalName != null) {
                        upToken.setUsername(principalName);
                    }
                    logger.finest("8 upToken : " + upToken );

                    foundResult = true;
                }
                logger.finest("9 foundResult : " + foundResult );
            } catch (SQLException e) {
                final String message = "There was a SQL error while getting principal name of user [" + username + "]";
                if (log.isErrorEnabled()) {
                    log.error(message, e);
                }
                logger.finest("9.1 SQLException : " + e );
                throw new AuthenticationException(message, e);
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                JdbcUtils.closeConnection(conn);
            }
        }
        logger.finest("10 super upToken: " + upToken );
        return super.doGetAuthenticationInfo(upToken);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = (SimpleAuthorizationInfo) super.doGetAuthorizationInfo(principals);
        info.addRoles(asList(commonRole));
        info.addStringPermissions(asList(commonPermission));
        return info;
    }

}