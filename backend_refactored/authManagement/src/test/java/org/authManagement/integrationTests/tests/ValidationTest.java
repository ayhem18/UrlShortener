package org.authManagement.integrationTests.tests;

import java.util.Random;

import org.authManagement.integrationTests.configurations.TestConfig;
import org.authManagement.requests.CompanyRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.utils.CustomGenerator;


@SpringBootTest(classes = TestConfig.class) // the SpringBootTest annotation should find the main application class and use it to load all the beans needed
@AutoConfigureMockMvc
public class ValidationTest {

    private MockMvc mockMvc;
    private CustomGenerator customGenerator;
    
    @Autowired
    public ValidationTest(MockMvc mockMvc, CustomGenerator customGenerator) {
        this.mockMvc = mockMvc;
        this.customGenerator = customGenerator;
    }


    @Test
    // Create request body
    void testRegisterCompany() throws Exception {
        // create a short company id
        for (int i = 0; i < 10; i++) {
            double random = Math.random();

            int n;
            if (random < 0.5) {
                n = (new Random()).nextInt(7);
            } else {
                n = (new Random()).nextInt(10) + 17;
            }

            CompanyRegisterRequest request = new CompanyRegisterRequest(
                this.customGenerator.randomAlphaString(n), 
                this.customGenerator.randomAlphaString(n) + ".com", 
                "TIER_1", 
                "owner@example.com", 
                "example.com"
        );

        // Perform POST request and validate response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register/company")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    }
     
}
