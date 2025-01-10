package com.github.akarazhev.activej.examples.boot;

import io.activej.config.Config;
import io.activej.inject.Injector;
import io.activej.inject.module.ModuleBuilder;

import java.net.InetAddress;

import static io.activej.config.converter.ConfigConverters.ofInetAddress;
import static io.activej.config.converter.ConfigConverters.ofInteger;

//[START EXAMPLE]
public final class ConfigModuleExample {
    private static final String PROPERTIES_FILE = "example.properties";

    public static void main(String[] args) {
        Injector injector = Injector.of(ModuleBuilder.create()
                .bind(Config.class).to(() -> Config.ofClassPathProperties(PROPERTIES_FILE))
                .bind(String.class).to(c -> c.get("phrase"), Config.class)
                .bind(Integer.class).to(c -> c.get(ofInteger(), "number"), Config.class)
                .bind(InetAddress.class).to(c -> c.get(ofInetAddress(), "address"), Config.class)
                .build());

        System.out.println(injector.getInstance(String.class));
        System.out.println(injector.getInstance(Integer.class));
        System.out.println(injector.getInstance(InetAddress.class));
    }
}
//[END EXAMPLE]
