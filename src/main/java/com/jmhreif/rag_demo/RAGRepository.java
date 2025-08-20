package com.jmhreif.rag_demo;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface RAGRepository extends Neo4jRepository<Chunk, String> {
    @Query("MATCH (node:Chunk)-[:FROM_DOCUMENT]-(doc:Document)-[:FILED]-(company:Company) " +
        " WHERE node.id IN $documentIds " +
        "RETURN node{ " +
        "     .id, .text, " +
        "     metadata: { " +
        "         company: company.name, " +
        "         risks: [ (company)-[:FACES_RISK]->(risk:RiskFactor) | risk.name ] " +
        "     } " +
        "};")
    List<Chunk> getRAGDocuments(List<String> documentIds);

    //This method only for /vectorPlus endpoint demo purpose
    @Query("MATCH (node:Chunk)-[:FROM_DOCUMENT]-(doc:Document)-[:FILED]-(company:Company) " +
            " WHERE node.id IN $documentIds " +
            "RETURN node{ " +
            "   .id, .text, " +
            "   metadata: { company: company.name } " +
            "};")
    List<Chunk> getRelatedCompanies(List<String> documentIds);
}
