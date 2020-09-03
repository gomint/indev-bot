module gomint.indevplugin {
    requires gomint.api;
    requires static lombok;
    requires discord4j.core;
    requires discord4j.common;
    requires reactor.core;
    requires org.slf4j;

    opens io.gomint.indev.config to gomint.api;
}
