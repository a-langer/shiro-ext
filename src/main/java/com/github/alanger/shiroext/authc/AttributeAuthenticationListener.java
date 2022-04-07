package com.github.alanger.shiroext.authc;

import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alanger.shiroext.AttributeMapper;
import com.github.alanger.shiroext.realm.AttributeProvider;

public class AttributeAuthenticationListener extends AttributeMapper implements AuthenticationListener {

    protected transient Logger log = LoggerFactory.getLogger(AttributeAuthenticationListener.class);

    private transient RealmSecurityManager securityManager = null;

    private String onSuccessAttribute = AttributeAuthenticationListener.class.getCanonicalName();

    public String getOnSuccessAttribute() {
        return onSuccessAttribute;
    }

    public void setOnSuccessAttribute(String onSuccessAttribute) {
        this.onSuccessAttribute = onSuccessAttribute;
    }

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        log.trace("onSuccess token: {} , info: {}", token, info);

        if (securityManager == null)
            securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();

        Session session = SecurityUtils.getSubject().getSession();
        String username = (String) info.getPrincipals().getPrimaryPrincipal();
        Set<String> infoRealms = info.getPrincipals().getRealmNames();

        for (Realm realm : securityManager.getRealms()) {

            if (infoRealms.contains(realm.getName())) {
                if (log.isTraceEnabled())
                    log.debug("User {} is authorized through realm: {} = {}", username, realm.getName(), realm);

                if (realm instanceof AttributeProvider) {
                    AttributeProvider attributeProvider = (AttributeProvider) realm;
                    Map<String, Object> attrs = attributeProvider.getAttributesForUser(username);
                    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                        if (size() > 0 && !containsKey(entry.getKey())) {
                            continue;
                        }
                        session.setAttribute(entry.getKey(), entry.getValue());
                    }

                    if (log.isTraceEnabled())
                        log.debug("Realm {} is AttributeProvider, in session added keys: {}", realm.getName(),
                                attrs.keySet());
                }
            }
        }
        if (onSuccessAttribute != null)
            session.setAttribute(onSuccessAttribute, true);
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        log.trace("onFailure token: {} , exception: {}", token, ae);
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        log.trace("onLogout principals: {}", principals);
    }

}
