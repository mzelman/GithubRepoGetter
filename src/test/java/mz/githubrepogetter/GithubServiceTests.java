package mz.githubrepogetter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mz.githubrepogetter.entity.Branch;
import mz.githubrepogetter.entity.Commit;
import mz.githubrepogetter.entity.Owner;
import mz.githubrepogetter.entity.Repository;
import mz.githubrepogetter.exception.UsernameNotFoundException;
import mz.githubrepogetter.service.GithubService;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@RestClientTest(GithubService.class)
public class GithubServiceTests {

    @Autowired
    MockRestServiceServer server;

    @Autowired
    GithubService githubService;

    @Autowired
    ObjectMapper mapper;

    private List<Repository> repositories;

    @Test
    public void testGetUserRepositories() throws JsonProcessingException {

        repositories = List.of(
            new Repository("example1", new Owner("user1"), false, "url1", null),
            new Repository("example2", new Owner("user1"), false, "url2", null),
            new Repository("example3", new Owner("user1"), false, "url3", null)
    );

        server.expect(requestTo("https://api.github.com/users/user1/repos"))
                            .andRespond(withSuccess(mapper.writeValueAsString(repositories), MediaType.APPLICATION_JSON));

        List<Repository> foundRepositories = githubService.getUserRepositories("user1");
        assertEquals(3, foundRepositories.size());
    }

    @Test
    public void testGetUserRepositoriesInvalidUser() throws JsonProcessingException {

        server.expect(requestTo("https://api.github.com/users/user1/repos"))
        .andRespond(withStatus(HttpStatusCode.valueOf(404)));

        assertThrows(UsernameNotFoundException.class, () -> githubService.getUserRepositories("user1"));
    }

    @Test
    public void testFilterOutForkRepositories_NoForks() {

        repositories = List.of(
            new Repository("example1", new Owner("user1"), false, "url1", null),
            new Repository("example2", new Owner("user2"), false, "url2", null),
            new Repository("example3", new Owner("user3"), false, "url3", null)
    );

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(3, filtered.size());
    }

    @Test
    public void testFilterOutForkRepositories_WithForks() {
        repositories = List.of(
            new Repository("example1", new Owner("user1"), false, "url1", null),
            new Repository("example2", new Owner("user2"), true, "url2", null),
            new Repository("example3", new Owner("user3"), false, "url3", null)
    );

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(2, filtered.size());
    }

    @Test
    public void testFilterOutForkRepositories_AllForks() {
        repositories = List.of(
            new Repository("example1", new Owner("user1"), true, "url1", null),
            new Repository("example2", new Owner("user2"), true, "url2", null),
            new Repository("example3", new Owner("user3"), true, "url3", null)
    );

        List<Repository> filtered = githubService.filterOutForkRepositories(repositories);

        assertEquals(0, filtered.size());
    }

    @Test
    public void testAddRepositoryBranches() throws JsonProcessingException {

        repositories = List.of(
            new Repository("example1", new Owner("user1"), true, "url1", null),
            new Repository("example2", new Owner("user1"), true, "url2", null)
    );

        List<Branch> branchesRepo1 = Arrays.asList(new Branch("branch1", new Commit("1234")),
                new Branch("branch2", new Commit("2345")));
        List<Branch> branchesRepo2 = Arrays.asList(new Branch("branch3", new Commit("3456")));

        Repository repository1 = githubService.addRepositoryBranches(repositories.get(0), branchesRepo1);
        Repository repository2 = githubService.addRepositoryBranches(repositories.get(1), branchesRepo2);

        assertEquals(2, repository1.getBranches().size());
        assertEquals(1, repository2.getBranches().size());
    }

    @Test
    public void getRepositoryBranchesTest() throws JsonProcessingException {

        repositories = List.of(
            new Repository("example1", new Owner("user1"), true, "url1", null),
            new Repository("example2", new Owner("user1"), true, "url2", null)
    );

        List<Branch> branchesRepo1 = Arrays.asList(new Branch("branch1", new Commit("1234")),
                new Branch("branch2", new Commit("2345")));
        List<Branch> branchesRepo2 = Arrays.asList(new Branch("branch3", new Commit("3456")));

        server.expect(requestTo("https://api.github.com/url1/branches"))
            .andRespond(withSuccess(mapper.writeValueAsString(branchesRepo1), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.github.com/url2/branches"))
            .andRespond(withSuccess(mapper.writeValueAsString(branchesRepo2), MediaType.APPLICATION_JSON));

            assertEquals(2, githubService.getRepositoryBranches(repositories.get(0)).size());
            assertEquals(1, githubService.getRepositoryBranches(repositories.get(1)).size());
    }

}