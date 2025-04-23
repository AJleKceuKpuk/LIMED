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
    // Если это запрос на logout – просто добавляем accessToken в заголовок и возвращаем конфигурацию.
    if (config.url && config.url.endsWith('/logout')) {
      const accessToken = localStorage.getItem('accessToken');
      if (accessToken) {
        config.headers['Authorization'] = `Bearer ${accessToken}`;
      }
      return config;
    }

    // Проверяем refresh-токен
    const storedRefresh = localStorage.getItem('refreshToken');
    if (storedRefresh) {
      try {
        const decodedRefresh = jwtDecode(storedRefresh);
        if (decodedRefresh.exp * 1000 < Date.now()) {
          document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
          localStorage.removeItem('refreshToken');
          return Promise.reject(new Error("Refresh token expired"));
        }
      } catch (error) {
        document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        localStorage.removeItem('refreshToken');
        return Promise.reject(error);
      }
    }
    
    let accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      try {
        const decoded = jwtDecode(accessToken);
        if (decoded.exp * 1000 < Date.now()) {
          if (!isRefreshing) {
            isRefreshing = true;
            try {
              const response = await axios.post(
                '/token/refresh',
                {},
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
              return Promise.reject(error);
            }
          }
          const retryOriginalRequest = new Promise((resolve) => {
            addRefreshSubscriber((newToken) => {
              config.headers['Authorization'] = `Bearer ${newToken}`;
              resolve(config);
            });
          });
          return retryOriginalRequest;
        } else {
          config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
      } catch (err) {
        // Если произошла ошибка при декодировании — просто возвращаем конфигурацию
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response-интерцептор для обработки 403 ошибки
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 403) {
      // Если получаем 403, значит у токенов проблема (refresh отозван, пользователь забанен и т.п.)
      // Очищаем refresh-токен как из cookie, так и из localStorage
      document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      localStorage.removeItem('refreshToken');
      // Очищаем остальные данные авторизации
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      // Перенаправляем пользователя на страницу входа
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
