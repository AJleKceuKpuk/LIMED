// src/axiosConfig.js
import axios from 'axios';
import { createBrowserHistory } from 'history';

const history = createBrowserHistory();

// Создаем экземпляр axios с базовыми настройками
const instance = axios.create({
  baseURL: '/', // Используем относительный базовый URL
  withCredentials: true,
});

// Интерцептор для обработки всех ответов
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    // Если сервер возвращает статус 403 – значит, пользователь забанен или токен недействителен
    if (error.response && error.response.status === 403) {
      console.error(
        'Доступ запрещён (403). Возможно, пользователь забанен или токен недействителен.'
      );
      
      // Опционально можно показать уведомление пользователю,
      // например, с помощью библиотеки уведомлений:
      // alert('Ваш доступ ограничен. Пожалуйста, свяжитесь с поддержкой или повторно авторизуйтесь.');

      // Очистим данные авторизации в localStorage
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');

      // Перенаправление на страницу входа
      history.push('/login');
    }
    return Promise.reject(error);
  }
);

export default instance;
