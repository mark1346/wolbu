package wb.wolbu.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import wb.wolbu.dto.MemberDTO;
import wb.wolbu.dto.PasswordChangeDTO;
import wb.wolbu.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemberIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;

    private static Long memberId;

    @Test
    @Order(1)
    @DisplayName("회원 가입 테스트")
    public void testRegisterMember() throws Exception {
        // Given
        MemberDTO memberDTO = MemberDTO.builder()
                .name("John Doe")
                .email("asd@gmail.com")
                .phoneNumber("010-1234-5678")
                .password("Password1!")
                .memberType("STUDENT")
                .build();

        // When & Then
        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("asd@gmail.com"))
                .andExpect(jsonPath("$.memberType").value("STUDENT"))
                .andDo(result -> {
                    MemberDTO createdMember = objectMapper.readValue(result.getResponse().getContentAsString(), MemberDTO.class);
                    memberId = createdMember.getId();
                });
    }

    @Test
    @Order(2)
    @DisplayName("회원 정보 조회 테스트")
    @WithMockUser(username = "John Doe", roles = "STUDENT")
    public void testGetMember() throws Exception {
        mockMvc.perform(get("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("asd@gmail.com"))
                .andExpect(jsonPath("$.memberType").value("STUDENT"));
    }

    @Test
    @Order(3)
    @DisplayName("회원 정보 수정 테스트")
    @WithMockUser(username = "John Doe", roles = "STUDENT")
    public void testUpdateMember() throws Exception {
        MemberDTO updateDTO = MemberDTO.builder()
                .name("John Updated")
                .email("john@example.com")
                .phoneNumber("010-9876-5432")
                .password("Password1!")
                .memberType("STUDENT")
                .build();

        mockMvc.perform(put("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("010-9876-5432"));
    }

    @Test
    @Order(4)
    @DisplayName("비밀번호 변경 테스트")
    @WithMockUser(username = "John Doe", roles = "STUDENT")
    public void testChangePassword() throws Exception {
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO("Password1!", "NewPass1!");

        mockMvc.perform(patch("/api/members/" + memberId + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(5)
    @DisplayName("회원 삭제 테스트")
    @WithMockUser(username = "John Doe", roles = "STUDENT")
    public void testDeleteMember() throws Exception {
        mockMvc.perform(delete("/api/members/" + memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertFalse(memberRepository.existsById(memberId));
    }

}
