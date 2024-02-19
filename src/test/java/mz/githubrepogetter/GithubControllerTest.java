package mz.githubrepogetter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import mz.githubrepogetter.pojo.Owner;
import mz.githubrepogetter.pojo.Repository;
import mz.githubrepogetter.service.GithubService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GithubControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubService githubService;

    @Test
    public void testGetUserRepositories() throws Exception {
        List<Repository> repositories = Arrays.asList(
                new Repository("example1", new Owner("testUser"), false, "url1", null),
                new Repository("example2", new Owner("testUser"), false, "url2", null));

        when(githubService.getUserRepositories(anyString())).thenReturn(repositories);

        mockMvc.perform(get("/repositories").param("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(repositories.size()))
                .andExpect(jsonPath("$[0].name").value("example1"))
                .andExpect(jsonPath("$[1].name").value("example2"));

        verify(githubService, times(1)).getUserRepositories("testUser");
    }
}