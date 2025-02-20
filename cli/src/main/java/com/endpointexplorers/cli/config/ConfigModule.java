package com.endpointexplorers.cli.config;

import com.endpointexplorers.cli.component.FilesSaver;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class ConfigModule extends AbstractModule {

    @Provides
    @Singleton
    @Named("baseUrl")
    public String provideBaseUrl() {
        return System.getProperty("base.url", "http://localhost:8080/");
    }

    @Provides
    @Singleton
    @Named("runExperimentsUrl")
    public String provideRunExperimentsUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments";
    }

    @Provides
    @Singleton
    @Named("getExperimentUrl")
    public String provideGetExperimentUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/";
    }

    @Provides
    @Singleton
    @Named("getCompletedExperimentsUrl")
    public String provideGetCompletedExperimentsUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/completed";
    }

    @Provides
    @Singleton
    @Named("getExperimentListUrl")
    public String provideExperimentListUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiment-list";
    }

    @Provides
    @Singleton
    @Named("getStatsUrl")
    public String provideStatsUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "stats";
    }

    @Provides
    @Singleton
    @Named("runMultipleExperimentsUrl")
    public String provideRunMultipleExperimentsUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/multi";
    }

    @Provides
    @Singleton
    @Named("setGroupNameUrl")
    public String provideSetGroupNameUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/group";
    }

    @Provides
    @Singleton
    @Named("deleteExperimentGroupUrl")
    public String provideDeleteExperimentGroupUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/group/";
    }

    @Provides
    @Singleton
    @Named("deleteExperimentIdUrl")
    public String provideDeleteExperimentIdUrl(@Named("baseUrl") String baseUrl) {
        return baseUrl + "experiments/";
    }

    @Provides
    @Singleton
    public FilesSaver provideFilesSaver(
            @Named("cliPlotsResources") String cliPlotsResourcesPath,
            @Named("cliCSVsResources") String cliCSVsResourcesPath,
            @Named("cliOthersResources") String cliOthersResourcesPath
    ) {
        return new FilesSaver(cliPlotsResourcesPath, cliCSVsResourcesPath, cliOthersResourcesPath);
    }
}