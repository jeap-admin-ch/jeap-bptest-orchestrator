package ch.admin.bit.jeap.testorchestrator.adapter.testagent;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@AutoConfiguration
@ComponentScan
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private OrchestratorAsyncExceptionHandler orchestratorAsyncExceptionHandler;

    public Executor getAsyncExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Autowired
    public void setOrchestratorAsyncExceptionHandler(OrchestratorAsyncExceptionHandler orchestratorAsyncExceptionHandler) {
        this.orchestratorAsyncExceptionHandler = orchestratorAsyncExceptionHandler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return this.orchestratorAsyncExceptionHandler;
    }

}
