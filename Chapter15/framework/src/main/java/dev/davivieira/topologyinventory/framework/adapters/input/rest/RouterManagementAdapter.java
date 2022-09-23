package dev.davivieira.topologyinventory.framework.adapters.input.rest;

import dev.davivieira.topologyinventory.application.usecases.RouterManagementUseCase;
import dev.davivieira.topologyinventory.domain.entity.CoreRouter;
import dev.davivieira.topologyinventory.domain.entity.Router;
import dev.davivieira.topologyinventory.domain.vo.IP;
import dev.davivieira.topologyinventory.domain.vo.Id;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import dev.davivieira.topologyinventory.framework.adapters.input.rest.request.router.CreateRouter;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/router")
@Tag(name = "Router Operations", description = "Router management operations")
public class RouterManagementAdapter {

    @Inject
    Instance<RouterManagementUseCase> routerManagementUseCase;

    @Transactional
    @GET
    @Path("/{id}")
    @Operation(operationId = "retrieveRouter", description = "Retrieve a router from the network inventory")
    public Uni<Response> retrieveRouter(@PathParam("id") Id id) {
        return Uni.createFrom()
                .item(routerManagementUseCase.get().retrieveRouter(id))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @DELETE
    @Path("/{id}")
    @Operation(operationId = "removeRouter", description = "Remove a router from the network inventory")
    public Uni<Response> removeRouter(@PathParam("id") Id id) {
        return Uni.createFrom()
                .item(routerManagementUseCase.get().removeRouter(id))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @POST
    @Path("/")
    @Operation(operationId = "createRouter", description = "Create and persist a new router on the network inventory")
    public Uni<Response> createRouter(CreateRouter createRouter) {
        var router = routerManagementUseCase.get().createRouter(
                null,
                createRouter.getVendor(),
                createRouter.getModel(),
                IP.fromAddress(createRouter.getIp()),
                createRouter.getLocation(),
                createRouter.getRouterType()

        );

        return Uni.createFrom()
                .item(routerManagementUseCase.get().persistRouter(router))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @POST
    @Path("/{routerId}/to/{coreRouterId}")
    @Operation(operationId = "addRouterToCoreRouter", description = "Add a router into a core router")
    public Uni<Response> addRouterToCoreRouter(
            @PathParam("routerId") String routerId, @PathParam("coreRouterId") String coreRouterId) {
        Router router = routerManagementUseCase.get()
                .retrieveRouter(Id.withId(routerId));
        CoreRouter coreRouter = (CoreRouter) routerManagementUseCase.get()
                .retrieveRouter(Id.withId(coreRouterId));

        return Uni.createFrom()
                .item(routerManagementUseCase.get()
                        .addRouterToCoreRouter(router, coreRouter))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @DELETE
    @Path("/{routerId}/from/{coreRouterId}")
    @Operation(operationId = "removeRouterFromCoreRouter", description = "Remove a router from a core router")
    public Uni<Response> removeRouterFromCoreRouter(
            @PathParam("routerId") String routerId, @PathParam("coreRouterId") String coreRouterId) {
        Router router = routerManagementUseCase.get()
                .retrieveRouter(Id.withId(routerId));
        CoreRouter coreRouter = (CoreRouter) routerManagementUseCase.get()
                .retrieveRouter(Id.withId(coreRouterId));

        return Uni.createFrom()
                .item(routerManagementUseCase.get()
                        .removeRouterFromCoreRouter(router, coreRouter))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }
}
