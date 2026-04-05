# Endpoints da API

Base URL (local): `http://localhost:8081`

## Autenticacao e autorizacao

- Publicos (sem token):
  - `POST /usuarios/create`
  - `POST /usuarios/login`
- Protegidos (exigem JWT):
  - Todos os demais endpoints
- Restricao por role:
  - `POST /usuarios/create-new-worker` exige `ROLE_ADM` (`@PreAuthorize("hasRole('ADM')")`)
  - `POST /usuarios/add-to-equipe` exige `ROLE_ADM` (`@PreAuthorize("hasRole('ADM')")`)

## Usuarios (`/usuarios`)

- `POST /usuarios/create`
  - Body: `UsuarioCreateDTO`
  - Observacao: aceita `idEquipe` (legado) e `idsEquipes` (novo)
  - Response: `201 Created` com `UsuarioResponseDTO`

- `POST /usuarios/login`
  - Body: `UsuarioLoginRequestDTO`
  - Response: `200 OK` com `UsuarioLoginResponseDTO` (token, `idEquipe` legado e `idsEquipes`)

- `GET /usuarios/{id}`
  - Path param: `id` numerico (`{id:\d+}`)
  - Response: `200 OK` com `UsuarioResponseDTO`

- `GET /usuarios/all`
  - Response: `200 OK` com `List<UsuarioResponseDTO>`

- `DELETE /usuarios/{id}`
  - Path param: `id` numerico (`{id:\d+}`)
  - Response: `204 No Content`

- `POST /usuarios/create-new-worker`
  - Body: `UsuarioCreateDTO`
  - Requer usuario autenticado com role `ADM`
  - Response: `201 Created` com `UsuarioResponseDTO`

- `POST /usuarios/add-to-equipe`
  - Body: `UsuarioEquipeAssignDTO`
  - Requer usuario autenticado com role `ADM`
  - Regra: equipe de destino deve pertencer a empresa do ADM
  - Response: `200 OK` com `UsuarioResponseDTO`

## Empresas (`/empresas`)

- `POST /empresas/create`
  - Body: `EmpresaCreateRequestDTO`
  - Response: `201 Created` com `EmpresaResponseDTO`

- `GET /empresas/{id}`
  - Path param: `id`
  - Response: `200 OK` com `EmpresaResponseDTO`

- `GET /empresas/all`
  - Response: `200 OK` com `List<EmpresaResponseDTO>`

- `DELETE /empresas/{id}`
  - Path param: `id`
  - Response: `204 No Content`

## Equipes (`/equipes`)

- `POST /equipes/create`
  - Body: `EquipeCreateDTO`
  - Response: `201 Created` com `EquipeResponseDTO`

- `GET /equipes/{id}`
  - Path param: `id` numerico (`{id:\d+}`)
  - Response: `200 OK` com `EquipeResponseDTO`

- `GET /equipes/all`
  - Response: `200 OK` com `List<EquipeResponseDTO>`

- `DELETE /equipes/{id}`
  - Path param: `id` numerico (`{id:\d+}`)
  - Response: `204 No Content`

## Folders (`/folders`)

- `POST /folders/create`
  - Body: `FolderCreateDTO`
  - Response: `201 Created` com `FolderResponseDTO`

- `GET /folders/{id}`
  - Path param: `id`
  - Response: `200 OK` com `FolderResponseDTO`

- `GET /folders/tree/{folderId}`
  - Path param: `folderId`
  - Response: `200 OK` com `FolderTreeNodeDTO`

- `GET /folders/content/{folderId}`
  - Path param: `folderId`
  - Response: `200 OK` com `FolderContentDTO` (somente conteudo direto da pasta)

- `DELETE /folders/{id}`
  - Path param: `id`
  - Response: `204 No Content` (soft delete)

- `POST /folders/restore/{id}`
  - Path param: `id`
  - Response: `200 OK`

- `POST /folders/move`
  - Body: `FolderMoveDTO`
  - Response: `200 OK` com `FolderResponseDTO`

## Arquivos (`/arquivos`)

- `POST /arquivos/upload`
  - Content-Type: `multipart/form-data`
  - Form-data:
    - `folderId` (Integer)
    - `file` (MultipartFile)
  - Response: `201 Created` com `ArquivoUploadResponseDTO`

- `GET /arquivos/{id}`
  - Path param: `id`
  - Response: `200 OK` com `ArquivoResponseDTO`

- `GET /arquivos/by-folder/{folderId}`
  - Path param: `folderId`
  - Response: `200 OK` com `List<ArquivoResponseDTO>`

- `GET /arquivos/download/{id}`
  - Path param: `id`
  - Response: `200 OK` com binario do arquivo (`Content-Disposition: attachment`)

- `DELETE /arquivos/{id}`
  - Path param: `id`
  - Response: `204 No Content` (soft delete)

- `POST /arquivos/restore/{id}`
  - Path param: `id`
  - Response: `200 OK`

## Exemplos rapidos

- Login (publico): `POST http://localhost:8081/usuarios/login`
- Criacao de usuario (publico): `POST http://localhost:8081/usuarios/create`
- Listar usuarios (protegido): `GET http://localhost:8081/usuarios/all`
- Upload de arquivo (protegido): `POST http://localhost:8081/arquivos/upload`
- Download de arquivo (protegido): `GET http://localhost:8081/arquivos/download/{id}`

## Regras de acesso por equipe

- Usuario pode pertencer a varias equipes (`idsEquipes`)
- Equipe pode ter varios usuarios
- Endpoints de pastas/arquivos validam se o usuario autenticado pertence a equipe do recurso
- Sem pertencimento, a API retorna `403 Forbidden`

## Exemplos de JSON (POST e GET)

### Usuarios

POST `/usuarios/create` (request):
```json
{
  "nome": "Maria Silva",
  "email": "maria@empresa.com",
  "senha": "Senha@123",
  "idsEquipes": [1, 2],
  "admSistema": false
}
```

GET `/usuarios/10` (response):
```json
{
  "id": 10,
  "nome": "Maria Silva",
  "email": "maria@empresa.com",
  "dataCadastro": "2026-04-05",
  "idEquipe": 1,
  "idsEquipes": [1, 2],
  "admSistema": false
}
```

POST `/usuarios/create-new-worker` (request):
```json
{
  "nome": "Joao Lima",
  "email": "joao@empresa.com",
  "senha": "Senha@123",
  "idsEquipes": [2],
  "admSistema": false
}
```

POST `/usuarios/add-to-equipe` (request):
```json
{
  "usuarioId": 10,
  "equipeId": 3
}
```

POST `/usuarios/login` (request):
```json
{
  "email": "maria@empresa.com",
  "senha": "Senha@123"
}
```

POST `/usuarios/login` (response):
```json
{
  "id": 10,
  "nome": "Maria Silva",
  "email": "maria@empresa.com",
  "idEquipe": 1,
  "idsEquipes": [1, 2, 3],
  "admSistema": false,
  "token": "<jwt_token>"
}
```

### Empresas

POST `/empresas/create` (request):
```json
{
  "empresa": {
    "nome": "Empresa XPTO",
    "cnpj": "12345678000199"
  },
  "equipe": {
    "nomeEmpresa": "Equipe Comercial"
  },
  "usuario": {
    "nome": "Admin Empresa",
    "email": "admin@xpto.com",
    "senha": "Senha@123"
  }
}
```

GET `/empresas/1` (response):
```json
{
  "id": 1,
  "nome": "Empresa XPTO",
  "cnpj": "12345678000199",
  "dataCadastro": "2026-04-05",
  "idAdm": 7
}
```

### Equipes

POST `/equipes/create` (request):
```json
{
  "nomeEmpresa": "Equipe Financeiro",
  "idAdm": 7,
  "idUser": 10,
  "idEmpresa": 1
}
```

GET `/equipes/3` (response):
```json
{
  "id": 3,
  "nomeEmpresa": "Equipe Financeiro",
  "idAdm": 7,
  "idUser": 10,
  "idEmpresa": 1
}
```

### Folders

POST `/folders/create` (request):
```json
{
  "nome": "Contratos",
  "parentId": 1,
  "equipeId": 2
}
```

GET `/folders/5` (response):
```json
{
  "id": 5,
  "nome": "Contratos",
  "parentId": 1,
  "equipeId": 2,
  "isRoot": false,
  "deleted": false,
  "dataCriacao": "2026-04-05T18:20:10"
}
```

POST `/folders/move` (request):
```json
{
  "folderId": 5,
  "newParentId": 9
}
```

GET `/folders/content/1` (response):
```json
{
  "pasta": {
    "id": 1,
    "nome": "ROOT",
    "parentId": null,
    "equipeId": 2,
    "isRoot": true,
    "deleted": false,
    "dataCriacao": "2026-04-05T18:00:00"
  },
  "subpastas": [
    {
      "id": 5,
      "nome": "Contratos",
      "parentId": 1,
      "equipeId": 2,
      "isRoot": false,
      "deleted": false,
      "dataCriacao": "2026-04-05T18:20:10"
    }
  ],
  "arquivos": []
}
```

GET `/folders/tree/1` (response simplificado):
```json
{
  "id": 1,
  "nome": "ROOT",
  "parentId": null,
  "isRoot": true,
  "subfolders": [
    {
      "id": 5,
      "nome": "Contratos",
      "parentId": 1,
      "isRoot": false,
      "subfolders": [],
      "arquivos": []
    }
  ],
  "arquivos": []
}
```

### Arquivos

POST `/arquivos/upload` (request):
`multipart/form-data` (nao e JSON)
- `folderId`: `5`
- `file`: selecione o arquivo

GET `/arquivos/1` (response):
```json
{
  "id": 1,
  "nome": "contrato.pdf",
  "path": "equipe-2/folder-5/uuid-contrato.pdf",
  "tamanho": 245678,
  "tipo": "application/pdf",
  "folderId": 5,
  "deleted": false,
  "dataUpload": "2026-04-05T18:31:22"
}
```

GET `/arquivos/by-folder/5` (response):
```json
[
  {
    "id": 1,
    "nome": "contrato.pdf",
    "path": "equipe-2/folder-5/uuid-contrato.pdf",
    "tamanho": 245678,
    "tipo": "application/pdf",
    "folderId": 5,
    "deleted": false,
    "dataUpload": "2026-04-05T18:31:22"
  }
]
```

GET `/arquivos/download/1` (response):
- Retorna binario do arquivo
- Headers principais:
  - `Content-Type`: tipo do arquivo (ex.: `application/pdf`)
  - `Content-Disposition`: `attachment; filename="contrato.pdf"`
