package com.deposition.infra.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@ConfigurationProperties(prefix = "deposition.security")
public class EndpointAccessProperties {

    private List<Rule> rules = new ArrayList<>();

    @Data
    public static class Rule {
        private HttpMethod method;
        private String path;
        private String access = Access.AUTHENTICATED.value;

        public Access accessType() {
            return Access.from(access);
        }

        public String roleName() {
            var resolvedAccess = access == null ? Access.AUTHENTICATED.value : access.trim();
            if (!resolvedAccess.toLowerCase(Locale.ROOT).startsWith("role:")) {
                return null;
            }

            var roleName = resolvedAccess.substring("role:".length()).trim();
            if (roleName.isEmpty()) {
                throw new IllegalArgumentException("Role-based access rule must declare a role name");
            }
            return roleName;
        }
    }

    public enum Access {
        AUTHENTICATED("authenticated"),
        ANONYMOUS("anonymous"),
        ROLE("role");

        private final String value;

        Access(String value) {
            this.value = value;
        }

        public static Access from(String value) {
            if (value == null || value.isBlank()) {
                return AUTHENTICATED;
            }

            var normalized = value.trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "authenticated" -> AUTHENTICATED;
                case "anonymous" -> ANONYMOUS;
                default -> {
                    if (normalized.startsWith("role:")) {
                        yield ROLE;
                    }
                    throw new IllegalArgumentException("Unsupported endpoint access value: " + value);
                }
            };
        }
    }
}