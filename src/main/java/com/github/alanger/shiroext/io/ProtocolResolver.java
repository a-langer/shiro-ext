package com.github.alanger.shiroext.io;

@FunctionalInterface
public interface ProtocolResolver {

    Resource resolve(String location, ResourceLoader resourceLoader);

}