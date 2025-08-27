package com.jmhreif.rag_demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RAGTools {
    private final Neo4jVectorStore vectorStore;
    private final RAGRepository repo;

    public RAGTools(Neo4jVectorStore vectorStore, RAGRepository repo) {
        this.vectorStore = vectorStore;
        this.repo = repo;
    }

    @Tool(description = "Search for similar documents using vector similarity")
    public String vectorSearch(String query) {
        List<Document> results = vectorStore.similaritySearch(query);

        String formattedResults = results.stream()
                .map(Document::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Vector Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }

    @Tool(description = "Get enriched document chunks with company and risk information from the graph")
    public String graphEnrichedSearch(String query) {
        List<Document> vectorResults = vectorStore.similaritySearch(query);

        List<Chunk> graphResults = repo.getRAGDocuments(
                vectorResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        String formattedResults = graphResults.stream()
                .map(Chunk::toString)
                .collect(Collectors.joining("\n"));
        System.out.println("----- Graph Enriched Search Tool Results -----");
        System.out.println(formattedResults);

        return formattedResults;
    }

    @Tool(description = "Compare company results between small and large result sets")
    public String vectorPlusComparison(String query) {
        // Get small result set (default 4)
        List<Document> smallResults = vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(10).build()
        );
        List<Chunk> smallCompanies = repo.getRelatedCompanies(
            smallResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        // Get large result set (20)
        List<Document> largeResults = vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(20).build()
        );
        List<Chunk> largeCompanies = repo.getRelatedCompanies(
            largeResults.stream().map(Document::getId).collect(Collectors.toList())
        );

        // Return structured data for LLM to process
        return String.format("""
            QUERY: %s
            
            SMALL_SET_COUNT: %d
            SMALL_SET_COMPANIES: 
            %s
            
            LARGE_SET_COUNT: %d
            LARGE_SET_COMPANIES: 
            %s
            """, 
            query,
            smallCompanies.size(),
            smallCompanies.stream().map(chunk -> chunk.metadata().toString()).collect(Collectors.joining("\n")),
            largeCompanies.size(),
            largeCompanies.stream().map(chunk -> chunk.metadata().toString()).collect(Collectors.joining("\n"))
        );
    }
}
