package com.argusoft.path.tht.systemconfiguration.swagger;

import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * Swagger Configurations.
 *
 * @author Dhruv
 */
@Configuration
@EnableSwagger2
@PropertySource(value = "classpath:app-build-version.properties")
public class SwaggerConfig {

    private static final Contact DEFAULT_CONTACT = new Contact(
            "Argusoft India Ltd & Path", "https://www.path.org/", "digitalsquare@path.org");

    @Value("${info.app.version}")
    private String appVersion;

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("all-api")
                .apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.argusoft.path.tht"))
                .build()
                .ignoredParameterTypes(ContextInfo.class);
    }

    public ApiInfo apiInfo() {

        return new ApiInfo(
                "Testing Harness Tool API Documentation",//User App Info
                "Application programming Interface [ API ]  is at the core of enabling interoperability."
                        + " It’s a connective tissue that facilitates data sharing and enables digital THT Management experiences. "
                        + "It helps to expose the actions that the devloper needs.\n\n"
                        + "THT exposes the following APIs. "
                        + "It’s vital to know which information these APIs return.\n\n"
                        + "Detailed information on these APIs is as follows", //JSON REST API for MGS Backend
                appVersion,
                "Terms of service",
                DEFAULT_CONTACT,
                "License of API", "API license URL", Collections.emptyList());

    }

}
