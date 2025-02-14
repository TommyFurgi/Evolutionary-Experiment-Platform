package com.endpointexplorers.cli.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

public class PathModule extends AbstractModule {
    @Provides
    @Named("cliPlotsResources")
    public String provideCliPlotsResourcesPath() {
        return "cli/clientResources/plots";
    }

    @Provides
    @Named("cliCSVsResources")
    public String provideCliCSVsResourcesPath() {
        return "cli/clientResources/csv";
    }

    @Provides
    @Named("cliOthersResources")
    public String provideCliOthersResourcesPath() {
        return "cli/clientResources/others";
    }
}
