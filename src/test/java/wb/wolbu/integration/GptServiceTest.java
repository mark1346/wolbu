package wb.wolbu.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import wb.wolbu.service.GptService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class GptServiceTest {
    @Autowired
    private GptService gptService;

    @Test
    public void getCourseRecommendationTest() {
        // Given
        String userGoal = "저는 31살 직장인입니다. 현재 모아둔 돈은 1억 정도이고, 매달 수입은 400만원 정도 발생하고 있습니다. 60세쯤 은퇴한 후엔, 주식 배당금으로 생활하고 싶어요.";

        // When
        String recommendation = gptService.getCourseRecommendation(userGoal);

        // Then
        System.out.println("Recommendation: " + recommendation);
        assertNotNull(recommendation);
        assertTrue(recommendation.length() > 0);
    }
}
