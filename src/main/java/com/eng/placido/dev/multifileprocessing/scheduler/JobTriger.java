package com.eng.placido.dev.multifileprocessing.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobTriger {

    private final JobLauncher jobLauncher;
    private final Job job;

    @SneakyThrows
    @Scheduled(cron = "0/10 * * ? * *")
    void launchJobPeriodically() {
        log.info("============================Starting job============="+new Date());

        var jobParameters = new JobParametersBuilder();
        jobParameters.addDate("uniqueness", new Date());
        JobExecution jobExecution = this.jobLauncher.run(job, jobParameters.toJobParameters());

        log.info("============================Ending job============="+new Date());
    }

}
