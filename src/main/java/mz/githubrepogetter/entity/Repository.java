package mz.githubrepogetter.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Repository {

    private String name;
    private Owner owner;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean fork;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String url;
    private List<Branch> branches;

}