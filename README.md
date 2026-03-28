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
- Hybrid API: REST (Auth, Upload) + GraphQL (Feed, Query) + WebSocket (Notify, Message)

## Main Features
- Post moments (image & text)
- Comment on moments
- Heart (Like / Unlike)
- Follow / Unfollow, Block /  users
- Activity Feed (Post / Comment / Like)
- Filter activity by type
- JWT & OAuth2 Authentication
- GraphQL Batch Loader & DTO Projection (N+1 problem prevention)

## 🛡️ Security Flow
Login → JWT 발급 → Authorization Header → API 인증

## Project Structure

```
backend
 ├── config            # Application configuration
 ├── controller        # REST Controller
 ├── custom            # Custom annotations & helpers
 ├── entity           
 │   ├── dto           # Data Transfer Objects
 │   └── model         # JPA entities
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
 ├── wrapper           # Unified API / REST, GraphQL response wrappers
 └── ws                # WebSocket config

frontend
 ├── static            # HTML, CSS, Vanilla JS
 │   └── icon          # Favicon & static icons
 └── graphql           # GraphQL queries & mutations

```


