const fileInput = document.getElementById('fileInput');
const selectedFileName = document.getElementById('selectedFileName');
const analyzeBtn = document.getElementById('analyzeBtn');
const loadingSpinner = document.getElementById('loadingSpinner');
const resultsSection = document.getElementById('resultsSection');
const summarySection = document.getElementById('summarySection');
const totalLogsEl = document.getElementById('totalLogs');
const analyzedLogsEl = document.getElementById('analyzedLogs');
const alertBox = document.getElementById('alertBox');
const dropZone = document.getElementById('dropZone');
const browseBtn = document.getElementById('browseBtn');

let selectedFile = null;

browseBtn.addEventListener('click', () => fileInput.click());

fileInput.addEventListener('change', (event) => {
    selectedFile = event.target.files[0] || null;
    selectedFileName.textContent = selectedFile ? selectedFile.name : 'No file selected';
});

dropZone.addEventListener('dragover', (event) => {
    event.preventDefault();
    dropZone.classList.add('drag-over');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('drag-over');
});

dropZone.addEventListener('drop', (event) => {
    event.preventDefault();
    dropZone.classList.remove('drag-over');
    selectedFile = event.dataTransfer.files[0] || null;
    selectedFileName.textContent = selectedFile ? selectedFile.name : 'No file selected';
});

analyzeBtn.addEventListener('click', async () => {
    if (!selectedFile) {
        showAlert('Please choose a log file first.', 'warning');
        return;
    }

    loadingSpinner.classList.remove('d-none');
    resultsSection.innerHTML = '';
    alertBox.innerHTML = '';
    summarySection.classList.add('d-none');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        const response = await fetch('/api/logs/analyze', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || 'Analysis failed');
        }

        if (data.message) {
            showAlert(data.message, 'info');
        } else {
            renderResults(data);
        }
    } catch (error) {
        showAlert(error.message || 'AI analysis failed.', 'danger');
    } finally {
        loadingSpinner.classList.add('d-none');
    }
});

function renderResults(data) {
    totalLogsEl.textContent = data.totalLogs || 0;
    analyzedLogsEl.textContent = data.analyzedLogs || 0;
    summarySection.classList.remove('d-none');

    if (!data.results || data.results.length === 0) {
        resultsSection.innerHTML = '<div class="alert alert-info">No analyzable logs found.</div>';
        return;
    }

    const cards = data.results.map(result => `
        <div class="card result-card rounded-4 mb-4">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <h5 class="card-title mb-0">${escapeHtml(result.originalLog || 'Log entry')}</h5>
                    <span class="badge bg-danger-subtle text-danger-emphasis">${escapeHtml(result.severity || 'UNKNOWN')}</span>
                </div>
                <div class="row g-3">
                    <div class="col-md-6"><strong>Error Type:</strong><div>${escapeHtml(result.errorType || 'N/A')}</div></div>
                    <div class="col-md-6"><strong>Severity:</strong><div>${escapeHtml(result.severity || 'N/A')}</div></div>
                    <div class="col-12"><strong>Root Cause:</strong><div>${escapeHtml(result.rootCause || 'N/A')}</div></div>
                    <div class="col-12"><strong>Suggested Fix:</strong><div>${escapeHtml(result.suggestedFix || 'N/A')}</div></div>
                    <div class="col-12"><strong>Summary:</strong><div>${escapeHtml(result.summary || 'N/A')}</div></div>
                </div>
            </div>
        </div>
    `).join('');

    resultsSection.innerHTML = cards;
}

function showAlert(message, type) {
    alertBox.innerHTML = `<div class="alert alert-${type} rounded-3">${escapeHtml(message)}</div>`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}
