package com.sdunk.jiraestimator.api;

import androidx.annotation.Nullable;
import androidx.test.espresso.IdlingResource;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class APIIdlingResource implements IdlingResource {

    private final AtomicBoolean isIdle = new AtomicBoolean(true);

    @Nullable
    private volatile IdlingResource.ResourceCallback callback;

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }

    void setIdleState() {
        isIdle.set(true);
        if (this.callback != null) {
            Objects.requireNonNull(callback).onTransitionToIdle();
        }
    }
}