package com.jobtracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_createProgressFilterAndStats() throws Exception {
        String auth = "Bearer " + registerAndGetToken("flow@example.com");
        long id = createApplication(auth, "Acme Corp");

        mockMvc.perform(patch("/api/applications/" + id + "/status")
                        .header("Authorization", auth)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"SCREENING\",\"note\":\"Recruiter call\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SCREENING"))
                .andExpect(jsonPath("$.stageHistory", hasSize(2)));

        // same status again -> business rule violation
        mockMvc.perform(patch("/api/applications/" + id + "/status")
                        .header("Authorization", auth)
                        .contentType(APPLICATION_JSON)
                        .content("{\"status\":\"SCREENING\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/applications")
                        .header("Authorization", auth)
                        .param("status", "SCREENING")
                        .param("company", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/applications")
                        .header("Authorization", auth)
                        .param("status", "APPLIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/dashboard/stats").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(1));
    }

    @Test
    void protectedEndpoints_requireAuth_andOwnershipIsEnforced() throws Exception {
        mockMvc.perform(get("/api/applications")).andExpect(status().isForbidden());

        String authA = "Bearer " + registerAndGetToken("a@example.com");
        String authB = "Bearer " + registerAndGetToken("b@example.com");
        long id = createApplication(authA, "Globex");

        mockMvc.perform(get("/api/applications/" + id).header("Authorization", authB))
                .andExpect(status().isNotFound());
    }

    private String registerAndGetToken(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", email, "password", "password123", "fullName", "Test User"));

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createApplication(String auth, String company) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "companyName", company, "jobTitle", "Backend Engineer", "source", "LinkedIn"));

        MvcResult result = mockMvc.perform(post("/api/applications")
                        .header("Authorization", auth)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}