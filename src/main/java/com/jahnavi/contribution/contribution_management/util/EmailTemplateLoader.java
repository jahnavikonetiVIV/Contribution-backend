package com.jahnavi.contribution.contribution_management.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads email templates from classpath resources.
 */
public final class EmailTemplateLoader {

    private EmailTemplateLoader() {
    }

    /**
     * Loads a template from classpath. Path should be relative to resources, e.g. "email-templates/contribution/fund-receipt-confirmation-template.html"
     */
    public static String loadTemplate(String path) throws IOException {
        try (InputStream is = EmailTemplateLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Template not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
