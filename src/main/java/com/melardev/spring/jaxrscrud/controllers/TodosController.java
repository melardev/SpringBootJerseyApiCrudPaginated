package com.melardev.spring.jaxrscrud.controllers;


import com.melardev.spring.jaxrscrud.dtos.responses.*;
import com.melardev.spring.jaxrscrud.entities.Todo;
import com.melardev.spring.jaxrscrud.repositories.TodosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Path("/todos")
public class TodosController {


    @Context
    HttpServletRequest request;

    @Context
    private TodosRepository todosRepository;

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response index(@DefaultValue("1") @QueryParam("page") int page,
                          @DefaultValue("5") @QueryParam("page_size") int pageSize,
                          @Context HttpServletRequest request) {

        Pageable pageable = getPageable(page, pageSize);
        Page<Todo> todos = this.todosRepository.findAll(pageable);
        List<TodoSummaryDto> todoDtos = buildTodoDtos(todos);
        return Response.ok().entity(new TodoListResponse(PageMeta.build(todos, request.getRequestURI()), todoDtos))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        Optional<Todo> todo = this.todosRepository.findById(id);
        if (todo.isPresent())
            return Response.ok().entity(new TodoDetailsResponse(todo.get())).build();
        else
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse("Todo not found")).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getNotCompletedTodos(@DefaultValue("1") @QueryParam("page") int page,
                                         @DefaultValue("5") @QueryParam("page_size") int pageSize,
                                         @Context HttpServletRequest request) {

        Pageable pageable = getPageable(page - 1, pageSize);
        Page<Todo> todos = this.todosRepository.findByCompletedFalse(pageable);
        return Response.ok().entity(new TodoListResponse(PageMeta.build(todos, request.getRequestURI()), buildTodoDtos(todos))).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/completed")
    public Response getCompletedTodos(@DefaultValue("1") @QueryParam("page") int page,
                                      @DefaultValue("5") @QueryParam("page_size") int pageSize) {

        Page<Todo> todosPage = todosRepository.findByCompletedIsTrue(getPageable(page, pageSize));
        return Response.ok().entity(new TodoListResponse(PageMeta.build(todosPage, request.getRequestURI()), buildTodoDtos(todosPage))).build();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response create(Todo todo) {
        return Response.status(Response.Status.CREATED)
                .entity(new TodoDetailsResponse(todosRepository.save(todo), "Todo created successfully"))
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Todo todoInput) {
        Optional<Todo> optionalTodo = todosRepository.findById(id);
        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();
            todo.setTitle(todoInput.getTitle());
            todo.setDescription(todoInput.getDescription());
            todo.setCompleted(todoInput.isCompleted());
            return Response.ok()
                    .entity(new TodoDetailsResponse(todosRepository.save(optionalTodo.get()), "Todo updated successfully"))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Todo does not exist"))
                    .build();
        }
    }


    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Optional<Todo> todo = todosRepository.findById(id);
        if (todo.isPresent()) {
            todosRepository.delete(todo.get());
            // If you use NO_CONTENT, the response body will be empty
            return Response.status(Response.Status.OK).entity(new SuccessResponse("You have successfully deleted the article")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("This todo does not exist"))
                    .build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAll() {
        todosRepository.deleteAll();
        return Response.ok().entity(new SuccessResponse("Deleted all todos successfully")).build();
    }

    private Pageable getPageable(int page, int pageSize) {
        if (page <= 0)
            page = 1;

        if (pageSize <= 0)
            pageSize = 5;

        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.Direction.DESC, "createdAt");
        return pageRequest;
    }

    private List<TodoSummaryDto> buildTodoDtos(Page<Todo> todos) {
        List<TodoSummaryDto> todoDtos = todos.getContent().stream().map(TodoSummaryDto::build).collect(Collectors.toList());
        return todoDtos;
    }
}
