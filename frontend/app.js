const API_BASE = 'http://localhost:8765/api';

let currentRole = "1";

document.addEventListener('DOMContentLoaded', async () => {
    // Initialize application state
    await initializeApp();
});

async function initializeApp() {
    const setupDone = await checkSetup();
    if (setupDone) {
        await loadGroups();
        await loadLastReport();
        await loadScenarios();
    } else {
        // If server was offline, retry in 3 seconds
        setTimeout(initializeApp, 3000);
    }
}

const reportForm = document.getElementById('reportForm');
const neutralizeBtn = document.getElementById('neutralizeBtn');

// UI Components
const modal = document.getElementById('settingsModal');
const settingsBtn = document.getElementById('settingsBtn');
const closeBtn = document.querySelector('.close-btn');
const configForm = document.getElementById('configForm');
const encryptTestBtn = document.getElementById('encryptTestBtn');
const scenarioSelect = document.getElementById('reportScenario');

const createGroupForm = document.getElementById('createGroupForm');

// Initial check to see if first-time setup is needed
async function checkSetup() {
    try {
        const response = await fetch(`${API_BASE}/config`);
        if (response.ok) {
            const config = await response.json();
            if (config.userRole) {
                currentRole = config.userRole;
                updateFormLabels(currentRole);
            }
            if (config.setupRequired) {
                // Only alert if we haven't already opened the modal in this session to avoid spam
                if (modal.style.display !== 'flex') {
                    alert('⚠️ Bem-vindo! É necessário configurar o E-mail e a Chave Mestra antes de usar o sistema.');
                    openSettingsModal(config);
                }
            }
            return true; // Successfully connected
        }
    } catch (e) {
        updateStatus('Servidor offline (Iniciando motor...)', '#ef4444');
        console.warn("Server offline, waiting to check setup...");
    }
    return false; // Connection failed
}

function updateFormLabels(role) {
    const labels = {
        "1": ["Saúde", "Rotina/Educação", "Estado Emocional", "Observações Gerais"],
        "2": ["Alimentação", "Sono", "Atividades", "Observações"],
        "3": ["Sinais Vitais", "Medicação", "Evolução", "Intercorrências"],
        "4": ["Desempenho", "Foco", "Tarefas", "Sugestões"],
        "5": ["Passeio", "Alimentação", "Comportamento", "Saúde"],
        "6": ["Treino Realizado", "Condição Física", "Frequência Cardíaca", "Metas"],
        "7": ["Veículo/Aparelho", "Diagnóstico", "Peças Trocadas", "Testes"],
        "8": ["Cômodo", "O que foi feito", "Pendências", "Observações"],
        "9": ["Obra/Fase", "Materiais Usados", "Avanço do Dia", "Problemas"],
        "10": ["Campo 1", "Campo 2", "Campo 3", "Campo 4"]
    };

    const selLabels = labels[role] || labels["1"];
    document.getElementById('labelField1').innerText = selLabels[0];
    document.getElementById('labelField2').innerText = selLabels[1];
    document.getElementById('labelField3').innerText = selLabels[2];
    document.getElementById('labelField4').innerText = selLabels[3];
}

async function loadGroups() {
    try {
        const res = await fetch(`${API_BASE}/groups`);
        if (res.ok) {
            const groups = await res.json();
            const reportGroupSelect = document.getElementById('reportGroup');
            reportGroupSelect.innerHTML = '<option value="">Selecione um grupo...</option>';

            const groupsListDiv = document.getElementById('groupsList');
            groupsListDiv.innerHTML = '';

            groups.forEach(g => {
                // Populate main dropdown for report creation
                const opt = document.createElement('option');
                opt.value = g.id;
                opt.innerText = `${g.name} (${g.recipientEmail})`;
                reportGroupSelect.appendChild(opt);

                // Populate management modal list
                const gDiv = document.createElement('div');
                gDiv.style = "display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.2); padding: 0.5rem; margin-bottom: 0.5rem; border-radius: 8px;";
                gDiv.innerHTML = `
                    <div>
                        <strong>${g.name}</strong><br>
                        <small>${g.recipientEmail} | ${g.periodicity}</small>
                    </div>
                `;
                const delBtn = document.createElement('button');
                delBtn.innerText = 'Excluir';
                delBtn.style = "background: #ef4444; border: none; padding: 0.3rem 0.5rem; color: white; border-radius: 4px; cursor: pointer;";
                delBtn.onclick = () => deleteGroup(g.id);
                gDiv.appendChild(delBtn);
                groupsListDiv.appendChild(gDiv);
            });
        }
    } catch (e) {
        console.error("Failed to load groups", e);
    }
}

async function deleteGroup(id) {
    if (!confirm('Certeza que deseja excluir este grupo?')) return;
    await fetch(`${API_BASE}/groups/${id}`, { method: 'DELETE' });
    loadGroups();
}

createGroupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById('newGroupName').value,
        recipientEmail: document.getElementById('newGroupEmail').value,
        periodicity: document.getElementById('newGroupPeriod').value
    };
    try {
        const res = await fetch(`${API_BASE}/groups`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            createGroupForm.reset();
            loadGroups();
        }
    } catch (e) {
        alert("Erro ao criar grupo.");
    }
});

function openSettingsModal(preloadedConfig = null) {
    if (preloadedConfig) {
        if (preloadedConfig.masterKey) document.getElementById('masterKey').value = preloadedConfig.masterKey;
        if (preloadedConfig.recipientEmail) document.getElementById('recipientEmail').value = preloadedConfig.recipientEmail;
        if (preloadedConfig.userRole) document.getElementById('userRole').value = preloadedConfig.userRole;
    } else {
        fetch(`${API_BASE}/config`).then(res => res.json()).then(config => {
            if (config.masterKey) document.getElementById('masterKey').value = config.masterKey;
            if (config.recipientEmail) document.getElementById('recipientEmail').value = config.recipientEmail;
            if (config.userRole) document.getElementById('userRole').value = config.userRole;
        }).catch(e => console.error("Failed to load config", e));
    }
    modal.style.display = 'flex';
}

settingsBtn.addEventListener('click', () => openSettingsModal());
manageGroupsBtn.addEventListener('click', () => { groupsModal.style.display = 'flex'; loadGroups(); });

closeBtn.addEventListener('click', () => { modal.style.display = 'none'; });
closeGroupsBtn.addEventListener('click', () => { groupsModal.style.display = 'none'; });

window.addEventListener('click', (e) => {
    if (e.target === modal) modal.style.display = 'none';
    if (e.target === groupsModal) groupsModal.style.display = 'none';
});

configForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        masterKey: document.getElementById('masterKey').value,
        emailUser: document.getElementById('emailUser').value,
        emailPass: document.getElementById('emailPass').value,
        recipientEmail: document.getElementById('recipientEmail').value,
        openaiKey: document.getElementById('openaiKey').value,
        userRole: document.getElementById('userRole').value
    };

    try {
        const response = await fetch(`${API_BASE}/config`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (response.ok) {
            alert('Configurações salvas com sucesso!');
            modal.style.display = 'none';
            currentRole = payload.userRole;
            updateFormLabels(currentRole);
            loadGroups(); // Refresh default group if email changed
            loadScenarios(); // Refresh templates for the new role
        } else {
            alert('Falha ao salvar configurações.');
        }
    } catch (e) { alert('Erro ao salvar config.'); }
});

if (encryptTestBtn) {
    encryptTestBtn.addEventListener('click', async () => {
        const masterKey = document.getElementById('masterKey').value;
        const valueToEncrypt = prompt("Digite o texto que deseja criptografar para testar:");
        if (!masterKey || !valueToEncrypt) { alert('Preencha a Chave Mestra e um texto.'); return; }
        try {
            const response = await fetch(`${API_BASE}/config/encrypt`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ masterKey, value: valueToEncrypt })
            });
            if (response.ok) {
                const data = await response.json();
                prompt("Hash gerado:", data.encrypted);
            }
        } catch (e) { alert('Erro.'); }
    });
}

// Reset DB: double confirmation + backend wipe
const resetDbBtn = document.getElementById('resetDbBtn');
if (resetDbBtn) {
    resetDbBtn.addEventListener('click', async () => {
        const first = confirm(
            '⚠️ ATENÇÃO: ZONA DE PERIGO\n\n' +
            'Você está prestes a apagar TODOS os dados:\n' +
            '  • Relatórios enviados\n' +
            '  • Grupos de notificação\n' +
            '  • Configurações de e-mail\n\n' +
            'Essa ação é IRREVERSÍVEL e NÃO TEM VOLTA.\n\nDeseja continuar?'
        );
        if (!first) return;

        const second = confirm(
            '🔴 ÚLTIMA CONFIRMAÇÃO\n\n' +
            'Tem ABSOLUTA CERTEZA?\n\nTodos os dados serão perdidos para sempre.'
        );
        if (!second) return;

        try {
            const res = await fetch(`${API_BASE.replace('/api', '')}/api/admin/reset`, { method: 'DELETE' });
            if (res.status === 204) {
                alert('✅ Banco de dados resetado com sucesso.\nO sistema será recarregado.');
                modal.style.display = 'none';
                initializeApp(); // Reload the app state
            } else {
                alert('Erro ao resetar o banco. Tente novamente.');
            }
        } catch (e) {
            alert('Erro de conexão ao tentar resetar.');
        }
    });
}

async function loadLastReport() {
    try {
        const response = await fetch(`${API_BASE}/reports/last`);
        if (response.ok) {
            if (response.status === 204) {
                updateStatus('Pronto para novo relatório', '#10b981');
                return;
            }
            const data = await response.json();
            document.getElementById('assistedName').value = data.assistedName || '';
            document.getElementById('reportGroup').value = data.notificationGroupId || '';
            document.getElementById('field1').value = data.field1 || '';
            document.getElementById('field2').value = data.field2 || '';
            document.getElementById('field3').value = data.field3 || '';
            document.getElementById('field4').value = data.field4 || '';
            updateStatus('Último relatório carregado', '#10b981');
        }
    } catch (error) {
        console.error('Error loading last report:', error);
        updateStatus('Servidor offline (Tente atualizar (F5) em instantes)', '#ef4444');
    }
}

let pendingReportData = null;
let sendTimerInterval = null;
let timeRemaining = 180; // 3 minutes

const reflectionMessages = [
    "Respire fundo. A comunicação clara reduz conflitos duradouros.",
    "Lembre-se: O que está escrito fica documentado para sempre.",
    "Vale a pena gerar atrito por conta de uma palavra?",
    "Você revisou as sugestões da IA? Elas ajudam a manter a objetividade.",
    "Um relatório neutro foca nos fatos, não em atacar as pessoas.",
    "Pense no longo prazo: a paz custa menos que o conflito."
];
let reflectionInterval = null;

function startReflection() {
    const container = document.getElementById('reflectionContainer');
    const msgSpan = document.getElementById('reflectionMessage');
    if (!container || !msgSpan) return;

    container.style.display = 'block';

    let msgIndex = 0;
    const showNextMessage = () => {
        msgSpan.style.opacity = '0'; // fade out
        setTimeout(() => {
            msgSpan.innerText = `💡 "${reflectionMessages[msgIndex]}"`;
            msgSpan.style.opacity = '1'; // fade in
            msgIndex = (msgIndex + 1) % reflectionMessages.length;
        }, 500);
    };

    showNextMessage();
    reflectionInterval = setInterval(showNextMessage, 6000); // 6 seconds rotation
}

function stopReflection() {
    const container = document.getElementById('reflectionContainer');
    if (container) container.style.display = 'none';
    if (reflectionInterval) {
        clearInterval(reflectionInterval);
        reflectionInterval = null;
    }
}

function startSendTimer() {
    const sendReportBtn = document.querySelector('#reportForm button[type="submit"]');
    timeRemaining = 180;
    sendReportBtn.classList.replace('btn-primary', 'btn-secondary');
    sendReportBtn.style.color = '#ef4444'; // Indicator color for cancel
    sendReportBtn.innerText = `Cancelar Envio (${formatTime(timeRemaining)})`;

    startReflection();

    sendTimerInterval = setInterval(() => {
        timeRemaining--;
        if (timeRemaining <= 0) {
            clearInterval(sendTimerInterval);
            sendTimerInterval = null;
            stopReflection();
            executeSendReport();
        } else {
            sendReportBtn.innerText = `Cancelar Envio (${formatTime(timeRemaining)})`;
        }
    }, 1000);
}

function formatTime(seconds) {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
}

function cancelSendTimer() {
    if (sendTimerInterval) {
        clearInterval(sendTimerInterval);
        sendTimerInterval = null;
        pendingReportData = null;
        stopReflection();
        resetSendButton();
        alert('Envio cancelado. Você pode continuar editando o relatório.');
    }
}

function resetSendButton() {
    const sendReportBtn = document.querySelector('#reportForm button[type="submit"]');
    sendReportBtn.classList.replace('btn-secondary', 'btn-primary');
    sendReportBtn.style.color = '';
    sendReportBtn.innerText = 'Enviar Report';
}

async function executeSendReport() {
    resetSendButton();
    if (!pendingReportData) return;

    try {
        const response = await fetch(`${API_BASE}/reports`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(pendingReportData)
        });
        if (response.ok) {
            alert('Relatório enviado com sucesso!');
        } else {
            const error = await response.json();
            if (error.message && error.message.includes('REJEITADO_PELA_IA')) {
                alert('BLOQUEADO: ' + error.message);
                // Trigger the UI modal just in case it was bypassed
                const violationModal = document.getElementById('violationModal');
                if (violationModal) violationModal.style.display = 'flex';
            } else {
                alert(`Erro: ${error.message || 'Falha ao enviar'}`);
            }
        }
    } catch (error) {
        alert('Erro de conexão com o servidor.');
    } finally {
        pendingReportData = null;
    }
}

reportForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (sendTimerInterval) {
        // If timer is running, clicking the button cancels the send
        cancelSendTimer();
        return;
    }

    // Check if group is selected
    const groupId = document.getElementById('reportGroup').value;
    if (!groupId) {
        alert('Por favor, selecione um Grupo de Notificação.');
        return;
    }

    pendingReportData = {
        notificationGroupId: parseInt(groupId),
        assistedName: document.getElementById('assistedName').value || 'Não informado',
        field1: document.getElementById('field1').value,
        field2: document.getElementById('field2').value,
        field3: document.getElementById('field3').value,
        field4: document.getElementById('field4').value,
        forceSend: false // Default is false, will be set to true if confirmed via modal
    };

    const hasViolations = document.querySelectorAll('.suggestion-item').length > 0;
    const violationModal = document.getElementById('violationModal');

    if (hasViolations && violationModal) {
        violationModal.style.display = 'flex';

        // Temporarily bind listeners here to ensure they catch the dynamic elements
        document.getElementById('closeViolationModal').onclick = () => violationModal.style.display = 'none';
        document.getElementById('cancelSendBtn').onclick = () => violationModal.style.display = 'none';
        document.getElementById('confirmSendBtn').onclick = () => {
            violationModal.style.display = 'none';
            pendingReportData.forceSend = true; // Inject confirmation flag
            startSendTimer();
        };
    } else {
        // Send normally if no violations
        executeSendReport();
    }
});

async function loadScenarios() {
    if (!currentRole) return;
    try {
        const res = await fetch(`${API_BASE}/suggestions/${currentRole}`);
        if (res.ok) {
            const suggestions = await res.json();
            scenarioSelect.innerHTML = '<option value="">Nenhum (Texto em branco)</option>';
            suggestions.forEach(s => {
                const opt = document.createElement('option');
                opt.value = JSON.stringify(s); // Store the whole object
                opt.innerText = s.scenario;
                scenarioSelect.appendChild(opt);
            });
        }
    } catch (e) {
        console.error("Failed to load scenarios", e);
    }
}

scenarioSelect.addEventListener('change', (e) => {
    if (!e.target.value) {
        // Option to clear? Maybe just leave what's there?
        // Let's clear if they select "Nenhum"
        document.getElementById('field1').value = '';
        document.getElementById('field2').value = '';
        document.getElementById('field3').value = '';
        document.getElementById('field4').value = '';
        return;
    }
    const s = JSON.parse(e.target.value);
    document.getElementById('field1').value = s.field1Suggestion || '';
    document.getElementById('field2').value = s.field2Suggestion || '';
    document.getElementById('field3').value = s.field3Suggestion || '';
    document.getElementById('field4').value = s.field4Suggestion || '';
});

neutralizeBtn.addEventListener('click', async () => {
    // Trigger /deep analysis on all fields manually
    const btn = neutralizeBtn;
    const originalText = btn.innerText;
    btn.innerText = "Analisando...";
    btn.disabled = true;

    const fields = ['field1', 'field2', 'field3', 'field4'];
    for (const f of fields) {
        const el = document.getElementById(f);
        if (el.value.trim().length > 0) {
            await analyzeText(f, el.value, 'deep');
        }
    }

    btn.innerText = originalText;
    btn.disabled = false;
});

// AI Neutralization Logic
let analyzeTimeout;
function debounceAnalyze(fieldId, text) {
    clearTimeout(analyzeTimeout);
    analyzeTimeout = setTimeout(() => {
        analyzeText(fieldId, text, 'fast');
    }, 800);
}

async function analyzeText(fieldId, text, mode) {
    if (!text || text.trim().length === 0) {
        document.getElementById('suggest-' + fieldId).innerHTML = '';
        return;
    }

    try {
        const payload = {
            text: text,
            context: 'custody', // We map userRole to context on backend eventually, or send generic here
            language: 'pt-BR'
        };

        const res = await fetch(`${API_BASE}/neutralization/${mode}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            const data = await res.json();
            renderSuggestions(fieldId, data.suggestions);
        }
    } catch (e) {
        console.warn("Failed to analyze text:", e);
    }
}

function renderSuggestions(fieldId, suggestions) {
    const container = document.getElementById('suggest-' + fieldId);
    container.innerHTML = '';

    if (!suggestions || suggestions.length === 0) return;

    suggestions.forEach(s => {
        const div = document.createElement('div');
        div.className = `suggestion-item severity-${s.severity}`;

        div.innerHTML = `
            <div class="suggestion-header">
                <div>
                    Substitua <span class="suggestion-original">"${s.originalSpan}"</span>
                    por <span class="suggestion-replacement">"${s.suggestedReplacement}"</span>
                </div>
            </div>
            <div class="suggestion-reason">${s.reason}</div>
        `;
        container.appendChild(div);
    });
}

// Bind events to the textareas
['field1', 'field2', 'field3', 'field4'].forEach(f => {
    const el = document.getElementById(f);
    el.addEventListener('keyup', (e) => {
        debounceAnalyze(f, e.target.value);
    });

    el.addEventListener('blur', (e) => {
        // Run it immediately on blur
        clearTimeout(analyzeTimeout);
        analyzeText(f, e.target.value, 'fast');
    });
});

function updateStatus(text, color) {
    document.getElementById('statusText').innerText = text;
    document.querySelector('.dot').style.background = color;
}
