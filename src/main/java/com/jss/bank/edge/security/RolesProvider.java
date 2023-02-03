package com.jss.bank.edge.security;

import java.util.HashSet;
import java.util.Set;

public final class RolesProvider {

    private final Set<String> roles = new HashSet<>();

    {
        roles.add("root");
    }

    public void putRole(final String role) {
        roles.add(role);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final RolesProvider rolesProvider = new RolesProvider();

        public Builder role(final String role) {
            rolesProvider.putRole(role);
            return this;
        }

        public RolesProvider build() {
            return rolesProvider;
        }
    }
}
