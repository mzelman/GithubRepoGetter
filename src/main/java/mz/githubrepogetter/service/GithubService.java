package mz.githubrepogetter.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import mz.githubrepogetter.entity.Branch;
import mz.githubrepogetter.entity.Repository;
import mz.githubrepogetter.exception.UsernameNotFoundException;

@Service
public class GithubService {

    private String baseUrl = "https://api.github.com/";
    private final RestClient restClient;

    public GithubService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<Repository> getUserNonForkRepos(String username) {
        List<Repository> repositories = getUserRepositories(username);
        filterOutForkRepositories(repositories);
        repositories.stream().forEach((repository) -> addRepositoryBranches(repository, getRepositoryBranches(repository)));
        return repositories;
    }

    public List<Repository> getUserRepositories(String username) {
        return restClient.get()
                .uri(baseUrl + "users/" + username + "/repos")
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new UsernameNotFoundException(username);
                    } else {
                        List<Repository> repos = Arrays.asList(response.bodyTo(Repository[].class));
                        return repos;
                    }});
    }

    public List<Repository> filterOutForkRepositories(List<Repository> repositories) {
        return repositories.stream()
                .filter((repository) -> !repository.isFork())
                .collect(Collectors.toList());
    }

    public Repository addRepositoryBranches(Repository repository, List<Branch> branches) {
        repository.setBranches(branches);
        return repository;
    }

    public List<Branch> getRepositoryBranches(Repository repository) {
        Branch[] branches = restClient.get()
                            .uri(repository.getUrl() + "/branches")
                            .retrieve()
                            .body(Branch[].class);
        return Arrays.asList(branches);
    }

}