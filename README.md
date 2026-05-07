# Auth Service

Microservicio de autenticación para el sistema "Sanos y Salvos" construido con Spring Boot.

## Tecnologías

- Java 17
- Spring Boot 3.2.0
- Spring Security
- JWT (JJWT)
- PostgreSQL
- Swagger/OpenAPI
- Docker

## Características

- Registro de usuarios
- Login con JWT
- Validación de tokens
- Manejo de roles (ADMIN, USER, REFUGIO)
- Encriptación de contraseñas con BCrypt
- Manejo global de errores
- Documentación con Swagger

## Endpoints

- `POST /auth/register` - Registrar nuevo usuario
- `POST /auth/login` - Iniciar sesión
- `GET /auth/validate` - Validar token JWT
- `POST /auth/refresh` - Refrescar token

## Configuración

1. Instalar Java 17 y Maven.
2. Configurar PostgreSQL y crear base de datos `authdb`.
3. Actualizar `application.properties` con las credenciales de la base de datos.
4. Cambiar `jwt.secret` por una clave segura.

## Ejecutar

```bash
mvn spring-boot:run
```

## Docker

```bash
docker build -t auth-service .
docker run -p 8080:8080 auth-service
```

## Swagger

Acceder a http://localhost:8080/swagger-ui.html para la documentación de la API.

## Estructura del Proyecto

```
src/main/java/com/sanosysalvos/authservice/
├── controller/
│   └── AuthController.java
├── service/
│   ├── AuthService.java
│   ├── JwtService.java
│   └── CustomUserDetailsService.java
├── repository/
│   ├── UserRepository.java
│   └── RoleRepository.java
├── entity/
│   ├── User.java
│   └── Role.java
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   └── RefreshRequest.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetails.java
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── exception/
│   └── GlobalExceptionHandler.java
└── AuthServiceApplication.java
```