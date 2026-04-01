# Endpoints da API

Base URL (local): `http://localhost:8080`

## Autenticacao e autorizacao

- Publicos (sem token):
  - `POST /usuarios/create`
  - `POST /usuarios/login`
- Protegidos (exigem JWT):
  - Todos os demais endpoints
- Restricao por role:
  - `POST /usuarios/create-new-worker` exige `ROLE_ADM` (`@PreAuthorize("hasRole('ADM')")`)

## Usuarios (`/usuarios`)

- `POST /usuarios/create`
  - Body: `UsuarioCreateDTO`
  - Response: `201 Created` com `UsuarioResponseDTO`

- `POST /usuarios/login`
  - Body: `UsuarioLoginRequestDTO`
  - Response: `200 OK` com `UsuarioLoginResponseDTO` (token e dados do usuario)

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

- `DELETE /arquivos/{id}`
  - Path param: `id`
  - Response: `204 No Content` (soft delete)

- `POST /arquivos/restore/{id}`
  - Path param: `id`
  - Response: `200 OK`

## Exemplos rapidos

- Login (publico): `POST http://localhost:8080/usuarios/login`
- Criacao de usuario (publico): `POST http://localhost:8080/usuarios/create`
- Listar usuarios (protegido): `GET http://localhost:8080/usuarios/all`
- Upload de arquivo (protegido): `POST http://localhost:8080/arquivos/upload`
