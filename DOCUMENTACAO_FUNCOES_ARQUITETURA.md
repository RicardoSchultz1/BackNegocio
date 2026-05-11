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
- [ADICIONADO] com.tcs.backnegocio.controller.IaSearchController

Servicos:
- com.tcs.backnegocio.service.UsuarioService
- com.tcs.backnegocio.service.EmpresaService
- com.tcs.backnegocio.service.EmpresaOnboardingService
- com.tcs.backnegocio.service.EquipeService
- com.tcs.backnegocio.service.EquipeAccessService
- com.tcs.backnegocio.service.FolderService
- com.tcs.backnegocio.service.ArquivoService
- com.tcs.backnegocio.storage.SupabaseStorageService
- [ADICIONADO] com.tcs.backnegocio.service.IaSearchService

## 3. Seguranca e Autorizacao

Regras atuais:
- Publico (sem token):
  - POST /usuarios/create
  - POST /usuarios/login
- [MODIFICADO] PUT /arquivos/{id}/status tambem e publico (sem token).
- [MODIFICADO] Autenticado (JWT obrigatorio): todos os demais endpoints.
- Regra de role:
  - POST /usuarios/create-new-worker exige role ADM.
  - POST /usuarios/add-to-equipe exige role ADM.
  - [ADICIONADO] POST /usuarios/sync-equipes exige role ADM.

Detalhes:
- O token JWT contem subject com email do usuario.
- [MODIFICADO] O filtro busca usuario por email apenas quando `ativo = true` e popula o contexto de seguranca.
- Quando admSistema = true, a autoridade ROLE_ADM e atribuida.
- Senhas sao armazenadas com BCrypt.

## 4. Modulos e Funcoes da Aplicacao

### 4.1 Usuarios

Responsavel por:
- Criacao de usuarios.
- Login com retorno de token JWT.
- Consulta e remocao de usuarios.
- Criacao de colaborador por administrador da empresa.
- [ADICIONADO] Sincronizacao em lote das equipes de um usuario.

Regras importantes:
- Senha sempre criptografada.
- [MODIFICADO] Login valida email/senha e gera token apenas para usuario ativo (`ativo = true`).
- create-new-worker impede criacao de usuario em equipe de outra empresa.
- add-to-equipe permite associar usuario existente a equipe existente (somente ADM).
- [ADICIONADO] sync-equipes substitui o conjunto de equipes do usuario com base na lista final enviada pelo front, aplicando adicoes/remocoes automaticamente.
- Usuario pode pertencer a varias equipes (`idsEquipes`).
- [MODIFICADO] Exclusao de usuario agora e soft delete via campo booleano `ativo` (`false` = inativo).

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
- [ADICIONADO] Endpoint `GET /equipes/{id}/funcionarios` retorna nomes de funcionarios da equipe em `EquipeFuncionariosResponseDTO`.

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
- [ADICIONADO] Atualizacao de status do documento (`PUT /arquivos/{id}/status`).

Regras importantes:
- Nao permite upload com nome vazio.
- Nao permite arquivo ativo duplicado no mesmo folder.
- Nao permite restaurar arquivo quando a pasta pai esta deletada.
- Acesso por equipe e validado com base no usuario autenticado.
- [MODIFICADO] Excecao: `PUT /arquivos/{id}/status` e endpoint publico (sem JWT).
- [ADICIONADO] Regra de negocio temporaria: apenas o arquivo com id 2 pode ter status alterado.

### 4.6 Busca IA

[ADICIONADO] Responsavel por:
- Integrar com API externa de busca semantica (`http://localhost:8001/search`).
- Receber requisicao de busca com descricao e limite de resultados.
- Repassar para API externa via RestTemplate.
- Retornar lista de documentos com scores de similaridade.

Fluxo:
1. Cliente faz POST /ia/search com `description` e `limit`.
2. IaSearchService chama HTTP POST para `http://localhost:8001/search`.
3. API externa retorna lista de documentos com metadados.
4. Response e retornado ao cliente com links de download.

Regras importantes:
- Endpoint requer autenticacao JWT (protegido).
- Chama API externa diretamente, sem intermediarias.
- Tratamento de erros retorna HTTP 502 (Bad Gateway) se API externa falhar.
- DTOs: IaSearchRequestDTO (entrada), IaSearchResponseDTO (saida).

### 4.7 Integracao de Storage

SupabaseStorageService:
- Faz upload via endpoint HTTP do Supabase Storage.
- Monta path por equipe e folder: equipe-{id}/folder-{id}/UUID-arquivo.
- Retorna URL publica para consumo do cliente.

IaSearchService:
- Faz chamada HTTP POST para API externa de busca.
- Serializa/desserializa payloads JSON com IaSearchRequestDTO e IaSearchResponseDTO.
- Trata excecoes de rede/timeout retornando erro padronizado via BusinessException.

## 5. Modelo de Dados

Entidades principais:
- Empresa: dados da empresa e referencia de administrador.
- Equipe: vinculada a uma empresa, com ids de adm e usuario.
- [MODIFICADO] Usuario: vinculado a uma ou mais equipes (N:N), com flags `admSistema` e `ativo`.
- Folder: estrutura hierarquica com parent, equipe, root e deleted.
- Arquivo: metadados do arquivo, vinculacao a folder e flag deleted.

Observacoes de modelagem:
- Folder e Arquivo usam deleted para soft delete.
- [ADICIONADO] Usuario usa `ativo` para soft delete logico (`true` ativo / `false` inativo).
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
- [ADICIONADO] POST /usuarios/sync-equipes

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
- [ADICIONADO] GET /equipes/{id}/funcionarios
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
- [ADICIONADO] PUT /arquivos/{id}/status

[ADICIONADO] Busca IA:
- POST /ia/search

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
- [ADICIONADO] ia.search.url (default: http://localhost:8001/search)
- configuracao de banco de dados

## 10. Observacoes e Evolucao Recomendada

Pontos de evolucao:
- Adicionar OpenAPI/Swagger para documentacao viva.
- Cobrir services com testes unitarios e cenarios de regras criticas.
- Adicionar auditoria de operacoes sensiveis (create/delete/restore/move/upload).
- Incluir conceito de equipe ativa no token/contexto para cenarios com multiplas equipes por usuario.
