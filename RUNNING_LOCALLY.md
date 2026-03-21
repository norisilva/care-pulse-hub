# Guia de Execução Local - CarePulse Hub (Decoupled)

Este repositório agora segue uma arquitetura desacoplada, onde o **Motor de IA** é um serviço independente e o **CarePulse Hub** é um dos aplicativos que o consomem.

## Estrutura de Pastas
- `engine/core-java`: O cérebro do sistema (Spring Boot).
- `engine/plugins-python`: Servidor gRPC para processamento de IA/ML.
- `apps/carepulse-frontend`: O aplicativo original (legado) que consome o motor.

---

## 1. Servidor de IA (Python gRPC)

O servidor Python fornece processamento de sentimento via gRPC na porta `50051`.

### Execução:
```powershell
cd engine/plugins-python

# Configurar ambiente (se ainda não fez)
uv venv
.\.venv\Scripts\activate
uv pip install -r requirements.txt

# Gerar código gRPC (apenas primeira vez)
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. proto/strategy.proto

# Iniciar o servidor
python -m src.server
```

---

## 2. Motor Central (Java Spring Boot)

O backend orquestra os motores e a persistência JPA.

### Execução:
```powershell
cd engine/core-java
mvn spring-boot:run
```

---

## 3. Acessar os Aplicativos

- **Admin Dashboard (Motor):** [http://localhost:8765/index.html](http://localhost:8765/index.html)
- **App Legado (CarePulse):** Você pode abrir o arquivo `apps/carepulse-frontend/index.html` diretamente no navegador.
- **Banco de Dados (H2 Console):** [http://localhost:8765/h2-console](http://localhost:8765/h2-console)
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa` | Password: (vazia)

---

## Solução de Problemas (Windows)

### Erro: "A execução de scripts foi desabilitada"
Se não conseguir ativar o `.venv`, rode no PowerShell:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```
