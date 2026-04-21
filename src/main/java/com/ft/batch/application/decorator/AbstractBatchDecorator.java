package com.ft.batch.application.decorator;

import com.ft.batch.application.BatchJobExecutor;

import java.time.YearMonth;

public abstract class AbstractBatchDecorator implements BatchJobExecutor {
    protected final BatchJobExecutor delegate;

    protected AbstractBatchDecorator(BatchJobExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public abstract void execute(YearMonth yearMonth);
}
