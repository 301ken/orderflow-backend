package com.example.orderflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderflowFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private long createProduct(String token, String name, double price) throws Exception {
        String body = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"price\":" + price + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createClient(String token) throws Exception {
        String body = mockMvc.perform(post("/clients")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\",\"email\":\"acme@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createOrder(String token) throws Exception {
        long clientId = createClient(token);
        long productId = createProduct(token, "Order Product " + System.nanoTime(), 25.0);
        String payload = "{\"client\":{\"id\":" + clientId + "},"
                + "\"items\":[{\"quantity\":2,\"product\":{\"id\":" + productId + "}}]}";
        String body = mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void userCannotDeleteButAdminCan() throws Exception {
        String admin = login("admin", "admin123");
        String user = login("user", "user123");
        long productId = createProduct(admin, "Deletable " + System.nanoTime(), 5.0);

        mockMvc.perform(delete("/products/" + productId).header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/products/" + productId).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void auditLogIsAdminOnly() throws Exception {
        String user = login("user", "user123");
        String admin = login("admin", "admin123");

        mockMvc.perform(get("/audit-logs").header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/audit-logs").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void paymentFlowMovesOrderThroughStates() throws Exception {
        String admin = login("admin", "admin123");
        long orderId = createOrder(admin);

        mockMvc.perform(post("/orders/" + orderId + "/pay").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("noop"));

        mockMvc.perform(get("/orders/" + orderId).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + orderId + ",\"outcome\":\"SUCCEEDED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/orders/" + orderId).header("Authorization", "Bearer " + admin))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void illegalStatusTransitionReturnsBadRequest() throws Exception {
        String admin = login("admin", "admin123");
        long orderId = createOrder(admin);

        mockMvc.perform(patch("/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void firebaseLoginIssuesUsableToken() throws Exception {
        String body = mockMvc.perform(post("/auth/firebase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"uid-123:firebase@example.com\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();

        mockMvc.perform(get("/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void documentUploadAndListWithLocalStorage() throws Exception {
        String admin = login("admin", "admin123");
        long orderId = createOrder(admin);

        MockMultipartFile file = new MockMultipartFile(
                "file", "invoice.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/orders/" + orderId + "/documents")
                        .file(file)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("invoice.txt"));

        mockMvc.perform(get("/orders/" + orderId + "/documents").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("invoice.txt"));
    }
}
