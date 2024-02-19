package mz.githubrepogetter.web;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import mz.githubrepogetter.pojo.Repository;
import mz.githubrepogetter.service.GithubService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class GithubController {

    @Autowired
    private GithubService githubService;

    @GetMapping("/repositories")
    public ResponseEntity<List<Repository>> getUserRepositories(@RequestParam String username) {
        return new ResponseEntity<List<Repository>>(githubService.getUserRepositories(username), HttpStatus.OK);
    }

}