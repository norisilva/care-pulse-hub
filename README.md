# CarePulse Hub

[Português](#português) | [English](#english) | [📸 Demo Showcase](DEMO.md)

---

<a name="português"></a>
## 🇧🇷 Português

> **Engenharia de Software e IA ajudando pessoas.**  
> Mantendo relações humanas em ordem. Minimizando conflitos. Um relatório de cada vez.

### O que é o CarePulse?
Muitas vezes enfrentamos situações onde a forma como nos comunicamos importa tanto quanto o que dizemos. O **CarePulse Hub** é uma ferramenta simples, de execução local, que ajuda pessoas em responsabilidades de cuidado a enviar relatórios claros, neutros e organizados — sem carga emocional, ambiguidade ou atrito.

**100% Gratuito, sem propagandas e sempre será mantido assim.**

### Para quem é isso?
Qualquer pessoa que precise reportar algo regularmente: Pais em guarda compartilhada, cuidadores de idosos, técnicos de enfermagem, pet sitters, mecânicos, etc.

### 🚀 Novidades (v0.1.0)
- **Camada de Neutralização IA (Fase 2 — Deep Mode)**: Agora com análise estrutural (NLP) para detectar construções acusatórias e não apenas palavras.
- **Dicionário CNV v2.0**: Baseado inteiramente na **Comunicação Não-Violenta (Marshall Rosenberg)**, com foco em Observação, Sentimento, Necessidade e Pedido.
- **Bloqueio de Backend**: A API agora rejeita automaticamente textos que fujam do padrão neutro sem confirmação explícita.
- **Controle de Impulso**: Novo sistema de "cool-off" que aguarda 3 minutos (canceláveis) antes do envio real, caso o texto contenha violações.
- **Suporte OpenNLP**: Download automático de modelos estruturais para análise gramatical profunda.

### Como Começar
**Windows**:
1. Certifique-se de ter o **Java 21** instalado.
2. Dê um duplo-clique no arquivo `start.bat`. Ele fará as checagens, iniciará o motor e abrirá o navegador.

**Linux / Mac**:
1. Instale o Java 21 (ex: `sudo apt install openjdk-21-jre`).
2. Abra o terminal na pasta do projeto e rode: `bash start.sh`.

*(Importante: O sistema roda na porta 8765 por padrão para evitar conflitos com outros apps).*

### Inicialização Automática (Opção Recomendada)
Para garantir que os **relatórios agendados** funcionem sempre, você pode configurar o sistema para rodar junto com o Windows/Linux:
- **Windows**: Dê um duplo-clique no arquivo `enable-autostart.bat`.
- **Linux**: Execute o comando `bash enable-autostart.sh`.
Isso fará com que o motor do sistema rode de forma invisível toda vez que você ligar o computador.

---

<a name="english"></a>
## 🇺🇸 English

> **Software engineering and AI working together to help people.**  
> Keeping human relationships in order. Minimizing conflict. One report at a time.

### What is CarePulse?
CarePulse Hub is a simple, local-first tool that helps people in care responsibilities send clear, neutral, and organized reports — without emotional loading, ambiguity, or friction. It aims to minimize noise and distrust in daily human interactions.

**100% free, no ads, and it will always stay this way.**

### Who is this for?
Anyone who needs to regularly report something: Parents in custody arrangements, elderly caretakers, private tutors, pet sitters, mechanics, contractors, etc.

### 🚀 What's New (v0.1.0)
- **AI Neutralization Layer (Phase 2 — Deep Mode)**: Now with structural NLP analysis to detect accusatory grammar patterns, not just keywords.
- **NVC Dictionary v2.0**: Based on **Marshall Rosenberg's Non-Violent Communication**, focusing on Observation, Feeling, Need, and Request.
- **Backend Enforcement**: API now rejects non-neutral text unless explicitly confirmed by the user.
- **Impulse Control**: A 3-minute cancelable delay system for reports containing AI violations.
- **OpenNLP Integration**: Automatic model downloads for deep grammatical analysis.
### Getting Started
**Windows**:
1. Ensure you have **Java 21** installed.
2. Double-click the `start.bat` file. It will check requirements, start the engine, and open your browser automatically.

**Linux / Mac**:
1. Install Java 21 (e.g., `sudo apt install openjdk-21-jre`).
2. Open the terminal in the project folder and run: `bash start.sh`.

*(Note: The backend runs on port 8765 by default to prevent conflicts with other common software).*

### Automatic OS Startup (Recommended)
To ensure **scheduled reports** are always sent, you should configure the system to start with your OS:
- **Windows**: Double-click the `enable-autostart.bat` file.
- **Linux**: Run `bash enable-autostart.sh` in the terminal.
This will load the engine silently in the background every time you turn on your computer.

---

## 🛠️ Tech Stack / Tecnologia
- **Backend**: Spring Boot 4 (Java 21)
- **Database**: H2 (File-persisted)
- **Security**: Jasypt (Encrypted credentials)
- **Frontend**: Vanilla JS / HTML5 / CSS3

## 📄 License
MIT License. See [LICENSE](LICENSE) for details.

*Built with care, for people who care.*  
*Construído com cuidado, para quem cuida.*
