import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '2m', target: 5000 },  // Плавный подъем до 5к за 2 минуты
        { duration: '2m', target: 10000 }, // Подъем до 10к за следующие 2 минуты
        { duration: '3m', target: 20000 }, // Финальный рывок до 20к
        { duration: '5m', target: 20000 }, // Удержание плато (самый важный этап)
        { duration: '2m', target: 0 },     // Плавный спуск
    ],
    thresholds: {
        http_req_failed: ['rate<0.10'],    // Допускаем до 10% ошибок на таком экстриме
        http_req_duration: ['p(95)<500'], // Ожидаем задержку до 500мс под пиком
    },
};

const BASE_URL = 'http://localhost:8083/api/v1';

export default function () {
    const res = http.get(`${BASE_URL}/tips`);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // Увеличиваем паузу, чтобы снизить риск блокировки портов (TCP Reuse)
    sleep(3);
}