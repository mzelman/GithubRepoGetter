package mz.githubrepogetter.web;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import mz.githubrepogetter.entity.Repository;
import mz.githubrepogetter.service.GithubService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class GithubController {

    private GithubService githubService;

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<Repository>> getUserNonForkRepos(@RequestParam String username) {
        return new ResponseEntity<List<Repository>>(githubService.getUserNonForkRepos(username), HttpStatus.OK);
    }

}