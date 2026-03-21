package com.custodyreport.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve path to the project root relative to the running engine/core-java
        String projectRoot = Paths.get("").toAbsolutePath().getParent().getParent().toString();
        
        // 1. Map /apps/** to the root apps/ directory
        String appsPath = new File(projectRoot, "apps").getAbsolutePath();
        registry.addResourceHandler("/apps/**")
                .addResourceLocations("file:" + appsPath + File.separator);

        // 2. Map /admin/** to the engine/admin-dashboard/ directory
        String adminPath = new File(projectRoot, "engine/admin-dashboard").getAbsolutePath();
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:" + adminPath + File.separator);
                
        // Keep standard static resource handling as well (for files in src/main/resources/static)
    }
}
