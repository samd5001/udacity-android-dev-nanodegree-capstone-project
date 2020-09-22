package com.sdunk.jiraestimator.data.db;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DBExecutor {

    private static final Object LOCK = new Object();
    private static DBExecutor instance;
    private final Executor diskIO;

    private DBExecutor(Executor diskIO) {
        this.diskIO = diskIO;

    }

    public static DBExecutor getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                instance = new DBExecutor(Executors.newSingleThreadExecutor());
            }
        }
        return instance;
    }

    public Executor diskIO() {
        return diskIO;
    }
}
