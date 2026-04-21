package com.ft.batch.application;

import java.time.YearMonth;

public interface BatchJobExecutor {

    void execute(YearMonth yearMonth);
}
