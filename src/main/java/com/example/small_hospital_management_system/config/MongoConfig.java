package com.example.small_hospital_management_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Turns on auditing so fields marked {@code @CreatedDate} are filled in automatically on insert. */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
