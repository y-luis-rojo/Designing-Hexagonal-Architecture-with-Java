package dev.davivieira.topologyinventory.framework.adapters.input.rest;

import dev.davivieira.topologyinventory.application.usecases.RouterManagementUseCase;
import dev.davivieira.topologyinventory.application.usecases.SwitchManagementUseCase;
import dev.davivieira.topologyinventory.domain.entity.EdgeRouter;
import dev.davivieira.topologyinventory.domain.entity.Router;
import dev.davivieira.topologyinventory.domain.entity.Switch;
import dev.davivieira.topologyinventory.domain.vo.IP;
import dev.davivieira.topologyinventory.domain.vo.Id;
import dev.davivieira.topologyinventory.domain.vo.RouterType;
import dev.davivieira.topologyinventory.framework.adapters.input.rest.request.aswitch.CreateSwitch;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
@Path("/switch")
@Tag(name = "Switch Operations", description = "Operations for switch management")
public class SwitchManagementAdapter {

    @Inject
    Instance<SwitchManagementUseCase> switchManagementUseCase;
    @Inject
    Instance<RouterManagementUseCase> routerManagementUseCase;

    @Transactional
    @GET
    @Path("/{id}")
    @Operation(operationId = "retrieveSwitch", description = "Retrieve a switch from an edge router")
    public Uni<Response> retrieveSwitch(@PathParam("id") Id switchId) {
        return Uni.createFrom()
                .item(switchManagementUseCase.get().retrieveSwitch(switchId))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @POST
    @Path("/create/{edgeRouterId}")
    @Operation(operationId = "createAndAddSwitchToEdgeRouter", description = "Create switch and add to an edge router")
    public Uni<Response> createAndAddSwitchToEdgeRouter(
            CreateSwitch createSwitch, @PathParam("edgeRouterId") String edgeRouterId
    ) {
        Switch newSwitch = switchManagementUseCase.get()
                .createSwitch(
                        createSwitch.getVendor(),
                        createSwitch.getModel(),
                        IP.fromAddress(createSwitch.getIp()),
                        createSwitch.getLocation(),
                        createSwitch.getSwitchType());
        Router edgeRouter = routerManagementUseCase.get().retrieveRouter(Id.withId(edgeRouterId));
        if (!edgeRouter.getRouterType().equals(RouterType.EDGE))
            throw new UnsupportedOperationException("Please inform the id of an edge router to add a switch");
        Router router = switchManagementUseCase.get().addSwitchToEdgeRouter(newSwitch, (EdgeRouter) edgeRouter);

        return Uni.createFrom()
                .item((EdgeRouter) routerManagementUseCase.get().persistRouter(router))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }

    @Transactional
    @DELETE
    @Path("/{switchId}/from/{edgeRouterId}")
    @Operation(operationId = "removeSwitch", description = "Retrieve a router from the network inventory")
    public Uni<Response> removeSwitchFromEdgeRouter(
            @PathParam("switchId") String switchId, @PathParam("edgeRouterId") String edgeRouterId) {
        EdgeRouter edgeRouter = (EdgeRouter) routerManagementUseCase.get()
                .retrieveRouter(Id.withId(edgeRouterId));
        Switch networkSwitch = switchManagementUseCase.get().retrieveSwitch(Id.withId(switchId));

        Router router = switchManagementUseCase.get()
                .removeSwitchFromEdgeRouter(networkSwitch, edgeRouter);

        return Uni.createFrom()
                .item((EdgeRouter) routerManagementUseCase.get().persistRouter(router))
                .onItem()
                .transform(f -> f != null ? Response.ok(f) : Response.ok(null))
                .onItem()
                .transform(Response.ResponseBuilder::build);
    }
}
