package com.github.alanger.shiroext.realm;

public interface IFilterPermission {

    public String getPermissionWhiteList();

    public void setPermissionWhiteList(String permissionWhiteList);

    public String getPermissionBlackList();

    public void setPermissionBlackList(String permissionBlackList);

}
