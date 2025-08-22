package com.jmhreif.rag_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class RAGController {
    private final ChatClient chatClient;
    private final Neo4jVectorStore vectorStore;
    private final RAGRepository ragRepository;

    String prompt = """
            Please use the documents provided in the Context section to answer the question.
            
            Question: {question}
            
            Context:
            {context}
            """;

    public RAGController(ChatClient.Builder builder, Neo4jVectorStore vectorStore, RAGRepository ragRepository) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.ragRepository = ragRepository;
    }

    @GetMapping("/vector")
    public String vector(@RequestParam String question) {
        List<Document> results = vectorStore.similaritySearch(question);
        System.out.println("----- Vector RESULTS -----");
        System.out.println(results.stream().map(Document::toString).collect(Collectors.joining("\n")));

        var template = new PromptTemplate(prompt)
                .create(Map.of("question", question, "context", results));

        return chatClient.prompt(template).call().content();
    }

    //This method shows when vector retrieval is not a good fit
    @GetMapping("/vectorPlus")
    public void vectorPlus(@RequestParam String question) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query(question).build());
        List<Chunk> companies = ragRepository.getRelatedCompanies(results.stream().map(Document::getId).collect(Collectors.toList()));
        System.out.println("----- Related 4 COMPANIES -----");
        System.out.println(companies.stream().map(chunk -> chunk.metadata().toString()).collect(Collectors.joining("\n")));

        List<Document> moreResults = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(20).build());
        List<Chunk> moreCompanies = ragRepository.getRelatedCompanies(moreResults.stream().map(Document::getId).collect(Collectors.toList()));
        System.out.println("----- Related 20 COMPANIES -----");
        System.out.println(moreCompanies.stream().map(chunk -> chunk.metadata().toString()).collect(Collectors.joining("\n")));
    }

    @GetMapping("/graph")
    public String graph(@RequestParam String question) {
        List<Document> results = vectorStore.similaritySearch(question);

        List<Chunk> graphResults = ragRepository.getRAGDocuments(
                results.stream().map(Document::getId).collect(Collectors.toList())
        );
        System.out.println("----- Graph RESULTS -----");
        System.out.println(graphResults.stream().map(chunk -> chunk.metadata().toString()).collect(Collectors.joining("\n")));

        var template = new PromptTemplate(prompt)
                .create(Map.of("question", question,
                        "context", graphResults.stream().map(Chunk::toString).collect(Collectors.joining("\n"))));

        return chatClient.prompt(template).call().content();
    }
}
