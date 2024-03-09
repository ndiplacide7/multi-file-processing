package com.eng.placido.dev.multifileprocessing.config;

import com.eng.placido.dev.multifileprocessing.dto.VehicleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class ImportVehicleInvoicesJobConfig {

    @Value("${input.forder.vehicles}")
    private Resource[] resources;

    @Bean
    public Job importVehicleJob(JobRepository jobRepository, Step importVehicleStep) {
        return new JobBuilder("importVehicleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importVehicleStep)
                .build();
    }

    @Bean
    public Step importVehicleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("importVehicleStep", jobRepository)
                .<VehicleDTO, VehicleDTO>chunk(100, transactionManager)
                .reader(vehicleFlatItemReader())
                .processor(ImportVehicleInvoicesJobConfig::vehicleProcessor)
                .writer(items -> log.info("Writing {} items", items.size()))
                .build();
    }

    private static VehicleDTO vehicleProcessor(VehicleDTO item) {
        log.info("Processing item: {}", item);
        return item;
    }

    MultiResourceItemReader<VehicleDTO> vehicleMultiResourceItemReader() {
        return new MultiResourceItemReaderBuilder<VehicleDTO>()
                .name("Vehicle resource reader")
                .resources(resources)
                .delegate(vehicleFlatItemReader())
                .build();

    }

    public ResourceAwareItemReaderItemStream<VehicleDTO> vehicleFlatItemReader() {
        return new FlatFileItemReaderBuilder<VehicleDTO>()
                .name("vehicleItemReader")
                .resource(new ClassPathResource("data/tesla_invoices.csv"))
                .saveState(Boolean.FALSE)
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("referenceNumber", "model", "type", "customerFullName")
                .comments("#")
                .targetType(VehicleDTO.class)
                .build();
    }
}
