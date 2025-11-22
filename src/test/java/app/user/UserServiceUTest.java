package app.user;

import app.exception.LastAdminException;
import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;



    @Test
    void whenEditUserDetails_andRepositoryReturnsOptionalEmpty_thenThrowException() {

        UUID userId = UUID.randomUUID();
        UserEditRequest dto = null;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.editUserDetails(userId, dto));
    }


    @Test
    void whenEditUserProfile_andRepositoryReturnUserFromDatabase_thenChangeTheUserDetails_andSaveItToTheDatabase() {
        UUID userId = UUID.randomUUID();

        UserEditRequest dto = UserEditRequest.builder()
                .firstName("Eva")
                .lastName("Abadjieva")
                .profilePicture("www.picture.com")
                .country("Bulgaria")
                .build();


        User userRetrievedFromDatabase = User.builder()
                        .id(userId)
                        .firstName(("Kiro"))
                        .lastName("Kirov")
                        .profilePicture(null)
                        .country("Bulgaria")
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userRetrievedFromDatabase));

        userService.editUserDetails(userId, dto);

        assertEquals("Eva", userRetrievedFromDatabase.getFirstName());
        assertEquals("Abadjieva", userRetrievedFromDatabase.getLastName());
        assertNotNull(userRetrievedFromDatabase.getProfilePicture());
        assertEquals("www.picture.com", userRetrievedFromDatabase.getProfilePicture());
        assertEquals("Bulgaria",userRetrievedFromDatabase.getCountry());

        verify(userRepository, times(1)).save(userRetrievedFromDatabase);

    }

    @Test
    void whenChangeUserRole_andRepositoryReturnsOptionalEmpty_thenThrowException() {
        UUID userId = UUID.randomUUID();
        UserRole newRole = UserRole.ADMIN;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,  () -> userService.changeUserRole(userId, newRole));
    }


    @Test
    void whenChangeUserRole_fromUserToAdmin_thenChangeRoleSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeUserRole(userId, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, user.getRole());
        assertNotNull(user.getUpdatedOn());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void whenChangeUserRole_fromAdminToUser_andMultipleAdminsExist_thenChangeRoleSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(3L);

        userService.changeUserRole(userId, UserRole.USER);

        assertEquals(UserRole.USER, user.getRole());
        assertNotNull(user.getUpdatedOn());
        verify(userRepository, times(1)).save(user);
    }


    @Test
    void whenChangeUserRole_fromAdminToUser_andOnlyOneAdminExists_thenThrowException() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(LastAdminException.class,
                () -> userService.changeUserRole(userId, UserRole.USER));


        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository, never()).save(user);
    }

    @Test
    void whenGetUserCount_thenReturnCount() {
        when(userRepository.countByRole(UserRole.USER)).thenReturn(5L);

        long result = userService.getUserCount();

        assertEquals(5L, result);
        verify(userRepository, times(1)).countByRole(UserRole.USER);
    }



    @Test
    void whenGetTotalUsers_thenReturnTotalCount() {

        when(userRepository.count()).thenReturn(13L);


        long result = userService.getTotalUsers();


        assertEquals(13L, result);
        verify(userRepository, times(1)).count();
    }


    @Test
    void givenExistingUsername_whenRegisterUser_thenThrowException() {

        RegisterRequest registerRequest=  RegisterRequest.builder()
                .username("Emi123")
                .email("abad@email.com")
                .password("123")
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));
        verify(userRepository,never()).save(any());
    }


    @Test
    void givenExistingEmail_whenRegisterUser_thenThrowException() {

        RegisterRequest registerRequest=  RegisterRequest.builder()
                .username("Emi123")
                .email("abad@email.com")
                .password("123")
                .build();

        when(userRepository.findByUsername("Emi123")) .thenReturn(Optional.empty());
        when(userRepository.findByEmail("abad@email.com")) .thenReturn(Optional.of(new User()));

        assertThrows(UsernameAlreadyExistException.class,  () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }


    @Test
    void givenNewUser_whenRegister_thenCreateUser() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Emi123")
                .email("abad@email.com")
                .password("123")
                .build();

        when(userRepository.findByUsername("Emi123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("abad@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());



        User registeredUser = userService.register(registerRequest);


        assertThat(registeredUser).isNotNull();
        verify(passwordEncoder).encode("123");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void whenGetAllUsers_thenReturnUserList() {

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();
        List<User> users = List.of(user1, user2, user3);

        when(userRepository.findAll()).thenReturn(users);


        List<User> result = userService.getAllUsers();


        assertNotNull(result);
        assertEquals(3, result.size());
        verify(userRepository, times(1)).findAll();
    }



    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        String username = "Emi123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password("encodedPassword")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername(username);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void givenMissingUserFromDatabase_whenEditUserDetails_thenExceptionIsThrown() {

        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.editUserDetails(userId, dto));


    }




}