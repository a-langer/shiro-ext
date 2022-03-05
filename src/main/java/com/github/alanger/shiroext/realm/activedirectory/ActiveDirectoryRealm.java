package com.github.alanger.shiroext.realm.activedirectory;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.github.alanger.shiroext.realm.AttributeProvider;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveDirectoryRealm extends AbstractLdapRealm implements AttributeProvider {

    private static Logger log = LoggerFactory.getLogger(ActiveDirectoryRealm.class);

    private static final String DELIMETER = ",";

    private LdapContextFactory ldapContextFactory;

    public ActiveDirectoryRealm() {
        searchFilter = "(&(objectCategory=person)(objectClass=user)(sAMAccountName={0}))";
    }

    private String userAttributes = "displayName, distinguishedName, memberOf, mail, title, department, telephoneNumber";

    public void setUserAttributes(String userAttributes) {
        this.userAttributes = userAttributes;
    }

    protected String[] getUserAttributesArray() {
        String[] returningAttributes = {};
        if (this.userAttributes != null) {
            returningAttributes = this.userAttributes.replaceAll("\\s+", "").split(DELIMETER);
        }
        return returningAttributes;
    }

    public String getUrl() {
        return url;
    }

    public String getPrincipalSuffix() {
        return principalSuffix;
    }

    private String roleNameAttribute = "memberOf";

    public String getRoleNameAttribute() {
        return roleNameAttribute;
    }

    public void setRoleNameAttribute(String roleNameAttribute) {
        this.roleNameAttribute = roleNameAttribute;
    }

    private String roleName = "CN";

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    protected boolean roleNested = false;

    public boolean getRoleNested() {
        return this.roleNested;
    }

    public void setRoleNested(boolean roleNested) {
        this.roleNested = roleNested;
    }

    protected boolean roleShortName = true;

    public boolean getRoleShortName() {
        return roleShortName;
    }

    public void setRoleShortName(boolean roleShortName) {
        this.roleShortName = roleShortName;
    }

    protected String commonRole = null;

    public String getCommonRole() {
        return commonRole;
    }

    public void setCommonRole(String commonRole) {
        this.commonRole = commonRole;
    }

    protected String roleBase = "";

    public String getRoleBase() {
        return this.roleBase;
    }

    public void setRoleBase(String roleBase) {
        this.roleBase = roleBase;
    }

    protected String roleSearch = "(&(objectClass=group)(member={0}))";

    public String getRoleSearch() {
        return this.roleSearch;
    }

    public void setRoleSearch(String roleSearch) {
        this.roleSearch = roleSearch;
    }

    /**
     * Should we ignore PartialResultExceptions when iterating over
     * NamingEnumerations? Microsoft Active Directory often
     * returns referrals, which lead to PartialResultExceptions. Unfortunately
     * there's no stable way to detect, if the
     * Exceptions really come from an AD referral. Set to true to ignore
     * PartialResultExceptions.
     */
    protected boolean adCompat = false;

    public boolean getAdCompat() {
        return adCompat;
    }

    public void setAdCompat(boolean adCompat) {
        this.adCompat = adCompat;
    }

    private boolean named = false;

    public boolean getNamed() {
        return named;
    }

    public void setNamed(boolean named) {
        this.named = named;
    }

    private long sizeLimit = 0;

    public long getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    private int timeLimit = 0;

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    private Map<String, String> groupRolesMap = new LinkedHashMap<>();

    public void setGroupRolesMap(Map<String, String> groupRolesMap) {
        this.groupRolesMap.putAll(groupRolesMap);
    }

    public Map<String, String> getGroupRolesMap() {
        return this.groupRolesMap;
    }

    @Override
    protected void onInit() {
        this.initContextFactory();
        super.onInit();
    }

    private LdapContextFactory initContextFactory() {
        if (ldapContextFactory == null) {
            if (log.isDebugEnabled()) {
                log.debug("No LdapContextFactory specified - creating a default instance.");
            }
            ActiveDirectoryLdapContextFactory defaultFactory = new ActiveDirectoryLdapContextFactory();
            defaultFactory.setPrincipalSuffix(getPrincipalSuffix());
            defaultFactory.setUrl(getUrl());
            defaultFactory.setSystemUsername(getSystemUsername());
            defaultFactory.setSystemPassword(getSystemPassword());
            setLdapContextFactory(defaultFactory);
        }
        return ldapContextFactory;
    }

    public LdapContextFactory getLdapContextFactory() {
        return this.ldapContextFactory;
    }

    @Override
    public void setLdapContextFactory(LdapContextFactory ldapContextFactory) {
        this.ldapContextFactory = ldapContextFactory;
        super.setLdapContextFactory(ldapContextFactory);
    }

    public String getSystemUsername() {
        return this.systemUsername;
    }

    public String getSystemPassword() {
        return this.systemPassword;
    }

    private String roleWhiteList;

    public String getRoleWhiteList() {
        return roleWhiteList;
    }

    public void setRoleWhiteList(String roleWhiteList) {
        this.roleWhiteList = roleWhiteList;
    }

    private String roleBlackList;

    public String getRoleBlackList() {
        return roleBlackList;
    }

    public void setRoleBlackList(String roleBlackList) {
        this.roleBlackList = roleBlackList;
    }

    private boolean isRoleBlackOrWhite(String roleName) {
        if (roleWhiteList != null) {
            return roleName.matches(roleWhiteList);
        }
        if (roleBlackList != null) {
            return !roleName.matches(roleBlackList);
        }
        return true;
    }

    private String userWhiteList;

    public String getUserWhiteList() {
        return userWhiteList;
    }

    public void setUserWhiteList(String userWhiteList) {
        this.userWhiteList = userWhiteList;
    }

    private String userBlackList;

    public String getUserBlackList() {
        return userBlackList;
    }

    public void setUserBlackList(String userBlackList) {
        this.userBlackList = userBlackList;
    }

    private boolean isUserBlackOrWhite(String userName) {
        if (userWhiteList != null) {
            return userName.matches(userWhiteList);
        }
        if (userBlackList != null) {
            return !userName.matches(userBlackList);
        }
        return true;
    }

    private String userPrefix;

    public String getUserPrefix() {
        return userPrefix;
    }

    public void setUserPrefix(String userPrefix) {
        this.userPrefix = userPrefix;
    }

    private boolean isValidPrincipalName(String userPrincipalName) {
        if (userPrincipalName != null) {
            if (StringUtils.hasLength(userPrincipalName) && userPrincipalName.contains("@")) {
                String userPrincipalWithoutDomain = userPrincipalName.split("@")[0].trim();
                if (StringUtils.hasLength(userPrincipalWithoutDomain)) {
                    return isUserBlackOrWhite(userPrincipalWithoutDomain);
                }
            } else if (StringUtils.hasLength(userPrincipalName)) {
                return isUserBlackOrWhite(userPrincipalName);
            }
        }
        return false;
    }

    private String withoutPrefix(String username) {
        if (getUserPrefix() != null && username != null) {
            return username.startsWith(getUserPrefix()) ? username.replaceFirst(getUserPrefix(), "") : username;
        }
        return username;
    }

    private String withoutDomain(String username) {
        String domain = null;
        if (username != null && username.indexOf("\\") != -1) {
            String[] names = username.split("\\\\");
            username = names.length == 2 ? names[1] : null;
            domain = names.length == 2 ? names[0] : null;
        }
        if (username != null && username.indexOf("@") != -1) {
            String[] names = username.split("@");
            username = names.length == 2 ? names[0] : null;
            domain = names.length == 2 ? names[1] : null;
        }

        // Remove user prefix
        username = withoutPrefix(username);

        if (log.isTraceEnabled())
            log.trace("withOutDomain username: {}, domain: {}: ", username, domain);

        // Domain name maybe null or correct value
        if (!getNamed() && (domain != null && !domain.equalsIgnoreCase(getName()))) {
            return null;
        }
        // Necessarily required correct domain name
        if (getNamed() && (domain == null || !domain.equalsIgnoreCase(getName()))) {
            return null;
        }

        return username;
    }

    @Override // Interface org.apache.shiro.realm.Realm getAuthenticationInfo
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        try {
            log.trace("doGetAuthenticationInfo token: {}", token);

            // SimpleAuthenticationInfo
            return this.queryForAuthenticationInfo(token, this.getLdapContextFactory());
        } catch (javax.naming.AuthenticationException e) {
            // LDAP: error code 49 - 80090308: LdapErr: DSID-0C09042F ...
            String msg = "LDAP authentication failed.";
            if (log.isTraceEnabled())
                log.trace("doGetAuthenticationInfo {} {}", msg, e.toString());
            throw new AuthenticationException(msg, e);
        } catch (NamingException ne) {
            String msg = "LDAP naming error while attempting to authenticate user.";
            throw new AuthenticationException(msg, ne);
        }
    }

    protected AuthenticationInfo doGetAuthenticationInfo(final String username, final String password)
            throws AuthenticationException {
        return doGetAuthenticationInfo(new UsernamePasswordToken(username, password));
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token,
            LdapContextFactory ldapContextFactory) throws NamingException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String username = withoutDomain(upToken.getUsername());
        if (!isValidPrincipalName(username)) {
            return null;
        }

        // Binds using the username and password provided by the user.
        LdapContext ctx = null;
        try {
            ctx = ldapContextFactory.getLdapContext((Object) username, String.valueOf(upToken.getPassword()));
        } finally {
            LdapUtils.closeContext(ctx);
        }

        return buildAuthenticationInfo(username, upToken.getPassword());
    }

    protected AuthenticationInfo buildAuthenticationInfo(String username, char[] password) {
        if (getUserPrefix() != null) {
            username = getUserPrefix() + username;
        }
        return new SimpleAuthenticationInfo(username, password, getName());
    }

    @Override // Interface org.apache.shiro.realm.Realm
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try {
            log.trace("doGetAuthorizationInfo: {}", principals);

            // SimpleAuthorizationInfo
            return this.queryForAuthorizationInfo(principals, this.getLdapContextFactory());
        } catch (NamingException e) {
            String msg = "LDAP naming error while attempting to retrieve authorization for user [" + principals + "].";
            throw new AuthorizationException(msg, e);
        }
    }

    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
            LdapContextFactory ldapContextFactory) throws NamingException {
        Set<String> roleNames = Collections.emptySet();

        String username = withoutPrefix((String) getAvailablePrincipal(principals));
        if (!isValidPrincipalName(username)) {
            return null;
        }

        // Perform context search
        LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();
        try {
            roleNames = getRoleNamesForUser(username, ldapContext);
        } finally {
            LdapUtils.closeContext(ldapContext);
        }

        return buildAuthorizationInfo(roleNames);
    }

    private final AuthorizationInfo buildAuthorizationInfo(Set<String> roleNames) {
        return new SimpleAuthorizationInfo(roleNames);
    }

    // Find roles by group in ldap
    protected final Set<String> getRoleNamesForUser(String username, LdapContext ldapContext) throws NamingException {

        log.trace("getRoleNamesForUser username: {}", username);

        Set<String> rolesForGroups = new LinkedHashSet<>();

        if (!isValidPrincipalName(username)) {
            return rolesForGroups;
        }

        if (StringUtils.hasLength(commonRole))
            rolesForGroups.add(commonRole);

        Map<String, Collection<String>> attrCollection = getAttributesForUser(username, ldapContext);
        Collection<String> groupNames = attrCollection.get(roleNameAttribute); // memberOf

        if (groupNames != null) {
            if (getRoleNested()) {
                Set<String> newGroups = new LinkedHashSet<>(groupNames);
                while (!newGroups.isEmpty()) {
                    Set<String> newThisRound = new LinkedHashSet<>();

                    for (String roleDN : newGroups) {
                        if (log.isTraceEnabled())
                            log.trace("Perform a nested group search with base {} and filter {}", roleBase,
                                    format(roleSearch, roleDN));

                        NamingEnumeration<SearchResult> results = search(ldapContext, roleBase, roleSearch,
                                new Object[] { roleDN }, new String[] { roleName });
                        try {
                            while (results.hasMore()) {
                                SearchResult result = results.next();
                                Attributes attrs = result.getAttributes();
                                if (attrs == null)
                                    continue;
                                String dname = getDistinguishedName(ldapContext, roleBase, result);
                                if (dname != null && !groupNames.contains(dname)) {
                                    groupNames.add(dname);
                                    newThisRound.add(dname);

                                    if (log.isTraceEnabled()) {
                                        log.trace("  Found nested role {}", dname);
                                    }

                                }
                            }
                        } catch (PartialResultException ex) {
                            if (!adCompat)
                                throw ex;
                        } finally {
                            results.close();
                        }
                    }
                    newGroups = newThisRound;
                }
            }

            // Short role name [Admin_Group,Manger_Group]
            if (getRoleShortName()) {
                groupNames = getRoleShortNamesByAttribute(groupNames, roleName); // "CN"
            }

            if (log.isTraceEnabled())
                log.debug("Groups found for user [{}]: {}", username, groupNames);

            // Load roles from map [admin, all, manager]
            rolesForGroups.addAll(getRoleNamesForGroups(groupNames));

            // Filter roles by black or white list
            if (roleBlackList != null || roleWhiteList != null) {
                filterRoleBlackOrWhite(rolesForGroups);
            }
        }

        return rolesForGroups;
    }

    protected void filterRoleBlackOrWhite(Collection<String> roleNames) {
        roleNames.removeIf(g -> !isRoleBlackOrWhite(g));
    }

    protected NamingEnumeration<SearchResult> search(DirContext context, String searchBase, String searchFilter,
            Object[] searchArguments, String[] returningAttributes) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returningAttributes);
        searchCtls.setCountLimit(sizeLimit);
        searchCtls.setTimeLimit(timeLimit);
        return context.search(searchBase, searchFilter, searchArguments, searchCtls);
    }

    private Map<String, Collection<String>> getAttributesForUser(String username, DirContext ldapContext)
            throws NamingException {
        Map<String, Collection<String>> attributes = new LinkedHashMap<>();

        NamingEnumeration<SearchResult> answer = search(ldapContext, searchBase, searchFilter,
                new Object[] { username }, getUserAttributesArray());
        try {
            if (answer != null && answer.hasMoreElements()) {
                SearchResult sr = answer.next();

                if (log.isTraceEnabled())
                    log.debug("Retrieving attributes for user {}", sr.getName()); // CN=John Smith,OU=Group_Users

                Attributes attrs = sr.getAttributes();

                if (attrs != null) {
                    NamingEnumeration<? extends Attribute> ae = attrs.getAll();
                    while (ae.hasMore()) {
                        Attribute attr = ae.next();
                        String attrName = attr.getID();
                        Collection<String> attrValues = LdapUtils.getAllAttributeValues(attr);

                        if (log.isTraceEnabled())
                            log.debug("Ldap user attribute: {} = {} ", attrName, attrValues);

                        attributes.put(attrName, attrValues);
                    }
                }
            }
        } catch (PartialResultException ex) {
            if (!adCompat)
                throw ex;
        } finally {
            if (answer != null)
                answer.close();
        }

        return attributes;
    }

    @Override // AttributeProvider interface
    public Map<String, Object> getAttributesForUser(String username) {
        Map<String, Object> attributes = new LinkedHashMap<>();

        username = withoutDomain(username);
        if (!isValidPrincipalName(username)) {
            return attributes;
        }

        LdapContext ldapContext = null;
        try {
            ldapContext = getLdapContextFactory().getSystemLdapContext();
            attributes.putAll(getAttributesForUser(username, ldapContext));
        } catch (NamingException e) {
            log.error("Failure load attributes for user " + username + " from realm " + getName(), e);
        } finally {
            LdapUtils.closeContext(ldapContext);
        }
        return attributes;
    }

    // Ldap group name by attribute name
    public static final Collection<String> getRoleShortNamesByAttribute(Collection<String> groupNames,
            String userRoleAttribute) {
        Set<String> roleNames = new LinkedHashSet<>(groupNames.size());
        for (String groupName : groupNames) {
            // CN=Admin_Group
            String[] groupNamesPart = groupName.split(",");
            if (groupNamesPart == null || groupNamesPart.length <= 0)
                continue;

            for (String groupNamePart : groupNamesPart) {
                String[] attributes = groupNamePart.split("=");
                if (attributes == null || attributes.length != 2)
                    continue;

                // CN
                String attributeKey = attributes[0].trim();

                if (attributeKey.equalsIgnoreCase(userRoleAttribute)) {
                    // Admin_Group
                    String attributeValue = attributes[1].trim();
                    roleNames.add(attributeValue);
                }
            }
        }
        return roleNames;
    }

    // Role name by ldap group name from Ini
    private final Collection<String> getRoleNamesForGroups(Collection<String> groupNames) {
        Set<String> roleNames = new LinkedHashSet<>(groupNames.size());

        // Use roles map
        if (groupRolesMap != null && groupRolesMap.size() > 0) {
            for (String groupName : groupNames) {
                String strRoleNames = groupRolesMap.get(groupName);
                if (strRoleNames != null) {
                    for (String name : strRoleNames.split(DELIMETER)) {
                        if (log.isDebugEnabled())
                            log.debug("User is member of group [{}] so adding role [{}]", groupName, name);
                        roleNames.add(name);
                    }
                }
            }
        }
        // Original group names
        else {
            roleNames.addAll(groupNames);
        }
        return roleNames;
    }

    // ------------ LDAP utils
    private final String getAttributeValue(Attribute attr) throws NamingException {
        if (attr == null)
            return null;
        Object value = attr.get();
        if (value == null)
            return null;
        String valueString = null;
        if (value instanceof byte[])
            valueString = new String((byte[]) value);
        else
            valueString = value.toString();

        return valueString;
    }

    protected final String getAttributeValue(String attrId, Attributes attrs) throws NamingException {

        if (log.isTraceEnabled())
            log.trace("  retrieving attribute {}", attrId);

        if (attrId == null || attrs == null)
            return null;

        Attribute attr = attrs.get(attrId);
        if (attr == null)
            return null;

        return getAttributeValue(attr);
    }

    // Returns the distinguished name of a search result.
    protected String getDistinguishedName(DirContext context, String base, SearchResult result) throws NamingException {
        // Get the entry's distinguished name. For relative results, this means
        // we need to composite a name with the base name, the context name, and
        // the result name. For non-relative names, use the returned name.
        String resultName = result.getName();
        if (result.isRelative()) {
            if (log.isTraceEnabled()) {
                log.trace("  search returned relative name: {}", resultName);
            }
            NameParser parser = context.getNameParser("");
            Name contextName = parser.parse(context.getNameInNamespace());
            Name baseName = parser.parse(base);

            // Bugzilla 32269
            Name entryName = parser.parse(new CompositeName(resultName).get(0));

            Name name = contextName.addAll(baseName);
            name = name.addAll(entryName);
            return name.toString();
        } else {
            if (log.isTraceEnabled()) {
                log.trace("  search returned absolute name: {}", resultName);
            }
            try {
                // Normalize the name by running it through the name parser.
                NameParser parser = context.getNameParser("");
                URI userNameUri = new URI(resultName);
                String pathComponent = userNameUri.getPath();
                // Should not ever have an empty path component, since that is DN
                if (pathComponent.length() < 1) {
                    throw new InvalidNameException("Search returned unparseable absolute name: " + resultName);
                }
                Name name = parser.parse(pathComponent.substring(1));
                return name.toString();
            } catch (URISyntaxException e) {
                throw new InvalidNameException("Search returned unparseable absolute name: " + resultName);
            }
        }
    }

}
