package dev.davivieira.topologyinventory.application.ports.input;

import dev.davivieira.topologyinventory.application.ports.output.RouterManagementOutputPort;
import dev.davivieira.topologyinventory.application.usecases.NetworkManagementUseCase;
import dev.davivieira.topologyinventory.domain.entity.EdgeRouter;
import dev.davivieira.topologyinventory.domain.entity.Switch;
import dev.davivieira.topologyinventory.domain.service.NetworkService;
import dev.davivieira.topologyinventory.domain.vo.IP;
import dev.davivieira.topologyinventory.domain.vo.Id;
import dev.davivieira.topologyinventory.domain.vo.Network;

import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class NetworkManagementInputPort implements NetworkManagementUseCase {

    @Inject
    Instance<RouterManagementOutputPort> routerManagementOutputPort;

    @Override
    public Network createNetwork(
            IP networkAddress, String networkName, int networkCidr) {
        return Network
                .builder()
                .networkAddress(networkAddress)
                .networkName(networkName)
                .networkCidr(networkCidr)
                .build();
    }

    @Override
    public Switch addNetworkToSwitch(Network network, Switch networkSwitch) {
        Id routerId = networkSwitch.getRouterId();
        Id switchId = networkSwitch.getId();
        EdgeRouter edgeRouter = (EdgeRouter) routerManagementOutputPort.get()
                .retrieveRouter(routerId);
        Switch switchToAddNetwork = edgeRouter
                .getSwitches()
                .get(switchId);
        switchToAddNetwork.addNetworkToSwitch(network);
        routerManagementOutputPort.get().persistRouter(edgeRouter);
        return switchToAddNetwork;
    }

    @Override
    public Switch removeNetworkFromSwitch(String networkName, Switch networkSwitch) {
        Id routerId = networkSwitch.getRouterId();
        Id switchId = networkSwitch.getId();
        EdgeRouter edgeRouter = (EdgeRouter) routerManagementOutputPort.get()
                .retrieveRouter(routerId);
        Switch switchToRemoveNetwork = edgeRouter
                .getSwitches()
                .get(switchId);
        Predicate<Network> networkPredicate = Network.getNetworkNamePredicate(networkName);
        var network = NetworkService.
                findNetwork(switchToRemoveNetwork.getSwitchNetworks(), networkPredicate);
        routerManagementOutputPort.get().persistRouter(edgeRouter);
        return switchToRemoveNetwork.removeNetworkFromSwitch(network)
                ? switchToRemoveNetwork
                : null;
    }
}
