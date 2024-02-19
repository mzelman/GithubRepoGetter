package mz.githubrepogetter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import mz.githubrepogetter.exception.ServiceUnavailableException;
import mz.githubrepogetter.exception.UsernameNotFoundException;
import mz.githubrepogetter.pojo.Branch;
import mz.githubrepogetter.pojo.Commit;
import mz.githubrepogetter.pojo.Owner;
import mz.githubrepogetter.pojo.Repository;
import mz.githubrepogetter.service.GithubService;

@ExtendWith(MockitoExtension.class)
public class GithubServiceTests {

    @InjectMocks
    private GithubService githubService;

    @Spy
    private GithubService githubServiceSpy;

    @Test
    void testGetUserRepositories200() throws IOException, URISyntaxException {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);

        GithubService githubService = spy(new GithubService());
        doReturn(Arrays.asList(new Branch(), new Branch())).when(githubService).getRepositoryBranches(any());

        doReturn(mockConnection).when(githubService).createConnection(anyString());
        when(mockConnection.getInputStream()).thenReturn(createRepositoriesInputStream());

        List<Repository> resultRepositories = githubService.getUserRepositories("user");

        assertEquals(2, resultRepositories.size());
    }

    @Test
    public void testGetUserRepositories404() throws IOException, URISyntaxException {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(404);

        String username = "user";
        String baseUrl = "https://api.github.com/";

        doReturn(mockConnection).when(githubServiceSpy).createConnection(baseUrl + "users/" + username + "/repos");

        assertThrows(UsernameNotFoundException.class, () -> githubServiceSpy.getUserRepositories(username));
    }

    @Test
    public void testGetUserRepositories503() throws IOException, URISyntaxException {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(503);

        String username = "user";
        String baseUrl = "https://api.github.com/";

        doReturn(mockConnection).when(githubServiceSpy).createConnection(baseUrl + "users/" + username + "/repos");

        assertThrows(ServiceUnavailableException.class, () -> githubServiceSpy.getUserRepositories(username));
    }

    @Test
    public void testCreateConnection() {
        String testAddress = "https://example.com";
        try {
            HttpURLConnection connection = githubService.createConnection(testAddress);
            assertNotNull(connection);
            assertEquals("GET", connection.getRequestMethod());
            assertEquals(testAddress, connection.getURL().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch user repositories", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL", e);
        }
    }

    @Test
    public void testFilterOutForkRepositories_NoForks() {
        Repository[] repositories = {
                new Repository("example1", new Owner(), false, "url1", null),
                new Repository("example2", new Owner(), false, "url2", null),
                new Repository("example3", new Owner(), false, "url3", null)
        };

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(3, filtered.size());
    }

    @Test
    public void testFilterOutForkRepositories_WithForks() {
        Repository[] repositories = {
                new Repository("example1", new Owner(), false, "url1", null),
                new Repository("example2", new Owner(), true, "url2", null),
                new Repository("example3", new Owner(), false, "url3", null)
        };

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterOutForkRepositories_AllForks() {
        Repository[] repositories = {
                new Repository("example1", new Owner(), true, "url1", null),
                new Repository("example2", new Owner(), true, "url2", null),
                new Repository("example3", new Owner(), true, "url3", null)
        };

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(0, filtered.size());
    }

    @Test
    public void testAddRepositoryBranches() {

        MockitoAnnotations.openMocks(this);

        Repository repo1 = new Repository("repo1", new Owner(), false, "url1", null);
        Repository repo2 = new Repository("repo2", new Owner(), false, "url2", null);
        List<Repository> repositories = Arrays.asList(repo1, repo2);
        List<Branch> branchesRepo1 = Arrays.asList(new Branch("branch1", new Commit()),
                new Branch("branch2", new Commit()));
        List<Branch> branchesRepo2 = Arrays.asList(new Branch("branch3", new Commit()),
                new Branch("branch4", new Commit()));

        doReturn(branchesRepo1).when(githubServiceSpy).getRepositoryBranches(repo1);
        doReturn(branchesRepo2).when(githubServiceSpy).getRepositoryBranches(repo2);

        List<Repository> repositoriesWithBranches = githubServiceSpy.addRepositoryBranches(repositories);

        assertEquals(2, repositoriesWithBranches.size());
        assertEquals(branchesRepo1, repositoriesWithBranches.get(0).getBranches());
        assertEquals(branchesRepo2, repositoriesWithBranches.get(1).getBranches());
    }

    @Test
    public void testGetRepositoryBranches() throws IOException, URISyntaxException {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(createBranchesInputStream());

        Repository repository = new Repository("repo1", new Owner(), false, "url1", null);

        doReturn(mockConnection).when(githubServiceSpy).createConnection(repository.getUrl() + "/branches");

        List<Branch> branches = githubServiceSpy.getRepositoryBranches(repository);

        assertEquals(2, branches.size());
    }

    private InputStream createBranchesInputStream() {
        String json = "[\n" +
                "    {\n" +
                "        \"name\": \"branch1\",\n" +
                "        \"commit\": {\n" +
                "            \"sha\": \"\"\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"branch2\",\n" +
                "        \"commit\": {\n" +
                "            \"sha\": \"\"\n" +
                "        }\n" +
                "    }\n" +
                "]";
        return new ByteArrayInputStream(json.getBytes());
    }

    private InputStream createRepositoriesInputStream() {
        String json = "[\n" +
                "    {\n" +
                "        \"name\": \"repo1\",\n" +
                "        \"owner\": {\n" +
                "            \"login\": \"user\"\n" +
                "        },\n" +
                "        \"fork\": false,\n" +
                "        \"url\": \"url1\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\": \"repo2\",\n" +
                "        \"owner\": {\n" +
                "            \"login\": \"user\"\n" +
                "        },\n" +
                "        \"fork\": false,\n" +
                "        \"url\": \"url2\"\n" +
                "    }\n" +
                "]";
        return new ByteArrayInputStream(json.getBytes());
    }

}