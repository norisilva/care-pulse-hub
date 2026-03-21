# CarePulse Hub — Powered by AI Engine

[Português](#português) | [English](#english) | [📋 Como Rodar Local](RUNNING_LOCALLY.md)

---

<a name="português"></a>
## 🇧🇷 Português

> **Engenharia de Software e IA ajudando pessoas.**  
> Um motor de IA agnóstico. Um aplicativo de cuidado. Muito mais por vir.

### Visão Geral da Arquitetura

Este repositório implementa um **Motor de IA desacoplado** — um sistema backend independente que suporta múltiplos aplicativos clientes. O **CarePulse Hub** é o primeiro.

```
email-auto-report/
├── engine/
│   ├── core-java/          ← Motor Central: Spring Boot + IA + gRPC (Java 21)
│   ├── plugins-python/     ← Plugins de IA: Servidor gRPC (Python 3.13)
│   └── admin-dashboard/    ← Dashboard de Controle do Motor (em desenvolvimento)
└── apps/
    └── carepulse-frontend/ ← Aplicativo Cliente #1: CarePulse Hub
```

O motor expõe uma **API REST única** e pode ser consumido por qualquer interface: web, mobile, CLI ou outro serviço.

---

### O que é o CarePulse?

Uma ferramenta que ajuda pessoas em responsabilidades de cuidado (pais, tutores, cuidadores) a enviar relatórios **claros, neutros e organizados** — sem carga emocional, ambiguidade ou atrito.

**100% Gratuito, offline-first e sempre será assim.**

---

### 🚀 Capacidades Implementadas do Motor

| Fase | Capacidade | Status |
|------|-----------|--------|
| 0 | Isolamento e refatoração do motor (arhcitetura agnóstica) | ✅ |
| 1 | Estratégias: Decision Tables + Algoritmos Java | ✅ |
| 2 | Distribuição parametrizável por data e percentual | ✅ |
| 3 | Plugins Python via gRPC (sentiment analysis) | ✅ |
| 4 | Treinamento e Feedback Loop contínuo | ✅ |
| 5 | Multi-motor: N engines independentes, estratégias compartilhadas | ✅ |
| 5.1 | Persistência definitiva (anti-amnésia sistêmica via JPA) | ✅ |
| 6 | Desacoplamento de diretórios (engine vs apps) | ✅ |
| 6.1 | **Admin Dashboard do Motor** | 🔜 Em desenvolvimento |

---

### Como Rodar

Veja o guia completo: **[RUNNING_LOCALLY.md](RUNNING_LOCALLY.md)**

Resumo rápido:
```powershell
# 1. Subir o plugin Python (gRPC, porta 50051)
cd engine/plugins-python
uv venv && .venv\Scripts\activate
uv pip install -r requirements.txt
python -m src.server

# 2. Subir o motor Java (porta 8765)
cd engine/core-java
mvn spring-boot:run

# 3. Acessar
# App CarePulse: http://localhost:8765/apps/carepulse-frontend/index.html
# Admin Motor:  http://localhost:8765/index.html
# H2 Console:  http://localhost:8765/h2-console
```

---

<a name="english"></a>
## 🇺🇸 English

> **Software engineering and AI working together to help people.**  
> A standalone AI engine. One care app. Many more to come.

### Architecture Overview

This repository implements a **decoupled AI Engine** — an independent backend system designed to support multiple client applications. **CarePulse Hub** is the first client.

The engine exposes a single REST API consumed by any interface: web, mobile, CLI, or another service.

---

### What is CarePulse?

CarePulse Hub helps people in care responsibilities (parents, guardians, caretakers) send **clear, neutral and organized reports** — without emotional loading, ambiguity, or friction.

**100% free, offline-first, and it will always stay this way.**

---

### Engine Capabilities

| Phase | Capability | Status |
|-------|-----------|--------|
| 0 | Engine isolation & refactor (agnostic architecture) | ✅ |
| 1 | Strategies: Decision Tables + Java Algorithms | ✅ |
| 2 | Time-based distribution with percentages | ✅ |
| 3 | Python plugins via gRPC (sentiment analysis) | ✅ |
| 4 | Training & Feedback Loop | ✅ |
| 5 | Multi-engine: N independent engines, shared strategies | ✅ |
| 5.1 | Persistent state (JPA anti-amnesia) | ✅ |
| 6 | Directory decoupling (engine vs apps) | ✅ |
| 6.1 | **Engine Admin Dashboard** | 🔜 In development |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Engine Core | Spring Boot 4, Java 21 |
| AI Plugins | Python 3.13, gRPC, grpcio |
| NLP (PT-BR) | Apache OpenNLP, Lucene, CNV dictionary |
| Persistence | H2 (JPA/Hibernate) |
| Security | Jasypt (encrypted credentials) |
| Client App | Vanilla JS / HTML5 / CSS3 |

## 📄 License
MIT License. See [LICENSE](LICENSE) for details.

*Built with care, for people who care.*  
*Construído com cuidado, para quem cuida.*
