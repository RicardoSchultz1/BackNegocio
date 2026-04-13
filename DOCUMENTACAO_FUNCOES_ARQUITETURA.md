# Documentacao de Funcoes e Arquitetura - BackNegocio

Data: 2026-04-05
Base URL local: http://localhost:8081

## 1. Visao Geral

A aplicacao BackNegocio e uma API REST em Spring Boot para gestao de empresas, equipes, usuarios, estrutura de pastas e arquivos.

Principais capacidades:
- Cadastro e autenticacao de usuarios com JWT.
- Onboarding de empresa com criacao inicial de equipe e administrador.
- Gestao de equipes.
- Gestao hierarquica de pastas (arvore), incluindo mover, excluir logico e restaurar.
- Upload e gestao de arquivos com armazenamento em Supabase.

## 2. Arquitetura

### 2.1 Estilo arquitetural

Arquitetura em camadas:
- Controller: exposicao de endpoints HTTP.
- Service: regras de negocio e orquestracao.
- Repository: acesso a dados via Spring Data JPA e queries SQL nativas.
- Entity: modelo de dados persistente.
- DTO: contratos de entrada e saida da API.
- Security: autenticacao/autorizacao com JWT e Spring Security.
- Exception: tratamento padronizado de erros.
- Storage: integracao com Supabase Storage.

### 2.2 Fluxo de requisicao

1. Cliente chama endpoint REST.
2. JwtAuthenticationFilter valida token quando presente.
3. SecurityConfig define acesso publico ou autenticado.
4. Controller valida payload e delega para Service.
5. Service aplica regras de negocio e acessa Repository/Storage.
6. Resposta retorna como DTO.
7. Erros sao traduzidos por GlobalExceptionHandler.

### 2.3 Componentes principais

Aplicacao:
- com.tcs.backnegocio.BackNegocioApplication

Seguranca:
- com.tcs.backnegocio.security.SecurityConfig
- com.tcs.backnegocio.security.JwtAuthenticationFilter
- com.tcs.backnegocio.security.JwtUtil

Controladores:
- com.tcs.backnegocio.controller.UsuarioController
- com.tcs.backnegocio.controller.EmpresaController
- com.tcs.backnegocio.controller.EquipeController
- com.tcs.backnegocio.controller.FolderController
- com.tcs.backnegocio.controller.ArquivoController

Servicos:
- com.tcs.backnegocio.service.UsuarioService
- com.tcs.backnegocio.service.EmpresaService
- com.tcs.backnegocio.service.EmpresaOnboardingService
- com.tcs.backnegocio.service.EquipeService
- com.tcs.backnegocio.service.EquipeAccessService
- com.tcs.backnegocio.service.FolderService
- com.tcs.backnegocio.service.ArquivoService
- com.tcs.backnegocio.storage.SupabaseStorageService

## 3. Seguranca e Autorizacao

Regras atuais:
- Publico (sem token):
  - POST /usuarios/create
  - POST /usuarios/login
- Autenticado (JWT obrigatorio): todos os demais endpoints.
- Regra de role:
  - POST /usuarios/create-new-worker exige role ADM.
  - POST /usuarios/add-to-equipe exige role ADM.

Detalhes:
- O token JWT contem subject com email do usuario.
- O filtro busca usuario por email e popula o contexto de seguranca.
- Quando admSistema = true, a autoridade ROLE_ADM e atribuida.
- Senhas sao armazenadas com BCrypt.

## 4. Modulos e Funcoes da Aplicacao

### 4.1 Usuarios

Responsavel por:
- Criacao de usuarios.
- Login com retorno de token JWT.
- Consulta e remocao de usuarios.
- Criacao de colaborador por administrador da empresa.

Regras importantes:
- Senha sempre criptografada.
- Login valida email/senha e gera token.
- create-new-worker impede criacao de usuario em equipe de outra empresa.
- add-to-equipe permite associar usuario existente a equipe existente (somente ADM).
- Usuario pode pertencer a varias equipes (`idsEquipes`).

### 4.2 Empresas

Responsavel por:
- Cadastro de empresa.
- Consulta e remocao de empresa.

Onboarding (EmpresaOnboardingService):
- Cria empresa, equipe inicial e usuario administrador em transacao unica.
- Atualiza referencias de idAdm em empresa e equipe.

### 4.3 Equipes

Responsavel por:
- Criacao de equipe vinculada a empresa.
- Consulta e remocao de equipe.

Regras importantes:
- Ao criar equipe, o sistema garante uma pasta raiz (root) para organizacao de arquivos.
- Endpoint `GET /equipes/access` aplica regra de acesso por equipe:
  - Usuario comum: somente equipes vinculadas diretamente.
  - ADM da empresa: todas as equipes das empresas em que e administrador (`empresa.idAdm = usuario.id`).

### 4.4 Folders (Pastas)

Responsavel por:
- Criar pasta.
- Consultar pasta por id.
- Obter arvore de pastas e arquivos.
- Obter somente conteudo direto da pasta (`/folders/content/{folderId}`).
- Excluir logicamente subarvore de pastas.
- Restaurar subarvore.
- Mover pasta para novo parent.

Regras importantes:
- Root folder nao pode ser removida nem movida.
- Nao permite nomes duplicados no mesmo parent/equipe para itens ativos.
- Nao permite mover pasta para dentro da propria subarvore.
- Exclusao e restauracao propagam para arquivos na subarvore.
- Na criacao de pasta filha, a equipe e inferida automaticamente pela pasta pai (nao precisa enviar equipeId).
- Acesso por equipe e validado com base no usuario autenticado, com heranca para ADM da empresa.
- Endpoint `GET /folders/roots` retorna roots considerando a mesma regra de acesso (vinculo direto + heranca de ADM da empresa).

### 4.5 Arquivos

Responsavel por:
- Upload de arquivo para Supabase.
- Consulta por id.
- Listagem por pasta.
- Download de arquivo (`/arquivos/download/{id}`).
- Exclusao logica e restauracao.

Regras importantes:
- Nao permite upload com nome vazio.
- Nao permite arquivo ativo duplicado no mesmo folder.
- Nao permite restaurar arquivo quando a pasta pai esta deletada.
- Acesso por equipe e validado com base no usuario autenticado.

### 4.6 Integracao de Storage

SupabaseStorageService:
- Faz upload via endpoint HTTP do Supabase Storage.
- Monta path por equipe e folder: equipe-{id}/folder-{id}/UUID-arquivo.
- Retorna URL publica para consumo do cliente.

## 5. Modelo de Dados

Entidades principais:
- Empresa: dados da empresa e referencia de administrador.
- Equipe: vinculada a uma empresa, com ids de adm e usuario.
- Usuario: vinculado a uma ou mais equipes (N:N), com flag admSistema.
- Folder: estrutura hierarquica com parent, equipe, root e deleted.
- Arquivo: metadados do arquivo, vinculacao a folder e flag deleted.

Observacoes de modelagem:
- Folder e Arquivo usam deleted para soft delete.
- Existe restricao unica para pasta por nome+parent+equipe.
- Existe restricao unica para arquivo por nome+folder.
- Relacao Usuario <-> Equipe e N:N com tabela de juncao `usuario_equipe`.

## 6. Repositorios e Persistencia

- Repositorios baseados em JpaRepository.
- FolderRepository e ArquivoRepository contem queries nativas com CTE recursiva para:
  - montar arvore;
  - obter subarvore;
  - marcar subarvore/arquivos como deleted;
  - validar relacao de ancestry.

Beneficios:
- Operacoes de arvore mais eficientes no banco.
- Regras de consistencia aplicadas em lote.

## 7. Tratamento de Erros

GlobalExceptionHandler padroniza respostas de erro com ApiError:
- 400: validacao e regras de negocio (BusinessException default).
- 403: UnauthorizedException.
- 404: ResourceNotFoundException.
- 404: NoResourceFoundException (rota/recurso estatico nao encontrado).
- 409: conflitos de integridade.
- 500: excecoes nao tratadas.

Campos de erro:
- status
- error
- message
- timestamp
- details (quando houver validacao)

## 8. Endpoints por Contexto Funcional

Usuarios:
- POST /usuarios/create
- POST /usuarios/login
- GET /usuarios/{id}
- GET /usuarios/all
- DELETE /usuarios/{id}
- POST /usuarios/create-new-worker
- POST /usuarios/add-to-equipe

Empresas:
- POST /empresas/create
- GET /empresas/{id}
- GET /empresas/all
- DELETE /empresas/{id}

Equipes:
- POST /equipes/create
- GET /equipes/{id}
- GET /equipes/all
- GET /equipes/access
- DELETE /equipes/{id}

Folders:
- GET /folders/roots
- POST /folders/create
- GET /folders/{id}
- GET /folders/tree/{folderId}
- GET /folders/content/{folderId}
- DELETE /folders/{id}
- POST /folders/restore/{id}
- POST /folders/move

Arquivos:
- POST /arquivos/upload
- GET /arquivos/{id}
- GET /arquivos/by-folder/{folderId}
- GET /arquivos/download/{id}
- DELETE /arquivos/{id}
- POST /arquivos/restore/{id}

## 9. Dependencias Externas e Configuracoes

Dependencias de destaque:
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- Lombok
- JJWT

Configuracoes em application.properties:
- jwt.secret
- jwt.expiration
- supabase.url
- supabase.api-key
- supabase.storage.bucket
- configuracao de banco de dados

## 10. Observacoes e Evolucao Recomendada

Pontos de evolucao:
- Adicionar OpenAPI/Swagger para documentacao viva.
- Cobrir services com testes unitarios e cenarios de regras criticas.
- Adicionar auditoria de operacoes sensiveis (create/delete/restore/move/upload).
- Incluir conceito de equipe ativa no token/contexto para cenarios com multiplas equipes por usuario.
