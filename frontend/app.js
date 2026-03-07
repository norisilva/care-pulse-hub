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

reportForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Check if group is selected
    const groupId = document.getElementById('reportGroup').value;
    if (!groupId) {
        alert('Por favor, selecione um Grupo de Notificação.');
        return;
    }

    const reportData = {
        notificationGroupId: parseInt(groupId),
        assistedName: document.getElementById('assistedName').value || 'Não informado',
        field1: document.getElementById('field1').value,
        field2: document.getElementById('field2').value,
        field3: document.getElementById('field3').value,
        field4: document.getElementById('field4').value
    };

    try {
        const response = await fetch(`${API_BASE}/reports`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(reportData)
        });
        if (response.ok) {
            alert('Relatório enviado com sucesso!');
        } else {
            const error = await response.json();
            alert(`Erro: ${error.message || 'Falha ao enviar'}`);
        }
    } catch (error) { alert('Erro de conexão com o servidor.'); }
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

neutralizeBtn.addEventListener('click', () => { alert('IA habilitada no MVP.'); });

function updateStatus(text, color) {
    document.getElementById('statusText').innerText = text;
    document.querySelector('.dot').style.background = color;
}
