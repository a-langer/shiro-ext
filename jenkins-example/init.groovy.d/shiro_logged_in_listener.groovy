package com.github.alanger.shiroext.realm.activedirectory;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import jenkins.security.SecurityListener;
import jenkins.util.HttpSessionListener;
import jenkins.security.LastGrantedAuthoritiesProperty.SecurityListenerImpl;
import jenkins.JenkinsHttpSessionListener;
import jenkins.util.SystemProperties$Listener;
import jenkins.util.groovy.GroovyHookScript;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import hudson.WebAppMain;
import hudson.ExtensionList;
import hudson.model.User;
import com.github.alanger.shiroext.realm.AttributeProvider;

/**
 * Jenkins customizations:
 *   https://www.jenkins.io/doc/book/managing/groovy-hook-scripts/
 */
class JenkinsAuthenticationListener implements AuthenticationListener  {

    Logger logger = Logger.getLogger(JenkinsAuthenticationListener.class.getCanonicalName());

    private RealmSecurityManager securityManager = null;

    public JenkinsAuthenticationListener () {
        logger.finest("!!! new JenkinsAuthenticationListener !!!");
    }
    public JenkinsAuthenticationListener (RealmSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        logger.finest("onSuccess token: ${token} , info: ${info}");

        if (securityManager == null)
            securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();

        Session session = SecurityUtils.getSubject().getSession();
        String username = (String) info.getPrincipals()?.getPrimaryPrincipal();
        Set<String> infoRealms = info.getPrincipals()?.getRealmNames();
        boolean needCreated = username != null ? User.getById(username, false) == null : false;
        User user = username != null ? User.getById(username, needCreated ? true : false) : null;
        logger.finest("Listener username: ${username} (${needCreated}), user: ${user}, session: ${session}, infoRealms: ${infoRealms}");

        if (user) {
            for (Realm realm : securityManager.getRealms()) {
                if (infoRealms.contains(realm.getName())) {
                    logger.finest("User ${username} is authorized through realm: ${realm.getName()} = ${realm}");
    
                    if (realm instanceof AttributeProvider) {
                        AttributeProvider attributeProvider = (AttributeProvider) realm;
                        Map<String, Object> attributes = attributeProvider.getAttributesForUser(username);
                        logger.finest("Realm ${realm.getName()} is AttributeProvider, in session added keys: ${attributes.keySet()}");
    
                        for (String key : attributes.keySet()) {
                            session.setAttribute(key, attributes.get(key));
                            logger.finest("  key ${key}, value: ${attributes.get(key)[0]}");
                            switch(key) {
                                case 'displayName':
                                    if (needCreated || !user.getFullName())
                                        user.setFullName(attributes.get(key)[0])
                                    break;
                                case 'department':
                                    if (needCreated || !user.getDescription())
                                        user.setDescription((attributes.get('title') ? attributes.get('title')[0] + ", " : "") + attributes.get(key)[0])
                                    break;
                                case 'mail':
                                    def mailProperty = user.getProperty(hudson.tasks.Mailer.UserProperty.class);
                                    if (needCreated || mailProperty == null || !mailProperty.hasExplicitlyConfiguredAddress()) {
                                        user.addProperty(new hudson.tasks.Mailer.UserProperty(attributes.get(key)[0]))
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
//            SecurityListener.fireLoggedIn(username); // TODO Call method not working in this moment
            user.save();
        }
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        logger.finest("onFailure token: ${token} , exception: ${ae}");
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        logger.finest("onLogout principals: ${principals}");
    }
}

// Listener of http session for Jenkins
class LoggedInListener extends HttpSessionListener {

    Logger logger = Logger.getLogger(LoggedInListener.class.getCanonicalName());

    JenkinsAuthenticationListener authenticationListener;
    DefaultWebSecurityManager securityManager;
    
    public LoggedInListener() {
        logger.finest("!!! new LoggedInListener !!!");
    }

    public void sessionCreated(HttpSessionEvent se) {

        logger.finest("sessionCreated event: ${se}");
        
        // Checks, if Shiro context reloaded
        if (securityManager == null || !securityManager.equals(SecurityUtils.getSecurityManager())) {
            securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
            ModularRealmAuthenticator authenticator = (ModularRealmAuthenticator)securityManager.getAuthenticator();
            authenticator.getAuthenticationListeners().add(new JenkinsAuthenticationListener());
        }

        // Mark session as created for calling method SecurityListener#loggedIn later
        HttpSession session = se.getSession();
        if (session != null) {
            session.setAttribute("sessionCreated", true);
        }
        
    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }
}
// Add listener of http session to Jenkins
ExtensionList.lookup(HttpSessionListener.class).add(0, new LoggedInListener());

// Add security listener to Jenkins
ExtensionList.lookup(SecurityListener.class).add(0, new SecurityListenerImpl());
