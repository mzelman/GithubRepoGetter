package mz.githubrepogetter.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import mz.githubrepogetter.exception.ServiceUnavailableException;
import mz.githubrepogetter.exception.UsernameNotFoundException;
import mz.githubrepogetter.pojo.Branch;
import mz.githubrepogetter.pojo.Repository;

@Service
public class GithubService {

    private String baseUrl = "https://api.github.com/";

    public List<Repository> getUserRepositories(String username) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection(baseUrl + "users/" + username + "/repos");
            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                ObjectMapper mapper = new ObjectMapper();
                Repository[] repositories = mapper.readValue(connection.getInputStream(), Repository[].class);
                return addRepositoryBranches(filterOutForkRepositories(repositories));
            } else if (responseCode == 404) {
                throw new UsernameNotFoundException(username);
            } else {
                throw new ServiceUnavailableException();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch user repositories", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public HttpURLConnection createConnection(String address) throws URISyntaxException, IOException {
        URL url = new URI(address).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    public List<Repository> filterOutForkRepositories(Repository[] repositories) {
        return Arrays.asList(repositories).stream()
                .filter((repository) -> !repository.isFork())
                .collect(Collectors.toList());
    }

    public List<Repository> addRepositoryBranches(List<Repository> repositories) {
        repositories.stream().forEach((repository) -> repository.setBranches(getRepositoryBranches(repository)));
        return repositories;
    }

    public List<Branch> getRepositoryBranches(Repository repository) {
        HttpURLConnection connection = null;
        try {
            connection = createConnection(repository.getUrl() + "/branches");
            ObjectMapper mapper = new ObjectMapper();
            Branch[] branches = mapper.readValue(connection.getInputStream(), Branch[].class);
            return Arrays.asList(branches);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch user branches", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}