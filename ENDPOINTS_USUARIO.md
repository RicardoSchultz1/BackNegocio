# Endpoints de Usuario

Base URL (local): `http://localhost:8080`

## 1. Criacao de Usuario

Endpoint:
- Metodo: `POST`
- Rota: `/usuarios/create`

Request JSON:
```json
{
  "nome": "Ricardo Silva",
  "email": "ricardo@empresa.com",
  "senha": "123456",
  "idEquipe": 1,
  "admSistema": true
}
```

Response JSON (201 Created):
```json
{
  "id": 10,
  "nome": "Ricardo Silva",
  "email": "ricardo@empresa.com",
  "dataCadastro": "2026-03-30",
  "idEquipe": 1,
  "admSistema": true
}
```

## 2. Login

Endpoint:
- Metodo: `POST`
- Rota: `/usuarios/login`

Request JSON:
```json
{
  "email": "ricardo@empresa.com",
  "senha": "123456"
}
```

Response JSON (200 OK):
```json
{
  "id": 10,
  "nome": "Ricardo Silva",
  "email": "ricardo@empresa.com",
  "idEquipe": 1,
  "admSistema": true
}
```

Response JSON (403 Forbidden - credenciais invalidas):
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Email ou senha invalidos",
  "timestamp": "2026-03-30T10:15:30"
}
```

Response JSON (403 Forbidden - usuario sem acesso):
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Usuario does not belong to this equipe",
  "timestamp": "2026-03-30T10:20:45"
}
```

## Exemplo rapido no Postman

- Criacao: `POST http://localhost:8080/usuarios/create`
- Login: `POST http://localhost:8080/usuarios/login`
- Autorizacao: `POST http://localhost:8080/usuarios/autorizacao`
- Header: `Content-Type: application/json`
