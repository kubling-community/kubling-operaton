package com.kubling.samples.operaton.support;

import org.apache.commons.io.IOUtils;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public abstract class AbstractContainersConfig {

    protected static final String USER_DIR = System.getProperty("user.dir");

    protected static final Network network = Network.newNetwork();

    public enum SoftTransactionStrategy {
        IMMEDIATE_OPERATION,
        DEFER_OPERATION
    }

    public static byte[] getClasspathBytes(String path) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is == null) {
                    throw new IllegalStateException("Classpath resource not found: " + path);
                }
                return IOUtils.toByteArray(is);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
