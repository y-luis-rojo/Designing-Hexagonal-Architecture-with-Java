package dev.davivieira.topologyinventory.framework.adapters.output.mysql;

import dev.davivieira.topologyinventory.application.ports.output.RouterManagementOutputPort;
import dev.davivieira.topologyinventory.domain.entity.Router;
import dev.davivieira.topologyinventory.domain.vo.Id;
import dev.davivieira.topologyinventory.framework.adapters.output.mysql.mappers.RouterH2Mapper;
import dev.davivieira.topologyinventory.framework.adapters.output.mysql.repository.RouterManagementRepository;

import io.quarkus.hibernate.reactive.panache.Panache;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class RouterManagementMySQLAdapter implements RouterManagementOutputPort {

    @Inject
    Instance<RouterManagementRepository> routerManagementRepository;

    @Override
    public Router retrieveRouter(Id id) {
        var routerData = routerManagementRepository.get().findById(id.getUuid()).subscribe().asCompletionStage().join();
        return RouterH2Mapper.routerDataToDomain(routerData);
    }

    @Override
    public Router removeRouter(Id id) {
        var removed = routerManagementRepository.get().deleteById(id.getUuid()).subscribe().asCompletionStage().join();
        if (!removed) {
            throw new InternalError();
        }
        return null;
    }

    @Override
    public Router persistRouter(Router router) {
        var routerData = RouterH2Mapper.routerDomainToData(router);
        Panache.withTransaction(() -> routerManagementRepository.get().persist(routerData));
        return router;
    }
}
