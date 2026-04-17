const API_BASE = `${window.location.origin}/api/v1`;
const searchInput = document.getElementById('searchInput');
const resultGrid = document.getElementById('results');
const searchState = document.getElementById('search-state');
const resultCount = document.getElementById('result-count');

function categoryClass(category) {
    if (category === 'BULKING') return 'tag ocean';
    if (category === 'CUTTING') return 'tag';
    return 'tag mint';
}

function renderResults(items) {
    resultCount.textContent = `${items.length} результатов`;

    if (!items.length) {
        resultGrid.innerHTML = '<div class="empty-state">По этому запросу ничего не найдено. Попробуйте другой термин или создайте новые советы на dashboard.</div>';
        return;
    }

    resultGrid.innerHTML = items.map((item) => `
        <article class="result-card">
            <span class="${categoryClass(item.category)}">${item.category || 'INFO'}</span>
            <h3>${item.title}</h3>
            <p>${item.content}</p>
        </article>
    `).join('');
}

async function search(query) {
    const term = query.trim();
    if (!term) {
        searchState.textContent = 'Введите термин и запустите поиск.';
        resultGrid.innerHTML = '';
        resultCount.textContent = '0 результатов';
        return;
    }

    searchState.textContent = `Ищу совпадения по запросу "${term}"...`;
    resultGrid.innerHTML = '<div class="empty-state">Сканирую индекс и собираю релевантные документы...</div>';

    try {
        const response = await fetch(`${API_BASE}/tips/search?term=${encodeURIComponent(term)}`);
        if (!response.ok) throw new Error('elastic-search-failed');
        const data = await response.json();
        searchState.textContent = `Поиск завершён для запроса "${term}".`;
        renderResults(data);
    } catch (error) {
        searchState.textContent = 'Не удалось связаться с Elasticsearch-контуром.';
        resultGrid.innerHTML = '<div class="empty-state">Проверьте, что backend и Elasticsearch подняты и доступны на ожидаемых портах.</div>';
        resultCount.textContent = '0 результатов';
    }
}

document.getElementById('searchButton').addEventListener('click', () => search(searchInput.value));
searchInput.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        search(searchInput.value);
    }
});

document.querySelectorAll('.suggestion-chip').forEach((button) => {
    button.addEventListener('click', () => {
        const query = button.dataset.query || '';
        searchInput.value = query;
        search(query);
    });
});
