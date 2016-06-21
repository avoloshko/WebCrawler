package com.webcrawler.web.app;

import com.codahale.metrics.health.HealthCheck;

/**
 * Just some stub code to notify that the server is always healthy.
 */
public class DummyHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}