# GitHub Repositories API

## Overview

This project provides an API endpoint to list all GitHub repositories of a user, excluding forks. It returns essential information about each repository, including the repository name, owner's login, and names of every branch with the last commit SHA.

## Error Handling

- User Not Found: If the specified username is not found, the API responds with a 404 status code and displays the corresponding message.
- GitHub API Issues: If the GitHub API is down or faces other problems, the API responds with a 503 status code.

## Usage

To use this API, make a GET request to the following endpoint:

```bash
GET /repositories?username=yourUsername
```

By default API is running on localhost:8080.

### Request Headers

- `Accept: application/json`

### Request Parameters

- `username`: The GitHub username for which you want to list repositories.

### Response Format

### When Username is Found

If the specified username is found, the API responds with a JSON array containing information about each repository. Each object in the array has the following properties:

- `name`: The name of the repository.
- `owner`: An object containing the login of the repository owner.
  - `login`: The username of the repository owner.
- `branches`: An array containing details of each branch, including:
  - `name`: The name of the branch.
  - `commit`: An object containing the SHA of the last commit in the branch.
    - `sha`: The SHA of the last commit in the branch.

#### Example Response:

```json
[
    {
        "name": "repo1",
        "owner": {
            "login": "owner"
        },
        "branches": [
            {
                "name": "branch1",
                "commit": {
                    "sha": "commitSha1"
                }
            },
            {
                "name": "branch2",
                "commit": {
                    "sha": "commitSha2"
                }
            }
        ]
    },
    {
        "name": "repo2",
        "owner": {
            "login": "owner"
        },
        "branches": [
            {
                "name": "branch3",
                "commit": {
                    "sha": "commitSha3"
                }
            }
        ]
    }
]
```

### When Username is Not Found

If there is no user with the specified username, the API responds with a JSON object containing the following properties:

- `status`: The status code of 404.
- `message`: An information about error.

#### Example Response:

```json
{
    "status": "404",
    "message": "User with username ${username} does not exist."
}
```

### When There are Github API Issues

If there are issues with Github API, the API responds with a JSON object containing the following properties:

- `status`: The status code of 503.
- `message`: An information about error.

#### Example Response

```json
{
    "status": "503",
    "message": "Service unavailable. Try again later."
}
```

### Example Request

```bash
GET http://localhost:8080/repositories?username=user
Accept: application/json
```

### Built with

- Java 21
- Maven 4.0.0
- Spring Boot 3.2.2

### Running the project

1. Clone this repository to your local machine.
2. Navigate to the project directory.
3. Run the project using the mvn spring-boot:run command.
4. You can also create a jar file with command mvn clean package and run it with java -jar githubrepogetter-1.0.0.jar.
5. Make requests to the API endpoint as described above.
