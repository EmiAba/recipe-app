package app;

import app.user.service.UserInit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest

class RecipeAppApplicationTests {

	@MockBean
	private UserInit userInit;

	@Test
	void contextLoads() {
	}

}
