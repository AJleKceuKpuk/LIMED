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
        // Если access токен истёк, пытаемся его обновить
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

// Response-интерцептор для обработки ошибок
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    // Если ошибка 403, проверяем URL запроса
    if (
      error.response &&
      error.response.status === 403 &&
      (!error.config || !error.config.url || !error.config.url.includes('/admin/get-allusers'))
    ) {
      // Если это не запрос к /admin/get-allusers, очищаем refresh-токен и данные авторизации,
      // затем перенаправляем на страницу входа.
      document.cookie = "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      window.location.href = '/login';
    }
    // Для запроса к /admin/get-allusers с ошибкой 403 просто вернём ошибку,
    // чтобы можно было обработать её в компоненте без разлогинивания пользователя.
    return Promise.reject(error);
  }
);

export default axiosInstance;
