package com.authorizationserver.scheduler.config;

import com.authorizationserver.scheduler.jobs.UpdateRsaKeyJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration class for Quartz beans
 *
 * @author Blajan George
 */
@Slf4j
@Configuration
public class QuartzConfig {
    /**
     * Fire of the trigger is delayed at start by this value ( in ms )
     */
    @Value("${quartz.updateKeysJob.startTriggerDelay:100000}")
    private Long updateKeysJobStartTriggerDelay;
    /**
     * Specify interval at which trigger fire ( in ms )
     */
    @Value("${quartz.updateKeysJob.triggerFireInterval:86400000}")
    private Long updateKeysJobTriggerFireInterval;

    /**
     * Specify if update keys job is enabled
     */
    @Value("${quartz.updateKeysJob.enabled:true}")
    private boolean updateKeysJobEnabled;
    /**
     * Job group name
     */
    private static final String JOB_GROUP = "authorization-server";
    /**
     * Job name
     */
    private static final String UPDATE_RSA_KEY_JOB_NAME = "update-rsa-key-job";
    /**
     * Trigger name
     */
    private static final String UPDATE_RSA_KEY_TRIGGER_NAME = "update-rsa-key-trigger";
    /**
     * Scheduler name
     */
    private static final String SCHEDULER_NAME = "authorization-server-scheduler";
    /**
     * Job description
     */
    private static final String UPDATE_RSA_KEY_JOB_DESCRIPTION = "Update private and public keys in db";

    /**
     * Job details factory bean
     *
     * @return {@link org.springframework.scheduling.quartz.JobDetailFactoryBean}
     */
    @Bean
    public JobDetailFactoryBean jobDetail() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setName(UPDATE_RSA_KEY_JOB_NAME);
        jobDetailFactory.setGroup(JOB_GROUP);
        jobDetailFactory.setDescription(UPDATE_RSA_KEY_JOB_DESCRIPTION);
        jobDetailFactory.setJobClass(UpdateRsaKeyJob.class);
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    /**
     * Simple trigger factory bean
     *
     * @param job {@link JobDetail}
     * @return {@link SimpleTriggerFactoryBean}
     */
    @Bean
    public SimpleTriggerFactoryBean trigger(JobDetail job) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setGroup(JOB_GROUP);
        trigger.setName(UPDATE_RSA_KEY_TRIGGER_NAME);
        trigger.setJobDetail(job);
        trigger.setStartDelay(updateKeysJobStartTriggerDelay);
        trigger.setRepeatInterval(updateKeysJobTriggerFireInterval);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);
        return trigger;
    }

    /**
     * Spring bean job factory to automatically populate a job's bean properties from the specified job data map and scheduler contex as stated in docs
     *
     * @return {@link SpringBeanJobFactory}
     */
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        return new SpringBeanJobFactory();
    }

    /**
     * Scheduler bean factory
     *
     * @param trigger          Registered trigger
     * @param job              Registered job
     * @param quartzDataSource Registered data source
     * @return {@link SchedulerFactoryBean}
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger trigger, JobDetail job, DataSource quartzDataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        Properties properties = new Properties();
        //set driver delegate class as PostgreSQL otherwise will use 'StdJDBCDelegate' and throw an error when fetch 'JobDetails' from db
        properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        //mark DataSource as clustered so among multiple schedulers on multiple nodes just one scheduler will execute a job at a time
        properties.put("org.quartz.jobStore.isClustered", "true");
        //bind instance name in DB with host machine name, 2 nodes with same instance name but different instance id are part of a cluster
        properties.put("org.quartz.scheduler.instanceId", "AUTO");

        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(job);
        schedulerFactory.setTriggers(trigger);
        schedulerFactory.setDataSource(quartzDataSource);
        schedulerFactory.setQuartzProperties(properties);
        schedulerFactory.setSchedulerName(SCHEDULER_NAME);

        return schedulerFactory;
    }

    /**
     * Scheduler bean
     *
     * @param schedulerFactoryBean scheduler factory bean
     * @param trigger              trigger
     * @return {@link Scheduler}
     * @throws SchedulerException scheduler exception
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean, Trigger trigger) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        if (!updateKeysJobEnabled) {
            log.debug("Will pause trigger {} because job {} is disabled.", UPDATE_RSA_KEY_TRIGGER_NAME, UPDATE_RSA_KEY_JOB_NAME);
            scheduler.pauseTrigger(trigger.getKey());
        }

        return scheduler;
    }
}
