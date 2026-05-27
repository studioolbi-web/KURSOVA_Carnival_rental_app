package com.oliinyk.costumes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.oliinyk.costumes.model.User;
import com.oliinyk.costumes.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, emailService);
    }

    @Test
    void registerUser_Successful() {
        String email = "test@example.com";
        String password = "password";
        String role = "USER";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User registeredUser = authService.registerUser(email, password, role);

        assertNotNull(registeredUser);
        assertEquals(email, registeredUser.getEmail());
        assertTrue(BCrypt.checkpw(password, registeredUser.getPasswordHash()));
        assertFalse(registeredUser.isVerified());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq(email), anyString());
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsException() {
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.registerUser(email, "password", "USER"));
    }

    @Test
    void login_Successful() {
        String email = "test@example.com";
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user =
                User.builder().email(email).passwordHash(hashedPassword).isBlocked(false).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = authService.login(email, password);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    void login_InvalidPassword_ReturnsEmpty() {
        String email = "test@example.com";
        String password = "wrongpassword";
        String hashedPassword = BCrypt.hashpw("correctpassword", BCrypt.gensalt());
        User user = User.builder().email(email).passwordHash(hashedPassword).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = authService.login(email, password);

        assertTrue(result.isEmpty());
    }

    @Test
    void login_UserBlocked_ThrowsException() {
        String email = "blocked@example.com";
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user =
                User.builder().email(email).passwordHash(hashedPassword).isBlocked(true).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> authService.login(email, password));
        assertEquals("Ваш акаунт заблоковано адміністратором.", exception.getMessage());
    }

    @Test
    void verifyUser_Successful() {
        String email = "test@example.com";
        String code = "123456";
        User user = User.builder().email(email).verificationCode(code).isVerified(false).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = authService.verifyUser(email, code);

        assertTrue(result);
        assertTrue(user.isVerified());
        assertNull(user.getVerificationCode());
        verify(userRepository).update(user);
    }

    @Test
    void verifyUser_WrongCode_ReturnsFalse() {
        String email = "test@example.com";
        User user =
                User.builder().email(email).verificationCode("111111").isVerified(false).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = authService.verifyUser(email, "222222");

        assertFalse(result);
        assertFalse(user.isVerified());
    }
}
