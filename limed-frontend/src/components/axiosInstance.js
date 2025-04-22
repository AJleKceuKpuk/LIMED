// src/axiosInstance.js
import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

// Флаги для управления обновлением токена
let isRefreshing = false;
let refreshSubscribers = [];

// Функция для уведомления всех ожидающих запросов о новом токене
function onRefreshed(newAccessToken) {
  refreshSubscribers.forEach((callback) => callback(newAccessToken));
  refreshSubscribers = [];
}

// Функция для добавления запроса в очередь ожидания обновления токена
function addRefreshSubscriber(callback) {
  refreshSubscribers.push(callback);
}

const axiosInstance = axios.create({
  baseURL: 'https://192.168.0.180:8443',
  withCredentials: true,
});

// Интерцептор запроса с проверкой и обновлением токенов
axiosInstance.interceptors.request.use(
  async (config) => {
    // Сначала проверяем refresh токен (из localStorage)
    const storedRefresh = localStorage.getItem('refreshToken');
    if (storedRefresh) {
      try {
        const decodedRefresh = jwtDecode(storedRefresh);
        if (decodedRefresh.exp * 1000 < Date.now()) {
          // Если refresh токен истёк, очищаем его из cookie и localStorage
          document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
          localStorage.removeItem('refreshToken');
          return Promise.reject(new Error("Refresh token expired"));
        }
      } catch (error) {
        console.error("Ошибка декодирования refresh токена", error);
        document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        localStorage.removeItem('refreshToken');
        return Promise.reject(error);
      }
    }
    
    // Получаем access токен из localStorage
    let accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      try {
        const decoded = jwtDecode(accessToken);
        // Если access токен истёк
        if (decoded.exp * 1000 < Date.now()) {
          if (!isRefreshing) {
            isRefreshing = true;
            try {
              // Отправляем запрос на обновление access токена, передавая его в заголовке
              const response = await axios.post(
                '/token/refresh',
                {}, // тело запроса пустое
                {
                  headers: { Authorization: `Bearer ${accessToken}` },
                  withCredentials: true,
                  baseURL: axiosInstance.defaults.baseURL,
                }
              );
              accessToken = response.data.accessToken;
              localStorage.setItem('accessToken', accessToken);
              isRefreshing = false;
              onRefreshed(accessToken);
            } catch (error) {
              isRefreshing = false;
              console.error('Ошибка при обновлении токена:', error);
              return Promise.reject(error);
            }
          }
          // Формируем промис, который дождется завершения обновления токена
          const retryOriginalRequest = new Promise((resolve) => {
            addRefreshSubscriber((newToken) => {
              config.headers['Authorization'] = `Bearer ${newToken}`;
              resolve(config);
            });
          });
          return retryOriginalRequest;
        } else {
          // Если access токен не истёк – добавляем его в заголовок
          config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
      } catch (err) {
        console.error('Ошибка при декодировании access токена:', err);
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default axiosInstance;
