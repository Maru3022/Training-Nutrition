import http from 'k6/http';
import { check } from 'k6';

export default function () {
    // Делаем только ОДИН запрос для проверки связи
    let res = http.get('http://localhost:8083/api/v1/tips');

    console.log(`Response Status: ${res.status}`);
    console.log(`Response Body: ${res.body}`);

    check(res, {
        'Connection successful': (r) => r.status === 200,
    });
}