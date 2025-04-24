import React, { useState } from 'react';
import axiosInstance from './axiosInstance'; 
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Отправляем логин-запрос. Предполагается, что сервер возвращает accessToken.
      const response = await axiosInstance.post('/login', { username, password });
      
      // Сохраняем accessToken и username; id не передаём при логине.
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('username', username);
      
      // Переход на главную (например, на страницу игры)
      navigate('/game');
    } catch (err) {
      setError('Ошибка при входе. Проверьте имя пользователя и пароль.');
    }
  };

  return (
    <div>
      <h2>Вход в систему</h2>
      <form onSubmit={handleLogin}>
        <div>
          <label>Имя пользователя:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Пароль:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit">Войти</button>
      </form>
    </div>
  );
};

export default Login;
