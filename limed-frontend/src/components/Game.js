import React, { useState, useEffect } from 'react';
import axiosInstance from './axiosInstance'; 
import { useNavigate } from 'react-router-dom';

const Game = () => {
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const checkAccess = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        // Выполняем запрос к защищённому эндпоинту /game
        await axiosInstance.get('/game', {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true, // Этот параметр можно не передавать, если он уже задан в axiosInstance
        });
        // Если запрос прошёл без ошибок, убираем индикатор загрузки
        setLoading(false);
      } catch (error) {
        setLoading(false);
        // Если сервер возвращает 403, предполагаем, что доступ ограничен (например, из-за бана)
        if (error.response && error.response.status === 403) {
          // Считываем и отображаем сообщение, переданное с сервера
          const serverMessage = error.response.data;
          setErrorMessage(serverMessage || 'Доступ к игре ограничен.');
        } else {
          // Для других ошибок выводим общее сообщение
          setErrorMessage('Произошла ошибка при проверке доступа к игре.');
        }
      }
    };

    checkAccess();
  }, [navigate]);

  if (loading) {
    return <p>Загрузка...</p>;
  }

  if (errorMessage) {
    return (
      <div>
        <h2>Ошибка доступа</h2>
        <p>{errorMessage}</p>
        {/* Здесь можно предложить пользователю выполнить logout — кнопка для выхода уже может быть реализована в NavBar */}
      </div>
    );
  }

  return (
    <div>
      <h2>Добро пожаловать в игру!</h2>
      {/* Здесь размещается основной игровой контент */}
    </div>
  );
};

export default Game;
