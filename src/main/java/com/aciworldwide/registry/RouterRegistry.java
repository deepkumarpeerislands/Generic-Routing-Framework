package com.aciworldwide.registry;

import io.vertx.ext.web.Router;

/**
 * Package-private in-process registry for Router instance.
 *
 * We keep this simple and JVM-local. The event bus is used to publish readiness; the registry
 * provides direct object reference retrieval without serialization.
 *
 * NOTE: This is intentionally package-private and simple for the sample. For production you may
 * want a proper service interface or use Vert.x service proxies.
 */
public class RouterRegistry {
    private static volatile Router router;

    public static void setRouter(Router r) {
        router = r;
    }

    public static Router getRouter() {
        return router;
    }
}
