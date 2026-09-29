package com.aimemory.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantPlanLimitsTest {

    @Test
    void freePlan_shouldHaveCorrectLimits() {
        Tenant tenant = Tenant.builder().plan(Tenant.Plan.FREE).build();

        Tenant.PlanLimits limits = tenant.getPlanLimits();

        assertThat(limits.memories()).isEqualTo(1_000L);
        assertThat(limits.tokens()).isEqualTo(50_000L);
        assertThat(limits.rateLimitPerMinute()).isEqualTo(100);
    }

    @Test
    void proPlan_shouldHaveCorrectLimits() {
        Tenant tenant = Tenant.builder().plan(Tenant.Plan.PRO).build();

        Tenant.PlanLimits limits = tenant.getPlanLimits();

        assertThat(limits.memories()).isEqualTo(50_000L);
        assertThat(limits.tokens()).isEqualTo(1_000_000L);
        assertThat(limits.rateLimitPerMinute()).isEqualTo(500);
    }

    @Test
    void businessPlan_shouldHaveCorrectLimits() {
        Tenant tenant = Tenant.builder().plan(Tenant.Plan.BUSINESS).build();

        Tenant.PlanLimits limits = tenant.getPlanLimits();

        assertThat(limits.memories()).isEqualTo(500_000L);
        assertThat(limits.tokens()).isEqualTo(10_000_000L);
        assertThat(limits.rateLimitPerMinute()).isEqualTo(2_000);
    }
}