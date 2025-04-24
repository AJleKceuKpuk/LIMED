import axios from "axios";
import { jwtDecode } from "jwt-decode"; 

// Создаём базовый экземпляр Axios
const api = axios.create({
  baseURL: "http://192.168.174.239:8080", // URL вашего API
  withCredentials: true, // Автоматически передаём куки с запросами
});

// Глобальные переменные для управления обновлением токена
let isRefreshing = false; // Флаг для предотвращения повторных запросов на обновление
let refreshSubscribers = []; // Очередь для запросов, ожидающих обновления токена

// Функция уведомления подписчиков после получения нового токена
const onTokenRefreshed = (newToken) => {
  refreshSubscribers.forEach((callback) => callback(newToken));
  refreshSubscribers = [];
};

// Функция добавления запросов в очередь подписчиков
const addRefreshSubscriber = (callback) => {
  refreshSubscribers.push(callback);
};

// Проверка истечения токена
const isTokenExpired = (token) => {
  if (!token) return true; // Если токена нет, считаем его истёкшим
  const { exp } = jwtDecode(token); // Расшифровываем токен для получения времени истечения
  const currentTime = Math.floor(Date.now() / 1000); // Текущее время в секундах
  return exp < currentTime; // Истёк ли токен
};

// Функция обновления токена
const refreshAccessToken = async (currentAccessToken) => {
  const response = await api.post(
    "/token/refresh", // Серверный эндпоинт обновления токенов
    { accessToken: currentAccessToken }, // Передаём старый токен
    { withCredentials: true } // Отправляем refresh token через cookie
  );
  return response.data.accessToken; // Возвращаем новый токен
};

// Interceptor для проверки и обновления токена
api.interceptors.request.use(async (config) => {
  // Исключаем эндпоинты, где токен не нужен (например, /login или /registration)
  if (
    config.url.includes("/login") ||
    config.url.includes("/registration")
  ) {
    return config; // Пропускаем такие запросы без проверки токена
  }

  const accessToken = localStorage.getItem("accessToken");

  // Проверяем, истёк ли токен
  if (isTokenExpired(accessToken)) {
    console.log("Токен истёк, обновляем...");

    // Если токен уже обновляется, ждём завершения текущего запроса
    if (!isRefreshing) {
      isRefreshing = true;

      try {
        const newToken = await refreshAccessToken(accessToken); // Выполняем запрос обновления токена
        localStorage.setItem("accessToken", newToken); // Сохраняем новый токен
        onTokenRefreshed(newToken); // Уведомляем подписчиков
        isRefreshing = false;
      } catch (error) {
        console.error("Ошибка при обновлении токена:", error);
        isRefreshing = false;
        localStorage.removeItem("accessToken"); // Удаляем токен в случае ошибки
        throw error; // Прерываем запросы
      }
    }

    // Добавляем текущий запрос в очередь подписчиков
    return new Promise((resolve) => {
      addRefreshSubscriber((newToken) => {
        config.headers.Authorization = `Bearer ${newToken}`; // Добавляем новый токен в заголовок
        resolve(config); // Возобновляем выполнение запроса
      });
    });
  }

  // Если токен действителен, добавляем его в заголовок Authorization
  config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

// Экспортируем настроенный экземпляр Axios
export default api;
