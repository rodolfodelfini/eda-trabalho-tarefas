# Sistema de Gestão de Tarefas — EDA com Spring Boot e RabbitMQ

Exercício prático da disciplina Sistemas Baseados em Eventos e Mensageria Distribuída (Pós-Graduação em Arquitetura de Sistemas Escaláveis).

São três serviços Spring Boot que se comunicam só por eventos no RabbitMQ. O `task-service` cria e atualiza tarefas e publica os eventos; o `notification-service` registra notificações (simuladas) e o `analytics-service` mantém contagens diárias por prioridade. Cada serviço tem o seu próprio banco H2.

A arquitetura, os eventos e as decisões técnicas estão descritos em [documentacao/arquitetura.md](documentacao/arquitetura.md).

## Estrutura

```
task-management-system/
├── task-service/           porta 8081 - produtor (API de tarefas)
├── notification-service/   porta 8082 - consumidor (notificações)
├── analytics-service/      porta 8083 - consumidor (métricas)
├── documentacao/
└── docker-compose.yml      RabbitMQ
```

## Pré-requisitos

- Docker com Docker Compose (Docker Desktop, ou Docker Engine no WSL2)
- JDK 21
- Portas livres: 5672, 15672, 8081, 8082 e 8083

O Maven não precisa estar instalado: cada serviço tem o Maven Wrapper (`mvnw` / `mvnw.cmd`).

Dependências principais (iguais nos três serviços): `spring-boot-starter-web`, `spring-boot-starter-amqp`, `spring-boot-starter-data-jpa` e `h2`, sobre o Spring Boot 3.3.5.

## Como executar

### 1. Subir o RabbitMQ

Na raiz do projeto:

```bash
docker compose up -d
docker compose ps
```

Espere o status ficar `healthy`. O painel fica em http://localhost:15672 (usuário `admin`, senha `admin`).

### 2. Subir os serviços

Os consumidores precisam subir antes do produtor, porque são eles que criam as filas e os bindings. Use um terminal para cada serviço, nesta ordem:

```bash
cd analytics-service
./mvnw spring-boot:run
```

```bash
cd notification-service
./mvnw spring-boot:run
```

```bash
cd task-service
./mvnw spring-boot:run
```

No Windows (PowerShell), troque `./mvnw` por `.\mvnw.cmd`.

Cada serviço está pronto quando aparece no log a linha `Started ...Application`.

## Testando

Os exemplos usam `curl` (bash, Git Bash ou WSL). No PowerShell, o `curl` é um alias de outro comando; use `curl.exe` ou o `Invoke-RestMethod`.

### Criar tarefas

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Implementar EDA", "description": "Teste do fluxo", "assignee": "Joao Silva", "priority": "HIGH"}'

curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Servidor fora do ar", "assignee": "NOC", "priority": "CRITICAL"}'
```

Nos logs:

- `task-service`: `Tarefa criada: ID 1` e `Evento TASK_CREATED publicado para tarefa 1`
- `notification-service`: `recebeu TASK_CREATED` e `Canal: EMAIL` (ou `SMS` para a crítica)
- `analytics-service`: `Metrica atualizada: ... | Total: 2 | Criticas: 1`

### Consultar

```bash
curl http://localhost:8081/api/tasks
curl http://localhost:8082/api/notifications
curl http://localhost:8083/api/metrics/today
```

### Atualizar uma tarefa

```bash
curl -X PUT http://localhost:8081/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

Só o `notification-service` recebe esse evento: a `analytics.queue` não tem binding para `task.updated`.

### Validação

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"priority": "LOW"}'
```

Retorna `400` com `"detail": "Título da tarefa é obrigatório"`, e nenhum evento é publicado.

### No painel do RabbitMQ

- Exchanges: `task.exchange` (topic) e `task.dlx` (direct). Em `task.exchange`, a seção Bindings mostra as três ligações.
- Queues: `notification.queue`, `analytics.queue` e `notification.dlq`.

### Consumidor fora do ar

Pare o `notification-service` (Ctrl+C), crie algumas tarefas e veja em Queues que a `notification.queue` acumula mensagens em Ready. O `task-service` continua respondendo normalmente. Ao subir o serviço de novo, ele consome tudo o que ficou pendente.

### Retry e DLQ

Para provocar uma falha real no consumidor, publique pelo painel um evento cujo título passe de 255 caracteres (o limite da coluna `task_title` no banco do notification). Em Exchanges > `task.exchange` > Publish message:

- Routing key: `task.updated`
- Headers: `__TypeId__` = `com.example.task.event.TaskUpdatedEvent`
- Properties: `content_type` = `application/json`
- Payload: `{"eventId":"falha-1","eventType":"TASK_UPDATED","taskId":998,"title":"<mais de 255 caracteres>","priority":"LOW","status":"DONE"}`

O log do notification mostra `recebeu TASK_UPDATED: ID 998` três vezes (com 1s e 2s de intervalo) e depois `Retries exhausted`. A mensagem aparece na `notification.dlq`, onde dá para inspecioná-la com Get messages.

Foi usada a routing key `task.updated` para o teste não mexer nas métricas do analytics.

### Idempotência

No mesmo Publish message, com a routing key `task.created` e o header `__TypeId__` = `com.example.task.event.TaskCreatedEvent`, publique duas vezes o mesmo payload:

```json
{"eventId":"teste-dup-1","eventType":"TASK_CREATED","taskId":500,"title":"Duplicada","priority":"LOW"}
```

Em `/api/metrics/today`, o `lowPriorityTasks` sobe só 1, e o log do analytics mostra `Evento teste-dup-1 ja processado anteriormente`.

### Bancos

Cada serviço tem o console do H2 em `/h2-console` (por exemplo, http://localhost:8081/h2-console). Use como JDBC URL a mesma do `application.properties` (`jdbc:h2:file:./data/taskdb`, `.../notificationdb` ou `.../analyticsdb`), usuário `sa` e senha em branco.

## Problemas comuns

**`ClassNotFoundException` na classe principal ao rodar `spring-boot:run` no Windows.** Isso acontece quando o caminho da pasta tem acentos (`Área de Trabalho`, `Pós`...). A forma mais simples de contornar é criar uma junction (um atalho de pasta) com caminho sem acentos e rodar a partir dela. Os arquivos continuam no lugar original:

```powershell
mkdir C:\dev
cmd /c mklink /J C:\dev\task-management-system "<caminho atual do projeto>"
cd C:\dev\task-management-system\analytics-service
.\mvnw.cmd spring-boot:run
```

Outra opção é rodar pelos JARs, sempre de dentro da pasta de cada serviço (o H2 grava em `./data` relativo ao diretório atual):

```powershell
.\mvnw.cmd package -DskipTests
cd analytics-service;    java -jar target\analytics-service-1.0.0-SNAPSHOT.jar
cd notification-service; java -jar target\notification-service-1.0.0-SNAPSHOT.jar
cd task-service;         java -jar target\task-service-1.0.0-SNAPSHOT.jar
```

**`PRECONDITION_FAILED - inequivalent arg 'x-dead-letter-exchange'` no log do notification.** O volume do RabbitMQ guardou uma `notification.queue` antiga, criada sem a DLX. Recrie o broker:

```bash
docker compose down -v
docker compose up -d
```

**`Connection refused` na porta 5672.** O RabbitMQ não está rodando ou ainda não terminou de subir. Confira com `docker compose ps`.

## Testes automatizados

```bash
./mvnw test
```

Os testes atuais verificam se o contexto de cada serviço sobe corretamente.
