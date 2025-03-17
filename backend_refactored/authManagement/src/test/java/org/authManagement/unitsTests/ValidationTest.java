package org.authManagement.unitsTests;

import   java.util.Random;

import org.authManagement.configurations.WebTestConfig;
import org.authManagement.controllers.AuthController;
import org.authManagement.requests.CompanyRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.utils.CustomGenerator;


// let's think about the necessary annotations for this test
// to load the mockMVC only for the specific controller
// to load stub repositories: using context: SpringBootTest(class = some_config_class)
@SpringJUnitConfig(classes = WebTestConfig.class)
@WebMvcTest(AuthController.class)
public class ValidationTest {

    private MockMvc mockMvc;
    private CustomGenerator customGenerator;

    @Autowired
    public ValidationTest(MockMvc mockMvc, CustomGenerator customGenerator) {
        this.mockMvc = mockMvc;
        this.customGenerator = customGenerator;
    }

    @Test
    void testRegisterCompanyValidateCompanyId() throws Exception {
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


            mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/auth/register/company")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(new ObjectMapper().writeValueAsString(request)
                                    )
                    )
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

}

