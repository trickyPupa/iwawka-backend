import http from 'k6/http';
import { check, sleep } from 'k6';

const TARGET = __ENV.TARGET_URL || 'http://localhost:8080/health';
const METHOD = (__ENV.METHOD || 'GET').toUpperCase();
const BODY = __ENV.BODY || null;
const HEADERS = __ENV.HEADERS ? JSON.parse(__ENV.HEADERS) : {};
const THINK = Number(__ENV.THINK || '0');

const RATE = Number(__ENV.RATE || '50');          // запросов в секунду
const DURATION = __ENV.DURATION || '1m';          // длительность теста
const TIME_UNIT = __ENV.TIME_UNIT || '1s';        // шаг для rate
const PRE_VUS = Number(__ENV.PRE_VUS || Math.max(RATE, 20));
const MAX_VUS = Number(__ENV.MAX_VUS || Math.max(RATE * 2, 50));

export const options = {
  scenarios: {
    constant_rps: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: TIME_UNIT,
      duration: DURATION,
      preAllocatedVUs: PRE_VUS,
      maxVUs: MAX_VUS,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(90)<1000'],
  },
};

export default function () {
  const res = http.request(METHOD, TARGET, BODY, { headers: HEADERS });
  check(res, {
    'status < 400': (r) => r.status && r.status < 400,
  });
  if (THINK > 0) sleep(THINK);
}