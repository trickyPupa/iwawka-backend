import exec from 'k6/execution';
import { check, sleep } from 'k6';
import http from 'k6/http';

// Определяем URL в зависимости от окружения (Docker или локальный запуск)
const BASE_URL = __ENV.BASE_URL || 'http://nginx:80';
const AUTH_URL = `${BASE_URL}/api/auth/login`;
const CHAT_URL = `${BASE_URL}/api/chat`;
const MESSAGE_URL = `${BASE_URL}/api/message`;
const USERS_CSV = ('email@mail.ru:12345678,email2@mail.ru:12345678').split(',');

const STAGE_DURATION = __ENV.STAGE_DURATION || '1m';
const STAGE_START_RPS = Number(__ENV.STAGE_START_RPS || '50');
const STAGE_STEP_RPS = Number(__ENV.STAGE_STEP_RPS || '50');
const STAGE_STEPS = Number(__ENV.STAGE_STEPS || '5');
const MAX_VUS = Number(__ENV.MAX_VUS || '200');
const THINK = Number(__ENV.THINK || '0.5');

const P_SEND = Number(__ENV.P_SEND || '0.5');
const P_READ = Number(__ENV.P_READ || '0.3');
const P_NEW = Number(__ENV.P_NEW || '0.2');

// Подготовка стадий роста нагрузки
const stages = Array.from({ length: STAGE_STEPS }, (_, i) => {
  const target = STAGE_START_RPS + STAGE_STEP_RPS * i;
  return { target, duration: STAGE_DURATION };
});

export const options = {
  scenarios: {
    ramping: {
      executor: 'ramping-arrival-rate',
      startRate: STAGE_START_RPS,
      timeUnit: '1s',
      stages,
      preAllocatedVUs: Math.min(MAX_VUS, STAGE_START_RPS * 2),
      maxVUs: MAX_VUS,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(90)<1000', 'p(99)<5000'],
  },
};

function pickUser() {
  const idx = Math.floor(Math.random() * USERS_CSV.length);
  const [email, password] = USERS_CSV[idx].split(':');
  return { email, password };
}

function login() {
  const { email, password } = pickUser();
  const res = http.post(AUTH_URL, JSON.stringify({ email, password }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(res, { 'login ok': (r) => r.status === 200 && r.json('accessToken') });
  echo(res);
  return res.json('accessToken');
}

function authHeaders(token) {
  return { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } };
}

function ensureChat(token) {
  const chatName = `load-chat-${exec.vu.idInTest}`;
  const res = http.post(`${CHAT_URL}`, JSON.stringify({ name: chatName }), authHeaders(token));
  // 409 значит уже есть
  check(res, { 'chat created or exists': (r) => [200, 201, 409].includes(r.status) });
  const chatId = res.json('id') || res.json('chatId');
  return chatId;
}

function sendMessage(token, chatId) {
  const payload = { chatId, content: `hi ${Date.now()}` };
  const res = http.post(MESSAGE_URL, JSON.stringify(payload), authHeaders(token));

  const isOk = res.status < 400;
  check(res, { 'send ok': (r) => isOk });

  if (!isOk) {
    console.error(`[SEND] Failed ${res.status}: ${res.body.substring(0, 200)}`);
  }
}

function readMessages(token, chatId) {
  const res = http.get(`${MESSAGE_URL}/${chatId}`, authHeaders(token));
  check(res, { 'read ok': (r) => r.status < 500 });
}

function readNew(token, chatId) {
  const res = http.get(`${MESSAGE_URL}/${chatId}/new`, authHeaders(token));
  check(res, { 'read new ok': (r) => r.status < 500 });
}

export default function () {
  const token = login();
  if (!token) {
    console.error('Failed to login');
    return;
  }

  const chatId = ensureChat(token);
  if (!chatId) {
    console.error('Failed to get chat ID');
    return;
  }

  const roll = Math.random();
  if (roll < P_SEND) {
    sendMessage(token, chatId);
  } else if (roll < P_SEND + P_READ) {
    readMessages(token, chatId);
  } else {
    readNew(token, chatId);
  }

  if (THINK > 0) sleep(THINK);
}