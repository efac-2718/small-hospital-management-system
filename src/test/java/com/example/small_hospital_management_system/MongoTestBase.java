package com.example.small_hospital_management_system;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;

/**
 * Starts an in-memory, MongoDB-compatible server (mongo-java-server) once for all tests and points
 * Spring Boot at it. The tests therefore run without installing MongoDB.
 * The real application still connects to the MongoDB set in application.properties.
 */
public abstract class MongoTestBase {

    private static final MongoServer SERVER =
            new MongoServer(new MemoryBackend().version(ServerVersion.MONGO_5_0));
    private static final String URI;

    static {
        InetSocketAddress address = SERVER.bind();
        URI = "mongodb://localhost:" + address.getPort() + "/hospital_test";
        Runtime.getRuntime().addShutdownHook(new Thread(SERVER::shutdownNow));
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", () -> URI);
        registry.add("spring.devtools.restart.enabled", () -> "false");
    }
}
