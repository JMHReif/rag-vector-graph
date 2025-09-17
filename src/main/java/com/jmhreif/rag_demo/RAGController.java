package com.jmhreif.rag_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RAGController {
    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpProvider;

    public RAGController(ChatClient.Builder builder, SyncMcpToolCallbackProvider provider, RAGTools ragTools) {
        this.chatClient = builder
                .defaultToolCallbacks(provider.getToolCallbacks())
                .defaultTools(ragTools)
                .build();
        this.mcpProvider = provider;
    }

    @GetMapping("/vector")
    public String vector(@RequestParam String question) {
        String vectorPrompt = """
            Answer this question using vector search: %s
            
            Use the vectorSearch tool to find relevant documents.
            """.formatted(question);

        return chatClient.prompt()
                .user(vectorPrompt)
                .call()
                .content();
    }

    //This method shows when vector retrieval is not a good fit
    @GetMapping("/vectorPlus")
    public String vectorPlus(@RequestParam String question) {
        String vectorPlusPrompt = """
            Use the vectorPlusComparison tool to get data comparing small vs large vector search result sets for: %s
            
            Please only output the results.
            """.formatted(question);

        return chatClient.prompt()
                .user(vectorPlusPrompt)
                .call()
                .content();
    }

    @GetMapping("/graph")
    public String graph(@RequestParam String question) {
        String graphPrompt = """
            Answer this question using graph-enriched search: %s
            
            Use the graphEnrichedSearch tool to find relevant documents with company and risk information.
            """.formatted(question);

        return chatClient.prompt()
                .user(graphPrompt)
                .call()
                .content();
    }

    @GetMapping("/debug/tools")
    public String debugTools() {
        var callbacks = mcpProvider.getToolCallbacks();
        StringBuilder sb = new StringBuilder("Available MCP Tools:\n");
        for (var callback : callbacks) {
            sb.append("- ").append(callback.getToolDefinition().name()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/text2cypher")
    public String text2cypher(@RequestParam String question) {
        String cypherPrompt = """
            Generate a Cypher query for this question: %s
            
            First call get_neo4j_schema, then generate and execute a Cypher query using read_neo4j_cypher.
            Show the Cypher query you're executing before showing results.
            """.formatted(question);

        return chatClient.prompt()
                .user(cypherPrompt)
                .call()
                .content();
    }
}
