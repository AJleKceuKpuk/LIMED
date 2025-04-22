// src/components/Logout.js
import React from 'react';
import axiosInstance from './axiosInstance'; 
import { useNavigate } from 'react-router-dom';

const Logout = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const accessToken = localStorage.getItem('accessToken');
      if (!accessToken) {
        return; // Прерываем, если токен отсутствует
      }

      // Используем относительный URL, поэтому базовый URL и withCredentials уже подставятся из axiosInstance
      await axiosInstance.post('/logout');

      // Чистим localStorage от токена и имени пользователя
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');

      // Перенаправляем на страницу входа
      navigate('/login');
    } catch (err) {
      console.error("Ошибка при выполнении logout:", err);
    }
  };

  return (
    <div>
      <button onClick={handleLogout}>Выйти</button>
    </div>
  );
};

export default Logout;
