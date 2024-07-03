package it.gov.pagopa.payment.notices.service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import it.gov.pagopa.payment.notices.service.util.Constants;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static it.gov.pagopa.payment.notices.service.util.Constants.HEADER_REQUEST_ID;

@Configuration
public class OpenApiConfig {

    public static final String INTERNAL_PATH = "/print-payment-notice-service/internal/v1";

    public static final String EXTERNAL_PATH = "/print-payment-notice-service/external/v1";

    public static final String LOCAL_PATH = "http://localhost:8080";
    public static final String APIM_DEV = "https://api.dev.platform.pagopa.it";
    public static final String APIM_UAT = "https://api.uat.platform.pagopa.it";
    public static final String APIM_PROD = "https://api.platform.pagopa.it";


    @Bean
    OpenAPI customOpenAPI(
            @Value("${info.application.name}") String appName,
            @Value("${info.application.description}") String appDescription,
            @Value("${info.application.version}") String appVersion) {
        return new OpenAPI()
                .servers(List.of(new Server().url(LOCAL_PATH),
                        new Server().url("{host}{basePath}")
                                .variables(new ServerVariables()
                                        .addServerVariable("host",
                                                new ServerVariable()._enum(
                                                        List.of(APIM_DEV, APIM_UAT, APIM_PROD))
                                                        ._default(APIM_PROD))
                                        .addServerVariable("basePath",
                                                new ServerVariable()._enum(List.of(INTERNAL_PATH))._default(INTERNAL_PATH))
                                )))
                .components(new Components().addSecuritySchemes("ApiKey",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .description("The Azure Subscription Key to access this API.")
                                .name("Ocp-Apim-Subscription-Key")
                                .in(SecurityScheme.In.HEADER)))
                .info(new Info()
                        .title(appName)
                        .version(appVersion)
                        .description(appDescription)
                        .termsOfService("https://www.pagopa.gov.it/"));
    }

    @Bean
    public GlobalOpenApiCustomizer sortOperationsAlphabetically() {
        return openApi -> {
            Paths paths =
                    openApi
                            .getPaths()
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Paths::new,
                                    (map, item) -> map.addPathItem(item.getKey(), item.getValue()),
                                    Paths::putAll);

            paths.forEach((key, value) -> value.readOperations()
                    .forEach(operation -> {
                        var responses = operation.getResponses()
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(ApiResponses::new,
                                        (map, item) -> map.addApiResponse(item.getKey(), item.getValue()),
                                        ApiResponses::putAll);
                        operation.setResponses(responses);
                    }));
            openApi.setPaths(paths);
        };
    }

    @Bean
    public GlobalOpenApiCustomizer addCommonHeaders() {
        return openApi ->
                openApi.getPaths()
                        .forEach((key, value) -> {
                            // add Request-ID as request header
                            var header = Optional.ofNullable(value.getParameters())
                                    .orElse(Collections.emptyList())
                                    .parallelStream()
                                    .filter(Objects::nonNull)
                                    .anyMatch(elem -> Constants.HEADER_REQUEST_ID.equals(elem.getName()));
                            if(!header) {
                                value.addParametersItem(
                                        new Parameter()
                                                .in("header")
                                                .name(Constants.HEADER_REQUEST_ID)
                                                .schema(new StringSchema())
                                                .description(
                                                        "This header identifies the call, if not passed it is self-generated." +
                                                                " This ID is returned in the response."
                                                ));
                            }

                            // add Request-ID as response header
                            value.readOperations().forEach(operation -> operation.getResponses()
                                    .values()
                                    .forEach(response -> response.addHeaderObject(
                                            Constants.HEADER_REQUEST_ID,
                                            new Header().schema(new StringSchema())
                                                    .description("This header identifies the call"))));
                        });
    }

    @Bean
    public Map<String, GroupedOpenApi> configureGroupedOpenApi(Map<String, GroupedOpenApi> groupedsOpenApi) {
        groupedsOpenApi.forEach((id, groupedOpenApi) -> groupedOpenApi.getOpenApiCustomizers()
                .add(openApi -> {
                    var baseTitle = openApi.getInfo().getTitle();
                    var group = groupedOpenApi.getDisplayName();
                    openApi.getInfo().setTitle(baseTitle + " - " + group);
                    if("external".equals(id)) {
                        openApi.setServers(Collections.singletonList(new Server().url(APIM_PROD + EXTERNAL_PATH)));
                        openApi.getPaths().forEach((key, value) -> {
                          if (value.getPost() != null && value.getPost().getParameters() != null) {
                              value.getPost().getParameters().removeIf(parameter ->
                                      Constants.X_USER_ID.equals(parameter.getName()));
                          } else if(value.getGet() != null && value.getGet().getParameters() != null) {
                              value.getGet().getParameters().removeIf(parameter ->
                                      Constants.X_USER_ID.equals(parameter.getName()));
                          } else if(value.getDelete() != null && value.getDelete().getParameters() != null) {
                              value.getDelete().getParameters().removeIf(parameter ->
                                      Constants.X_USER_ID.equals(parameter.getName()));
                          }
                        });
                    }
                }));
        return groupedsOpenApi;
    }

}
