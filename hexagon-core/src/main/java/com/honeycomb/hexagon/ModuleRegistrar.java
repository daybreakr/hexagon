package com.honeycomb.hexagon;

import com.honeycomb.hexagon.core.ControllerInfo;
import com.honeycomb.hexagon.core.ModuleInfo;
import com.honeycomb.hexagon.core.ServiceInfo;
import com.honeycomb.hexagon.register.ControllerRegistration;
import com.honeycomb.hexagon.register.ModuleList;
import com.honeycomb.hexagon.register.ModuleRegistration;
import com.honeycomb.hexagon.register.ServiceRegistration;

import java.util.LinkedHashSet;
import java.util.Set;

public class ModuleRegistrar extends HexagonComponent {

    public void register(ModuleList moduleList) {
        if (assembly() == null) {
            throw new IllegalStateException("Engine not attached.");
        }

        for (ModuleRegistration module : moduleList.modules()) {

            // Register controllers.
            Set<String> registeredControllers = new LinkedHashSet<>();
            for (ControllerRegistration<?> controller : module.controllers()) {
                registerController(controller, module, registeredControllers);
            }

            // Register services.
            Set<String> registeredServices = new LinkedHashSet<>();
            for (ServiceRegistration<?> service : module.services()) {
                registerService(service, module, registeredServices);
            }

            // Register module.
            registerModule(module, registeredControllers, registeredServices);
        }
    }

    private void registerController(ControllerRegistration<?> controller,
                                    ModuleRegistration module,
                                    Set<String> outRegisteredControllers) {
        final String name = controller.apiClass().getName();
        final boolean enabled = module.enabled() && controller.enabled();

        ControllerInfo info = new ControllerInfo(name, module.name());
        info.label(controller.label());
        info.enabled(enabled);
        info.prerequisites(controller.prerequisites());
        info.dependencies(controller.dependencies());
        info.resolved(false);

        assembly().controllerRegistry().register(info, controller.provider());

        outRegisteredControllers.add(name);
    }

    private void registerService(ServiceRegistration<?> service,
                                 ModuleRegistration module,
                                 Set<String> outRegisteredServices) {
        final String name = service.serviceClass().getName();
        final boolean enabled = module.enabled() && service.enabled();

        ServiceInfo info = new ServiceInfo(name, module.name());
        info.label(service.label());
        info.enabled(enabled);
        info.prerequisites(service.prerequisites());
        info.dependencies(service.dependencies());
        info.categorise(service.categories());
        info.resolved(false);

        assembly().serviceRegistry().register(info, service.provider());

        outRegisteredServices.add(name);
    }

    private void registerModule(ModuleRegistration module,
                                Set<String> registeredControllers,
                                Set<String> registeredServices) {
        ModuleInfo info = new ModuleInfo(module.name());
        info.label(module.label());
        info.enabled(module.enabled());
        info.prerequisites(module.prerequisites());
        info.dependencies(module.dependencies());
        info.controllers(registeredControllers);
        info.services(registeredServices);
        info.resolved(false);

        assembly().moduleRegistry().register(info);
    }
}
