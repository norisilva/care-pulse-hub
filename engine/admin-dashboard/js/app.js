const API_BASE = '/api/engines';

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initModal();
    initSimulator();
    fetchEngines();
});

// --- State ---
let enginesList = [];

// --- Navigation ---
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const views = document.querySelectorAll('.view');
    const topbarTitle = document.querySelector('.topbar h2');

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = item.getAttribute('data-target');
            
            // Update active state
            navItems.forEach(n => n.classList.remove('active'));
            item.classList.add('active');
            
            // Switch views
            views.forEach(v => v.classList.remove('active'));
            const targetView = document.getElementById(`view-${targetId}`);
            if(targetView) {
                targetView.classList.add('active');
            } else {
                // For targets without views yet, show dashboard
                document.getElementById('view-dashboard').classList.add('active');
            }
            
            topbarTitle.textContent = item.textContent.trim();
        });
    });
}

// --- Modals ---
function initModal() {
    const modalOverlay = document.getElementById('modal-new-engine');
    const btnNew = document.getElementById('btn-new-engine');
    const btnCloseList = document.querySelectorAll('.close-btn');
    const btnSave = document.getElementById('btn-save-engine');
    
    btnNew.addEventListener('click', () => {
        modalOverlay.classList.add('active');
    });
    
    btnCloseList.forEach(btn => {
        btn.addEventListener('click', () => {
            modalOverlay.classList.remove('active');
        });
    });
    
    btnSave.addEventListener('click', async () => {
        const idInput = document.getElementById('engine-form-id').value;
        const nameInput = document.getElementById('engine-form-name').value;
        
        if(!idInput || !nameInput) {
            alert('Please fill out all fields.');
            return;
        }
        
        try {
            const res = await fetch(API_BASE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: idInput, name: nameInput })
            });
            
            if(res.ok) {
                modalOverlay.classList.remove('active');
                document.getElementById('engine-form-id').value = '';
                document.getElementById('engine-form-name').value = '';
                fetchEngines();
            } else {
                alert('Failed to create engine. ID might already exist.');
            }
        } catch(e) {
            console.error(e);
            alert('API Error');
        }
    });
}

// --- Data Fetching ---
async function fetchEngines() {
    try {
        const res = await fetch(API_BASE);
        if(!res.ok) throw new Error('API failed');
        enginesList = await res.json();
        
        updateDashboardStats();
        renderEnginesTable();
        updateSimulatorSelect();
    } catch(e) {
        console.error('Failed fetching engines:', e);
        const tbody = document.getElementById('engines-table-body');
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Unable to connect to Engine Core API. Check if backend is running.</td></tr>`;
    }
}

function updateDashboardStats() {
    document.getElementById('stat-engines-count').textContent = enginesList.length;
    
    // Count total unique strategies across all engines
    const uniqueStrategies = new Set();
    enginesList.forEach(eng => {
        if(eng.strategyIds) eng.strategyIds.forEach(sid => uniqueStrategies.add(sid));
    });
    
    document.getElementById('stat-strategies-count').textContent = uniqueStrategies.size;
}

function renderEnginesTable() {
    const tbody = document.getElementById('engines-table-body');
    
    if(enginesList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state">No engines registered yet. Create one!</td></tr>`;
        return;
    }
    
    tbody.innerHTML = enginesList.map(engine => {
        const strategiesHtml = engine.strategyIds && engine.strategyIds.length > 0 
            ? engine.strategyIds.map(sid => `<span class="badge">${sid}</span>`).join(' ')
            : `<span style="color:var(--text-muted);font-size:0.8rem">No strategies linked</span>`;
            
        return `
            <tr>
                <td style="font-family:monospace; color:var(--brand-primary);">${engine.id}</td>
                <td style="font-weight:500">${engine.name}</td>
                <td>${strategiesHtml}</td>
                <td>
                    <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 0.8rem;" onclick="loadEngineToSimulator('${engine.id}')">
                        Simulate
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

// --- Simulator ---
function initSimulator() {
    const btnSimulate = document.getElementById('btn-simulate');
    
    btnSimulate.addEventListener('click', async () => {
        const engineId = document.getElementById('sim-engine-select').value;
        const inputText = document.getElementById('sim-input-text').value;
        const outputContent = document.getElementById('sim-output-content');
        const timeBadge = document.getElementById('sim-time');
        
        if(!engineId) {
            alert('Please select an engine from the dropdown first.');
            return;
        }
        
        if(!inputText) {
            alert('Please enter text to simulate.');
            return;
        }

        const btnOriginalText = btnSimulate.textContent;
        btnSimulate.textContent = "Processing...";
        btnSimulate.disabled = true;
        
        timeBadge.textContent = 'running...';
        
        const payload = {
            payload: { text: inputText },
            parameters: {}
        };
        
        const startTime = performance.now();
        
        try {
            const res = await fetch(`${API_BASE}/${engineId}/process`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            
            const endTime = performance.now();
            timeBadge.textContent = `${Math.round(endTime - startTime)} ms`;
            
            if(res.ok) {
                const data = await res.json();
                
                // Format output
                outputContent.innerHTML = `
                    <div style="margin-bottom: 12px; color: white;">Analysis complete against <b>${engineId}</b></div>
                    <div class="result-payload">${JSON.stringify(data, null, 2)}</div>
                `;
            } else {
                outputContent.innerHTML = `<span style="color:var(--accent-red)">Error: Engine process failed. Check if engine has strategies.</span>`;
            }
        } catch(e) {
            console.error(e);
            outputContent.innerHTML = `<span style="color:var(--accent-red)">Network Error connecting to engine.</span>`;
        } finally {
            btnSimulate.textContent = btnOriginalText;
            btnSimulate.disabled = false;
        }
    });
}

function updateSimulatorSelect() {
    const select = document.getElementById('sim-engine-select');
    
    if(enginesList.length === 0) {
        select.innerHTML = '<option value="">No engines available</option>';
        return;
    }
    
    select.innerHTML = enginesList.map(eng => {
        return `<option value="${eng.id}">${eng.name} (${eng.id})</option>`;
    }).join('');
}

window.loadEngineToSimulator = function(engineId) {
    // Switch to simulator view via nav click logic
    const simNav = document.querySelector('.nav-item[data-target="simulation"]');
    if(simNav) simNav.click();
    
    // Select the engine
    const select = document.getElementById('sim-engine-select');
    if(select) select.value = engineId;
};
