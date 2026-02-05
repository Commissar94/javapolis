package javapolis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javapolis.model.dto.JudgeRequest;
import javapolis.model.dto.JudgeResponse;
import javapolis.service.JudgeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(JudgeControllerTest.Config.class)
class JudgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    static class Config {
        @Bean
        public JudgeService judgeService() {
            return Mockito.mock(JudgeService.class);
        }

        @Bean
        public JudgeController judgeController(JudgeService judgeService) {
            return new JudgeController(judgeService);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public MockMvc mockMvc(JudgeController judgeController) {
            return MockMvcBuilders.standaloneSetup(judgeController).build();
        }
    }

    @Test
    void judge_ShouldReturnResponse() throws Exception {
        // Given
        JudgeRequest request = new JudgeRequest("java", "public class Main {}");
        JudgeResponse expectedResponse = new JudgeResponse("OK", "output", "", 0);
        
        when(judgeService.judge(any(JudgeRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/judge/solutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.stdout").value("output"))
                .andExpect(jsonPath("$.stderr").value(""))
                .andExpect(jsonPath("$.exitCode").value(0));
    }
}
