package com.aimemory.security;

import com.aimemory.entity.Tenant;

public final class TenantContext {

    private static final ThreadLocal<Tenant> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Tenant tenant) {
        CURRENT.set(tenant);
    }

    public static Tenant get() {
        Tenant t = CURRENT.get();
        if (t == null) {
            throw new IllegalStateException("Nenhum tenant no contexto — request não autenticada");
        }
        return t;
    }

    public static void clear() {
        CURRENT.remove();
    }
}