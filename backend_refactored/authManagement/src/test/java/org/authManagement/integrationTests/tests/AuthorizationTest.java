package org.authManagement.integrationTests.tests;

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
@SpringBootTest // the SpringBootTest annotation should find the main application class
@AutoConfigureMockMvc
class AuthorizationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
//    @Test
//    void testInvalidCompanyRegistration() throws Exception {
//        // Create invalid request (missing required fields)
//        CompanyRegisterRequest invalidRequest = new CompanyRegisterRequest(
//            "", // Empty ID
//            "example.com",
//            "INVALID_TIER",
//            "not-an-email",
//            "example.com"
//        );
//
//        // Perform request and expect validation errors
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register/company")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(new ObjectMapper().writeValueAsString(invalidRequest)))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").isArray())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].field").exists());
//    }
}
