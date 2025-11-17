package app.web.mapper;

import app.user.model.User;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void testMapUserToUserEditRequest() {

        User user = User.builder()
                .firstName("Emi")
                .lastName("Aba")
                .profilePicture("https://example.com/photo.jpg")
                .country("Japan")
                .build();


        UserEditRequest result = DtoMapper.mapUserToUserEditRequest(user);


        assertNotNull(result);
        assertEquals("Emi", result.getFirstName());
        assertEquals("Aba", result.getLastName());
        assertEquals("https://example.com/photo.jpg", result.getProfilePicture());
        assertEquals("Japan", result.getCountry());
    }

}
