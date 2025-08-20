package com.jmhreif.rag_demo;

import jakarta.annotation.Nullable;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Map;

@Node
public record Chunk(@Id String id,
                    String text,
                    Map<String, Object> metadata,
                    @Nullable Double score) {
}