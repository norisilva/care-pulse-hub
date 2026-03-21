document.addEventListener('DOMContentLoaded', () => {
    
    const API_BASE = '/api/engines';
    let currentEngineId = null;

    // Sanitize HTML to prevent XSS
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    // UI Elements
    const navItems = document.querySelectorAll('.nav-item');
    const viewSections = document.querySelectorAll('.view-section');
    const engineSelect = document.getElementById('engineSelect');
    const strategiesList = document.getElementById('strategies-list');
    
    // Stats
    const statsEnginesCount = document.getElementById('stats-engines-count');
    const statsStrategiesCount = document.getElementById('stats-strategies-count');
    
    // Lab
    const btnProcess = document.getElementById('btn-process');
    const labPayload = document.getElementById('lab-payload');
    const labResultBox = document.getElementById('lab-result-box');
    const labResultOutput = document.getElementById('lab-result-output');
    const execTimeMs = document.getElementById('exec-time-ms');

    // Navigation logic
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const target = item.getAttribute('data-target');
            
            navItems.forEach(n => n.classList.remove('active'));
            item.classList.add('active');
            
            viewSections.forEach(sec => {
                if (sec.id === target) {
                    sec.classList.add('active');
                    sec.classList.remove('hidden');
                } else {
                    sec.classList.remove('active');
                    sec.classList.add('hidden');
                }
            });
        });
    });

    // Initialize Dashboard
    async function loadEngines() {
        try {
            const res = await fetch(API_BASE);
            const engines = await res.json();
            
            engineSelect.innerHTML = engines.map(e => `<option value="${escapeHtml(e.id)}">${escapeHtml(e.name)}</option>`).join('');
            statsEnginesCount.innerText = engines.length;
            
            if (engines.length > 0) {
                currentEngineId = engines[0].id;
                loadEngineDetails(currentEngineId);
            } else {
                strategiesList.innerHTML = '<p class="text-muted">No engines found. Please start up Spring Boot engines.</p>';
            }

        } catch (error) {
            console.error('API Error:', error);
            strategiesList.innerHTML = '<p style="color:red">Failed to connect to AI Engine Backend.</p>';
        }
    }

    async function loadEngineDetails(engineId) {
        try {
            const res = await fetch(`${API_BASE}/${engineId}`);
            const engine = await res.json();
            
            statsStrategiesCount.innerText = engine.strategyIds.length;
            
            strategiesList.innerHTML = engine.strategyIds.map(id => `
                <div class="strategy-badge">
                    <span class="strat-name"><i class="fa-solid fa-code-branch"></i> ${escapeHtml(id)}</span>
                    <span class="strat-desc">Strategy loaded and active.</span>
                </div>
            `).join('');
            
        } catch (error) {
            console.error('Failed to load engine details', error);
        }
    }

    engineSelect.addEventListener('change', (e) => {
        currentEngineId = e.target.value;
        loadEngineDetails(currentEngineId);
    });

    // Lab Process
    btnProcess.addEventListener('click', async () => {
        if (!currentEngineId) return;

        const originalText = btnProcess.innerHTML;
        btnProcess.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';
        btnProcess.disabled = true;
        labResultBox.classList.add('hidden');

        try {
            let payloadObj = JSON.parse(labPayload.value);
            
            const reqBody = {
                parameters: {
                    userRole: "admin"
                },
                payload: payloadObj
            };

            const response = await fetch(`${API_BASE}/${currentEngineId}/process`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(reqBody)
            });

            const result = await response.json();
            
            labResultOutput.innerText = JSON.stringify(result, null, 2);
            execTimeMs.innerText = result.executionTimeMs || 0;
            labResultBox.classList.remove('hidden');

        } catch (e) {
            labResultOutput.innerText = "Error parsing json or calling API: \n" + e;
            labResultBox.classList.remove('hidden');
        } finally {
            btnProcess.innerHTML = originalText;
            btnProcess.disabled = false;
        }
    });

    // Boot
    loadEngines();
});
