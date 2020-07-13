module gomint.indevplugin {
    requires gomint.api;
    requires lombok;
    requires transitive slf4j.api;
    requires discord4j.core;
    requires discord4j.common;
    requires reactor.core;

    opens io.gomint.indev.config to gomint.api;
}
