# Mini Social – Activity Feed (Instagram-like)

This project focuses on solving common backend challenges such as:
- Activity aggregation
- Polymorphic feed items
- N+1 query problem in GraphQL
  
## 🛠 Tech Stack
- Java 22
- Spring Boot 3
- Spring GraphQL
- Spring Security (JWT)
- Postgresql
- Hybrid API: REST (Auth, Upload) + GraphQL (Feed, Query)

## Main Features
- Post moments (image & text)
- Comment on moments
- Heart (Like / Unlike)
- Follow / Unfollow, Block /  users
- Activity Feed (Post / Comment / Like)
- Filter activity by type
- JWT & OAuth2 Authentication
- GraphQL Batch Loader & DTO Projection (N+1 problem prevention)


## Project Structure

```
backend
 ├── config            # Application configuration
 ├── controller        # REST Controller
 ├── custom            # Custom annotations & helpers
 ├── entity           
 │   ├── dto           # JPA entities
 │   └── model         # Data Transfer Objects
 ├── exception         # @RestControllerAdvice
 ├── interceptor       # Rate limiting, request interception
 ├── mapper            # Mapping entity <-> DTO
 ├── projection        # GraphQL dto projection
 ├── repository        # Spring Data JPA
 ├── resolver          # Queries, Mutation, @SchemaMapping / @BatchMapping
 │   └── batchloader 
 ├── service           # Business logic
 │   └── auth          # signup, login, refresh token
 ├── security          # JWT / OAuth2
 ├── status            # Enum definitions
 └── wrapper           # Unified API / REST, GraphQL response wrappers

frontend
 ├── static            # HTML, CSS, Vanilla JS
 │   └── icon          # Favicon & static icons
 └── graphql           # GraphQL queries & mutations

```


