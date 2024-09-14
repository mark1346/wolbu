package wb.wolbu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GptService {
    @Autowired
    public GptService(RestTemplate restTemplate, @Value("${openai.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Value("${openai.api.key}")
    private final String apiKey;

    private final RestTemplate restTemplate;

    public String getCourseRecommendation(String userGoal) {
        // 사용자의 목표를 포함한 프롬프트 생성
        String prompt = String.format("""
            사용자 목표: %s
            
            위 목표에 맞는 강의 3개를 추천해주세요. 다음 가이드라인을 엄격히 따라주세요:
            1. 첫 문장은 반드시 "X하는 목표를 갖고 계시군요!"로 시작하세요. X는 사용자 목표의 핵심을 간단히 요약한 것입니다.
            2. 두 번째 문장은 목표 달성을 위한 간단한 조언을 제공하세요.
            3. 세 번째 문장은 "A, B, C 강의를 추천드려요!"로 끝내세요. A, B, C는 각각 작은따옴표로 감싼 강의명입니다.
            4. 강의명은 실제 존재하는 것처럼 구체적이고 관련성 있게 만들어주세요.
            5. 전체 답변은 세 문장을 넘지 않아야 합니다.
            6. 친근하고 격려하는 톤을 사용하되, 존댓말을 사용하세요.
            
            예시 답변:
            "은퇴 후엔 주식 배당금으로 생활하는 목표를 갖고 계시군요! 매달 수입의 일정 퍼센티지를 배당주에 투자하여, 적립식으로 포트폴리오를 쌓는 것을 추천 드려요. '배당금으로 먹고사는 법', '30대 직장인 주식포트폴리오 만들기', '금융소득으로 월 100' 강의를 추천드려요!"
            
            위 예시와 비슷한 형식과 길이로 답변을 작성해주세요. 단, 내용은 사용자의 실제 목표에 맞게 변경해야 합니다.
            """, userGoal);

        // API 요청을 위한 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // API 요청 본문 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", Arrays.asList(
                new HashMap<String, String>() {{
                    put("role", "system");
                    put("content", "당신은 '월급쟁이부자들'플랫폼의 강의 추천 로봇입니다. 사용자의 목표를 듣고 적절한 강의를 추천해주세요.");
                }},
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        ));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> choice = choices.get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");
        return message.get("content");
    }
}

