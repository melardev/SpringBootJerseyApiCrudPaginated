package com.melardev.spring.jaxrscrud.config;

import com.melardev.spring.jaxrscrud.controllers.TodosController;
import com.melardev.spring.jaxrscrud.filters.AppCorsFilter;
import com.melardev.spring.jaxrscrud.repositories.TodosRepository;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(TodosController.class);
        register(AppCorsFilter.class);
        register(TodosRepository.class);
    }

}
