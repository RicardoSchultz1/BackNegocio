# Prompt de Contexto: Arquitetura do Sistema BackNegocio

Use este documento como contexto ao explicar o código para outra IA.

---

## 1. Visão Geral do Sistema

**BackNegocio** é uma API REST em Spring Boot para gestão de empresas, equipes, usuários, pastas hierárquicas e arquivos com armazenamento em Supabase.

**Capacidades principais:**
- Autenticação e autorização com JWT
- Onboarding de empresas com criação de equipe e administrador
- Gestão de equipes vinculadas a empresas
- Estrutura hierárquica de pastas (árvore)
- Upload e download de arquivos
- Controle de acesso por equipe com herança para ADM da empresa

**Base URL:** `http://localhost:8081`

---

## 2. Arquitetura em Camadas

```
┌─────────────────────────────────────────────┐
│         HTTP Clients (Frontend)             │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│     JwtAuthenticationFilter (Security)      │
│        - Intercepta requisições              │
│        - Valida token JWT                    │
│        - Popula SecurityContext             │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         Controllers                         │
│  - UsuarioController                        │
│  - EmpresaController                        │
│  - EquipeController                         │
│  - FolderController                         │
│  - ArquivoController                        │
│ (Validação, conversão de DTO, roteamento)  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         Services (Lógica de Negócio)       │
│  - UsuarioService                           │
│  - EmpresaService                           │
│  - EmpresaOnboardingService                 │
│  - EquipeService                            │
│  - EquipeAccessService                      │
│  - FolderService                            │
│  - ArquivoService                           │
│  (Regras, autorização, orquestração)       │
└──────────────────┬──────────────────────────┘
                   │
         ┌─────────┼─────────┐
         │         │         │
    ┌────▼───┐ ┌───▼────┐ ┌─▼──────────┐
    │Reposit │ │Storage │ │Exceptions  │
    │ories   │ │Service │ │Handler     │
    │(JPA)   │ │        │ │            │
    └────┬───┘ └───┬────┘ └─┬──────────┘
         │         │        │
    ┌────▼─────────▼────────▼┐
    │      Banco de Dados    │
    │      + Supabase        │
    │      Storage           │
    └────────────────────────┘
```

---

## 3. Entidades Principais e Relacionamentos

```
┌─────────────────────────────────────────────────────────────┐
│ EMPRESA (1)                                                 │
│ ├─ id (PK)                                                  │
│ ├─ nome                                                     │
│ ├─ cnpj                                                     │
│ ├─ dataCadastro                                             │
│ ├─ idAdm (FK → Usuario.id)                                  │
│ └─ equipes[] (1:N → Equipe)                                 │
└─────────────────────────────────────────────────────────────┘
              │
              │ 1:N
              ▼
┌─────────────────────────────────────────────────────────────┐
│ EQUIPE (N da empresa, 1 de Usuario_Equipe)                  │
│ ├─ id (PK)                                                  │
│ ├─ nomeEmpresa                                              │
│ ├─ idAdm (Usuario criador/admin da equipe)                  │
│ ├─ idUser (Usuario opcional)                                │
│ ├─ empresa (FK → Empresa.id)                                │
│ ├─ usuarios[] (N:N → Usuario)                               │
│ └─ folders[] (1:N → Folder)                                 │
└─────────────────────────────────────────────────────────────┘
              │
       ┌──────┴──────┐
    N:N│             │1:N
       │             │
       ▼             ▼
┌──────────────┐  ┌─────────────────────────────────────────┐
│ USUARIO      │  │ FOLDER                                  │
│              │  │ ├─ id (PK)                              │
│ ├─ id (PK)   │  │ ├─ nome                                 │
│ ├─ nome      │  │ ├─ parent (FK → Folder.id, nullable)    │
│ ├─ email     │  │ ├─ equipe (FK → Equipe.id)              │
│ ├─ senha     │  │ ├─ isRoot (boolean)                     │
│ ├─ dataCad.  │  │ ├─ deleted (soft delete)                │
│ ├─ admSistema│  │ └─ dataCriacao                          │
│ └─ equipes[] │  └─────────────────────────────────────────┘
│    (N:N)     │              │
└──────────────┘           1:N│
                              │
                              ▼
                  ┌─────────────────────────────┐
                  │ ARQUIVO                     │
                  │ ├─ id (PK)                  │
                  │ ├─ nome                     │
                  │ ├─ path (Supabase URL)      │
                  │ ├─ tamanho                  │
                  │ ├─ tipo (MIME)              │
                  │ ├─ folder (FK → Folder.id)  │
                  │ ├─ deleted (soft delete)    │
                  │ └─ dataUpload               │
                  └─────────────────────────────┘
```

---

## 4. Fluxo de Requisição (Exemplo: Criar Pasta)

```
1. Cliente faz: POST /folders/create
   {
     "nome": "Contratos",
     "equipeId": 2,
     "parentId": 1
   }

2. JwtAuthenticationFilter intercepta
   └─> Extrai token JWT
   └─> Valida e obtém email do subject
   └─> Busca usuário no banco por email
   └─> Popula SecurityContext

3. FolderController.create(...) recebe DTO
   └─> Valida @Valid FolderCreateDTO
   └─> Delega para FolderService.create(dto)

4. FolderService.create(dto)
   └─> EquipeAccessService.validateCurrentUserAccess(equipeId)
       ├─> Obtém usuário autenticado
       ├─> Verifica vínculo direto + herança de ADM da empresa
       └─> Lança UnauthorizedException se sem acesso
   └─> Valida nome duplicado via FolderRepository
   └─> Obtém pasta pai
   └─> Cria objeto Folder
   └─> Persiste via FolderRepository.save()
   └─> Retorna FolderResponseDTO

5. Controller retorna ResponseEntity (201 Created)

6. Erro? GlobalExceptionHandler traduz para ApiError
```

---

## 5. Classes e Responsabilidades

### 5.1 Controladores

| Classe | Responsabilidade |
|--------|------------------|
| `UsuarioController` | POST/GET/DELETE de usuários, login, create-new-worker, add-to-equipe |
| `EmpresaController` | POST/GET/DELETE de empresas (CRUD básico) |
| `EquipeController` | POST/GET/DELETE de equipes, findAccessible() |
| `FolderController` | CRUD de pastas, tree, content, move, restore, soft delete |
| `ArquivoController` | Upload, download, CRUD de arquivos |

### 5.2 Serviços

| Classe | Responsabilidade |
|--------|------------------|
| `UsuarioService` | Lógica de usuário: criação, login, BCrypt, validação |
| `EmpresaService` | CRUD de empresa |
| `EmpresaOnboardingService` | Transação: cria empresa + equipe + admin em uma chamada |
| `EquipeService` | CRUD de equipe, findAccessible() com acesso herdado |
| `EquipeAccessService` | **Central de autorização:** valida acesso por equipe considerando vínculo direto + ADM da empresa |
| `FolderService` | CRUD de pasta, árvore, restauração, validações hierárquicas |
| `ArquivoService` | Upload/download, CRUD, soft delete, restauração |
| `SupabaseStorageService` | Integração HTTP com Supabase Storage para upload/download |

### 5.3 Repositórios

| Interface | Método Importante |
|-----------|-------------------|
| `UsuarioRepository` | findByEmail, findWithEquipesById, findWithEquipesByEmail |
| `EmpresaRepository` | findAllByIdAdm (retorna empresas onde usuário é ADM) |
| `EquipeRepository` | findByEmpresaIdIn, findDistinctByUsuariosId |
| `FolderRepository` | findActiveTree (query recursiva CTE), isInSubtree, markSubtreeDeleted |
| `ArquivoRepository` | findAllActiveInFolderTree, markDeletedByFolderSubtree |

### 5.4 Entidades (JPA)

| Classe | Anotações Chave |
|--------|-----------------|
| `Usuario` | @ManyToMany equipes, admSistema, email unique implícito |
| `Empresa` | @OneToMany equipes, idAdm (referência Usuario) |
| `Equipe` | @ManyToOne empresa, @ManyToMany usuarios, idAdm |
| `Folder` | @ManyToOne parent (nullable), @ManyToOne equipe, isRoot, deleted |
| `Arquivo` | @ManyToOne folder, deleted, nome+folder unique implícito |

### 5.5 DTO (Conversão)

| DTO | Uso |
|-----|-----|
| `UsuarioCreateDTO` | Request POST /usuarios/create e create-new-worker |
| `UsuarioLoginRequestDTO` | Request POST /usuarios/login |
| `UsuarioResponseDTO` | Response de consulta/login (sem senha) |
| `EmpresaCreateRequestDTO` | Request onboarding (empresa + equipe + usuario) |
| `EquipeCreateDTO` | Request POST /equipes/create |
| `FolderCreateDTO` | Request POST /folders/create |
| `FolderResponseDTO` | Response pasta individual |
| `FolderTreeNodeDTO` | Response arvore de pastas/arquivos |
| `FolderContentDTO` | Response conteúdo direto (subpastas + arquivos) |
| `ArquivoUploadResponseDTO` | Response POST /arquivos/upload |

### 5.6 Segurança

| Classe | Responsabilidade |
|--------|------------------|
| `SecurityConfig` | Configuração de autorização (públicos, autenticados, roles) |
| `JwtAuthenticationFilter` | Filtro que intercepta, valida token, popula contexto |
| `JwtUtil` | Geração e parsing de JWT |
| `GlobalExceptionHandler` | Traduz exceções em ApiError com status HTTP |

---

## 6. Regras de Negócio Críticas

### 6.1 Autenticação e Autorização

- **Publicos:** `POST /usuarios/create`, `POST /usuarios/login`
- **Autenticados:** Todos os demais (JWT obrigatório)
- **Roles:** `ROLE_ADM` atribuída via `usuario.admSistema == true`

### 6.2 Acesso a Equipes e Pastas (Regra Central)

Quando usuário solicita acesso a equipe/pasta:

1. **Vínculo Direto:** Usuario está em `usuario.equipes` (N:M com Equipe)?
   - SIM → Acesso garantido
   - NÃO → Ir para 2

2. **Herança de ADM da Empresa:** Usuario é ADM de alguma empresa que possui a equipe?
   - Consulta: `empresaRepository.findAllByIdAdm(usuario.id)` → Lista de empresas
   - Para cada empresa, obtém equipes: `equipeRepository.findByEmpresaIdIn(empresaIds)`
   - Se equipe requisitada está nessa lista → Acesso garantido
   - NÃO → Lança `UnauthorizedException`

**Implementação centralizada em:**
- `EquipeAccessService.validateCurrentUserAccess(equipeId)`
- `EquipeAccessService.getAccessibleEquipeIdsForCurrentUser()`

### 6.3 Pastas (Folders)

- Cada equipe tem uma pasta raiz (`isRoot = true`)
- Pasta raiz criada automaticamente ao criar equipe (`FolderService.ensureRootFolderInternal`)
- Raiz não pode ser movida nem deletada
- Soft delete propaga para subárvore via CTE recursiva
- Restauração valida se parent não está deletado
- Mover valida se não move para dentro de própria subárvore

### 6.4 Arquivos

- Upload via `SupabaseStorageService` com path: `equipe-{id}/folder-{id}/UUID-arquivo`
- Soft delete propaga para subárvore
- Não permite duplicatas ativas (nome + folder + deleted=false)
- Download retorna arquivo binário com `Content-Disposition: attachment`

### 6.5 Onboarding (Empresa)

Transação única cria:
1. Empresa
2. Equipe inicial para empresa
3. Usuario administrador
4. Vincula admin à equipe
5. Atualiza empresa.idAdm
6. Atualiza equipe.idAdm

---

## 7. Endpoints Principais

### Usuários
```
POST   /usuarios/create                    [Public]
POST   /usuarios/login                     [Public]
GET    /usuarios/{id}                      [Auth]
GET    /usuarios/all                       [Auth]
DELETE /usuarios/{id}                      [Auth]
POST   /usuarios/create-new-worker         [Auth + ROLE_ADM]
POST   /usuarios/add-to-equipe             [Auth + ROLE_ADM]
```

### Empresas
```
POST   /empresas/create                    [Auth]
GET    /empresas/{id}                      [Auth]
GET    /empresas/all                       [Auth]
DELETE /empresas/{id}                      [Auth]
```

### Equipes
```
POST   /equipes/create                     [Auth]
GET    /equipes/{id}                       [Auth]
GET    /equipes/all                        [Auth]
GET    /equipes/access                     [Auth] ⭐ Retorna equipes acessíveis (direto + ADM)
DELETE /equipes/{id}                       [Auth]
```

### Pastas
```
GET    /folders/roots                      [Auth] ⭐ Roots de equipes acessíveis
POST   /folders/create                     [Auth]
GET    /folders/{id}                       [Auth]
GET    /folders/tree/{folderId}            [Auth]
GET    /folders/content/{folderId}         [Auth]
DELETE /folders/{id}                       [Auth] (soft delete)
POST   /folders/restore/{id}               [Auth]
POST   /folders/move                       [Auth]
```

### Arquivos
```
POST   /arquivos/upload                    [Auth]
GET    /arquivos/{id}                      [Auth]
GET    /arquivos/by-folder/{folderId}      [Auth]
GET    /arquivos/download/{id}             [Auth]
DELETE /arquivos/{id}                      [Auth] (soft delete)
POST   /arquivos/restore/{id}              [Auth]
```

---

## 8. Configurações Importantes

### application.properties (privado)
```properties
jwt.secret = <chave secreta>
jwt.expiration = 86400000 (24h)
supabase.url = https://...supabase.co
supabase.api-key = <api-key>
supabase.storage.bucket = arquivos
spring.datasource.url = <db-url>
spring.datasource.username = <db-user>
spring.datasource.password = <db-pass>
spring.jpa.hibernate.ddl-auto = validate
```

### Dependências de Destaque
- Spring Web 3.x
- Spring Data JPA
- Spring Security + JJWT
- Lombok
- Bean Validation
- H2 ou PostgreSQL

---

## 9. Padrões e Convenções

| Aspecto | Padrão |
|---------|--------|
| Validação | `@Valid` em DTO, Bean Validation |
| Tratamento de Erro | GlobalExceptionHandler → ApiError (DTO padronizado) |
| Soft Delete | Campo `deleted` (boolean), queries filtram `deleted = false` |
| Transações | `@Transactional` em metodos Service que alteram dados |
| Autorização | Centralizada em `EquipeAccessService` |
| Repos com Queries Complexas | SQL nativo com CTE para operações de árvore |
| Relacionamento N:M | Usuario ↔ Equipe com tabela `usuario_equipe` |
| DTOs | Separadas por domínio (usuario/, equipe/, folder/, arquivo/) |

---

## 10. Fluxo Típico: Criar Pasta em Equipe

```java
// 1. Cliente envia
{
  "nome": "Contratos",
  "equipeId": 2,
  "parentId": 1
}

// 2. FolderController valida e chama
folderService.create(dto);

// 3. FolderService faz:
equipeAccessService.validateCurrentUserAccess(2);
// → Verifica: usuario está em equipe 2? Ou é ADM de empresa com equipe 2?

Folder parent = getActiveFolderOrThrow(1);
// → Valida pasta pai existe e usuario acessa equipe 2

folderRepository.existsActiveByNameAndParentAndEquipe("Contratos", 1, 2)
// → Valida nome não duplicado

Folder folder = Folder.builder()...build();
folderRepository.save(folder);

// 4. Retorna FolderResponseDTO
```

---

## 11. Ponto de Extensão: Como Usar Este Prompt

Quando quiser explicar código a outra IA, use assim:

**Exemplo 1:**
> "Estou trabalhando com BackNegocio (veja contexto em anexo). Preciso entender por que `EquipeAccessService.validateCurrentUserAccess()` verifica empresa.idAdm. Como é o fluxo de autorização?"

**Exemplo 2:**
> "Tenho um bug em FolderService. Quando ADM da empresa cria pasta, parece não ter acesso. Qual é a regra esperada? (Contexto: BackNegocio, veja prompt.md)"

**Exemplo 3:**
> "Quero adicionar um novo endpoint que lista arquivos do ADM em todas as equipes. Por onde começo? Que classes preciso tocar?"

---

## 12. Resumo de Conceitos-Chave

| Conceito | Definição |
|----------|-----------|
| **Equipe** | Unidade de trabalho dentro de empresa, vincula usuarios N:M, possui uma pasta raiz |
| **Empresa** | Agrupa equipes, tem um administrador (idAdm) |
| **ADM da Empresa** | Usuario com `empresa.idAdm == usuario.id`, herda acesso a todas as equipes/pastas da empresa |
| **Usuario Comum** | Acesso somente a equipes/pastas em que está vinculado diretamente |
| **Pasta Raiz** | Criada automaticamente por equipe, não pode ser movida/deletada |
| **Soft Delete** | Campo `deleted = true`, registros não aparecem em queries, restauração possível |
| **CTE Recursiva** | Query SQL para operar árvore de pastas (findActiveTree, isInSubtree, markSubtreeDeleted) |
| **JWT** | Token stateless com email no subject, validado por JwtAuthenticationFilter |
| **Supabase Storage** | Repositório externo de arquivos, integrado via HTTP |

---

## 13. Comandos Úteis

```bash
# Compilar sem testes
./mvnw.cmd -q -DskipTests compile

# Rodar aplicação
./mvnw.cmd spring-boot:run

# Correr testes
./mvnw.cmd test

# Gerar Docker image (baseado em Dockerfile existente)
docker build -t backnegocio:latest .

# Login local (exemplo)
curl -X POST http://localhost:8081/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","senha":"Senha@123"}'

# Listar equipes acessíveis (com token)
curl -X GET http://localhost:8081/equipes/access \
  -H "Authorization: Bearer <token>"
```

---

## 14. Documentos Relacionados

- `ENDPOINTS_USUARIO.md` – Lista completa de endpoints com exemplos JSON
- `DOCUMENTACAO_FUNCOES_ARQUITETURA.md` – Detalhe técnico de arquitetura e regras
- `Dockerfile` – Container setup (Spring Boot, JDK, exposição porta 8081)
- `pom.xml` – Dependências Maven

---

**Criado em:** 2026-04-13  
**Versão:** 1.0 (com regra de ADM da empresa)
