package edu.yandex.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

@Configuration
public class RouterConfiguration {

    // forward на уровне конфигурации Webflux не поддерживатеся, а реализация целого контроллера под редирект мне видится излишней
    @Bean
    public RouterFunction<ServerResponse> userHandlerRouterFunction() {
        return RouterFunctions.route()
                .GET("/", request -> ServerResponse.permanentRedirect(URI.create("/items")).build())
                .build();
    }
}
