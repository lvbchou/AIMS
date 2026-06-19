package com.aims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class AimsBackendApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(AimsBackendApplication.class, args);
    }

    private static void loadDotEnv() {
        Path envPath = resolveDotEnvPath();
        if (!Files.exists(envPath)) {
            System.out.println("No .env file found from working directory " + Path.of(System.getProperty("user.dir")).toAbsolutePath());
            return;
        }

        try {
            int loadedCount = 0;
            for (String line : Files.readAllLines(envPath)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                if (!key.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, stripQuotes(value));
                    loadedCount++;
                }
            }
            System.out.println("Loaded .env from " + envPath.toAbsolutePath() + " (" + loadedCount + " values)");
        } catch (IOException ex) {
            System.err.println("Could not load .env file: " + ex.getMessage());
        }
    }

    private static Path resolveDotEnvPath() {
        Path workingDir = Path.of(System.getProperty("user.dir"));
        Path local = workingDir.resolve(".env");
        if (Files.exists(local)) {
            return local;
        }
        Path childBackend = workingDir.resolve("AIMS_Backend").resolve(".env");
        if (Files.exists(childBackend)) {
            return childBackend;
        }
        Path backend = workingDir.resolve("Programming").resolve("AIMS_Backend").resolve(".env");
        if (Files.exists(backend)) {
            return backend;
        }
        Path parentBackend = workingDir.getParent() == null
                ? null
                : workingDir.getParent().resolve("Programming").resolve("AIMS_Backend").resolve(".env");
        if (parentBackend != null && Files.exists(parentBackend)) {
            return parentBackend;
        }
        return local;
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

}
