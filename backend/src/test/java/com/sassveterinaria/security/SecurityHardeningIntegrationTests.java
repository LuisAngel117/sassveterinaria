package com.sassveterinaria.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sassveterinaria.auth.domain.AppUserEntity;
import com.sassveterinaria.auth.domain.BranchEntity;
import com.sassveterinaria.auth.domain.UserBranchEntity;
import com.sassveterinaria.auth.domain.UserBranchId;
import com.sassveterinaria.auth.repo.AppUserRepository;
import com.sassveterinaria.auth.repo.BranchRepository;
import com.sassveterinaria.auth.repo.UserBranchRepository;
import com.sassveterinaria.auth.service.TotpService;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityHardeningIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserBranchRepository userBranchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    @Test
    void twoFactorSetupEnableAndChallengeLoginFlow() throws Exception {
        TestUser admin = createUser("admin2fa", "ADMIN", "Admin123!");

        JsonNode firstLogin = login(admin.username(), "Admin123!");
        String accessToken = firstLogin.path("accessToken").asText();
        String branchId = firstLogin.path("branch").path("id").asText();

        MvcResult setupResult = mockMvc.perform(post("/api/v1/auth/2fa/setup")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode setupJson = objectMapper.readTree(setupResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String secret = setupJson.path("secret").asText();

        String code = totpService.generateCurrentCode(secret);
        String enableBody = "{\"code\":\"" + code + "\"}";
        mockMvc.perform(post("/api/v1/auth/2fa/enable")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(enableBody))
            .andExpect(status().isNoContent());

        JsonNode secondLogin = login(admin.username(), "Admin123!");
        String challengeToken = secondLogin.path("challengeToken").asText();

        String login2faBody = "{\"challengeToken\":\"" + challengeToken + "\",\"code\":\"" + totpService.generateCurrentCode(secret) + "\"}";
        MvcResult login2faResult = mockMvc.perform(post("/api/v1/auth/login/2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login2faBody))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode login2faJson = objectMapper.readTree(login2faResult.getResponse().getContentAsString(StandardCharsets.UTF_8));

        org.junit.jupiter.api.Assertions.assertTrue(login2faJson.path("accessToken").asText().length() > 20);
        org.junit.jupiter.api.Assertions.assertEquals(branchId, login2faJson.path("branch").path("id").asText());
    }

    @Test
    void lockoutAfterFourFailedAttempts() throws Exception {
        TestUser user = createUser("lockuser", "ADMIN", "Admin123!");

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + user.username() + "\",\"password\":\"WrongPass123!\"}"))
                .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + user.username() + "\",\"password\":\"Admin123!\"}"))
            .andExpect(status().isLocked());
    }

    @Test
    void rateLimitReturns429OnLoginFlood() throws Exception {
        String username = "rl-user";
        String body = "{\"username\":\"" + username + "\",\"password\":\"Nope123!\"}";
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void sensitiveEndpointReturns403WithoutPermission() throws Exception {
        TestUser recepcion = createUser("recepforbidden", "RECEPCION", "Recepcion123!");
        JsonNode login = login(recepcion.username(), "Recepcion123!");
        String token = login.path("accessToken").asText();
        String branchId = login.path("branch").path("id").asText();

        String body = "{\"taxRate\":0.1500,\"reason\":\"actualizacion de impuestos\"}";
        mockMvc.perform(put("/api/v1/config/tax")
                .header("Authorization", "Bearer " + token)
                .header("X-Branch-Id", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    private JsonNode login(String username, String password) throws Exception {
        String loginBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private TestUser createUser(String username, String roleCode, String password) {
        UUID branchId = UUID.nameUUIDFromBytes(("branch-" + username).getBytes(StandardCharsets.UTF_8));
        BranchEntity branch = new BranchEntity();
        branch.setId(branchId);
        branch.setCode("B" + username.substring(0, Math.min(10, username.length())).toUpperCase());
        branch.setName("Branch " + username);
        branch.setActive(true);
        branch.setCreatedAt(OffsetDateTime.now());
        branchRepository.save(branch);

        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.nameUUIDFromBytes(("user-" + username).getBytes(StandardCharsets.UTF_8)));
        user.setEmail(username);
        user.setFullName("User " + username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoleCode(roleCode);
        user.setActive(true);
        user.setLockedUntil(null);
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setTotpVerifiedAt(null);
        user.setCreatedAt(OffsetDateTime.now());
        appUserRepository.save(user);

        UserBranchEntity ub = new UserBranchEntity();
        ub.setId(new UserBranchId(user.getId(), branchId));
        ub.setDefault(true);
        userBranchRepository.save(ub);

        return new TestUser(username, user.getId(), branchId);
    }

    private record TestUser(String username, UUID userId, UUID branchId) {
    }
}
