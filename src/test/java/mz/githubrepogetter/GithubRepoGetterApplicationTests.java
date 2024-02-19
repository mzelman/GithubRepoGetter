package mz.githubrepogetter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import mz.githubrepogetter.exception.UsernameNotFoundException;
import mz.githubrepogetter.pojo.Owner;
import mz.githubrepogetter.pojo.Repository;
import mz.githubrepogetter.service.GithubService;

@SpringBootTest
@AutoConfigureMockMvc
class GithubRepoGetterApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private GithubService githubService;

	@Test
	void contextLoads() {
		assertNotNull(mockMvc);
	}

	@Test
	public void testGetDataSuccess() throws Exception {
		Repository[] repositories = {
				new Repository("example1", new Owner("user"), false, "url1", null),
				new Repository("example2", new Owner("user"), false, "url2", null),
				new Repository("example3", new Owner("user"), false, "url3", null)
		};
		when(githubService.getUserRepositories("user")).thenReturn(Arrays.asList(repositories));

		mockMvc.perform(get("/repositories").param("username", "user"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	public void testGetDataNotFound() throws Exception {
		when(githubService.getUserRepositories("user")).thenThrow(new UsernameNotFoundException("user"));

		mockMvc.perform(get("/repositories").param("username", "user"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value("404"))
				.andExpect(jsonPath("$.message").value("User with username user does not exist."));
	}

}