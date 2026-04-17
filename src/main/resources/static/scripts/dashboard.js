const API_BASE = `${window.location.origin}/api/v1`;
let allTips = [];
let activeCategory = 'ALL';

const tipsList = document.getElementById('tips-list');
const mealLogList = document.getElementById('meal-log-list');
const adviceBox = document.getElementById('advice-box');
const globalStatus = document.getElementById('global-status');
const tipsCount = document.getElementById('tips-count');
const logsCount = document.getElementById('logs-count');

function setStatus(element, message, type = '') {
    element.textContent = message;
    element.className = `inline-status ${type}`.trim();
}

function categoryClass(category) {
    if (category === 'BULKING') return 'tag ocean';
    if (category === 'CUTTING') return 'tag';
    return 'tag mint';
}

function renderTips() {
    const visibleTips = activeCategory === 'ALL'
        ? allTips
        : allTips.filter((tip) => tip.category === activeCategory);

    tipsCount.textContent = allTips.length;

    if (!visibleTips.length) {
        tipsList.innerHTML = '<div class="empty-state">Для этой категории пока нет данных. Создайте новый совет справа и обновите список.</div>';
        return;
    }

    tipsList.innerHTML = visibleTips.map((tip) => `
        <article class="tip-card">
            <span class="${categoryClass(tip.category)}">${tip.category || 'INFO'}</span>
            <h3>${tip.title}</h3>
            <p>${tip.content}</p>
        </article>
    `).join('');
}

async function loadTips() {
    globalStatus.textContent = 'Обновляю библиотеку советов и синхронизирую dashboard.';
    try {
        const response = await fetch(`${API_BASE}/tips`);
        if (!response.ok) throw new Error('tips-request-failed');
        allTips = await response.json();
        renderTips();
        globalStatus.textContent = 'Данные API загружены. Можно создавать новые советы и meal logs.';
    } catch (error) {
        tipsList.innerHTML = '<div class="empty-state">Не удалось получить советы. Проверьте, что Spring Boot приложение и зависимости инфраструктуры запущены.</div>';
        globalStatus.textContent = 'Не получилось связаться с API. Проверьте backend и порты.';
    }
}

async function loadMealLogs(userId) {
    if (!userId) {
        mealLogList.innerHTML = '';
        logsCount.textContent = '0';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/meal-logs/${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error('meal-log-request-failed');
        const logs = await response.json();
        logsCount.textContent = logs.length;

        if (!logs.length) {
            mealLogList.innerHTML = '<div class="empty-state">У этого пользователя пока нет meal logs.</div>';
            return;
        }

        mealLogList.innerHTML = logs.slice().reverse().map((log) => `
            <article class="timeline-item">
                <strong>${log.foodName}</strong>
                <span class="tag mint">${log.calories ?? 0} kcal</span>
                <p>Белки ${log.protein ?? 0} г · Углеводы ${log.carbs ?? 0} г · Жиры ${log.fats ?? 0} г</p>
                <small>${log.consumedAt ? new Date(log.consumedAt).toLocaleString('ru-RU') : 'время будет присвоено сервером'}</small>
            </article>
        `).join('');
    } catch (error) {
        mealLogList.innerHTML = '<div class="empty-state">Историю meal logs пока не удалось загрузить.</div>';
        logsCount.textContent = '0';
    }
}

async function loadAdvice() {
    const userId = document.getElementById('advice-user-id').value.trim();
    if (!userId) {
        adviceBox.innerHTML = '<p>Укажите user ID, чтобы построить персональную рекомендацию.</p>';
        return;
    }

    adviceBox.innerHTML = '<p>Считаю калораж и подбираю релевантную категорию...</p>';
    await loadMealLogs(userId);

    try {
        const response = await fetch(`${API_BASE}/advice/${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error('advice-request-failed');
        const data = await response.json();
        adviceBox.innerHTML = `<p><strong>${data.userId}</strong></p><p>${data.advice}</p>`;
    } catch (error) {
        adviceBox.innerHTML = '<p>Совет пока не получен. Скорее всего, backend или связанные сервисы ещё не подняты.</p>';
    }
}

function parseNumericField(value) {
    if (value === '') return null;
    return Number(value);
}

document.getElementById('refresh-tips').addEventListener('click', loadTips);
document.getElementById('load-advice').addEventListener('click', loadAdvice);

document.querySelectorAll('[data-scroll]').forEach((button) => {
    button.addEventListener('click', () => {
        document.querySelector(button.dataset.scroll)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        if (button.dataset.scroll === '#tips-section') {
            loadTips();
        }
    });
});

document.querySelectorAll('#category-filters .chip').forEach((button) => {
    button.addEventListener('click', () => {
        document.querySelectorAll('#category-filters .chip').forEach((chip) => chip.classList.remove('active'));
        button.classList.add('active');
        activeCategory = button.dataset.category;
        renderTips();
    });
});

document.getElementById('tip-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = Object.fromEntries(form.entries());
    const status = document.getElementById('tip-status');

    setStatus(status, 'Сохраняю совет и синхронизирую SQL + Elastic...');

    try {
        const response = await fetch(`${API_BASE}/admin/tips`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('tip-save-failed');
        event.currentTarget.reset();
        setStatus(status, 'Совет сохранён. Каталог обновлён.', 'success');
        await loadTips();
    } catch (error) {
        setStatus(status, 'Не удалось сохранить совет.', 'error');
    }
});

document.getElementById('meal-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = {
        userId: form.get('userId'),
        foodName: form.get('foodName'),
        calories: parseNumericField(form.get('calories')),
        protein: parseNumericField(form.get('protein')),
        carbs: parseNumericField(form.get('carbs')),
        fats: parseNumericField(form.get('fats'))
    };
    const status = document.getElementById('meal-status');

    setStatus(status, 'Сохраняю meal log...');

    try {
        const response = await fetch(`${API_BASE}/meal-logs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error('meal-save-failed');
        setStatus(status, 'Meal log сохранён. История обновлена.', 'success');
        document.getElementById('advice-user-id').value = payload.userId;
        await loadMealLogs(payload.userId);
    } catch (error) {
        setStatus(status, 'Не удалось сохранить meal log.', 'error');
    }
});

document.getElementById('training-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = {
        userId: form.get('userId'),
        exerciseType: form.get('exerciseType'),
        durationMinutes: Number(form.get('durationMinutes'))
    };
    const status = document.getElementById('training-status');

    setStatus(status, 'Отправляю событие тренировки в Kafka...');

    try {
        const response = await fetch(`${API_BASE}/training/log`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error('training-log-failed');
        setStatus(status, 'Событие принято и отправлено в асинхронный контур.', 'success');
        event.currentTarget.reset();
        event.currentTarget.querySelector('[name="userId"]').value = payload.userId;
    } catch (error) {
        setStatus(status, 'Не удалось отправить событие тренировки.', 'error');
    }
});

loadTips();
loadMealLogs('athlete-01');
