// src/components/UserProfile.js
import React, { useState, useEffect } from 'react';
import axiosInstance from './axiosInstance'; 

const UserProfile = () => {
  // Состояния для профиля, загрузки и ошибок
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Состояния для обновления имени
  const [newUsername, setNewUsername] = useState('');
  const [usernameMessage, setUsernameMessage] = useState('');
  
  // Состояния для обновления email
  const [newEmail, setNewEmail] = useState('');
  const [emailMessage, setEmailMessage] = useState('');
  
  // Состояния для обновления пароля
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  
  // Получаем access token из localStorage
  const token = localStorage.getItem('accessToken');

  // Получаем профиль пользователя при монтировании компонента
  useEffect(() => {
    axiosInstance.get('/user/profile', {
      headers: { Authorization: `Bearer ${token}` },
    })
    .then(response => {
      setProfile(response.data);
      // Предзаполнение значениями для форм обновления
      setNewUsername(response.data.username);
      setNewEmail(response.data.email);
      setLoading(false);
    })
    .catch(err => {
      setError(err);
      setLoading(false);
    });
  }, [token]);

  // Обновление имени пользователя
  const handleUsernameUpdate = (e) => {
    e.preventDefault();
    axiosInstance.put(
      '/user/update-username',
      { newUsername },
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
    .then(response => {
      // Ожидается response.data = { accessToken: '...' }
      const updatedToken = response.data.accessToken;
      // Обновляем access token в localStorage
      localStorage.setItem('accessToken', updatedToken);
      // Обновляем новое имя в localStorage
      localStorage.setItem('username', newUsername);
      // Диспатчим глобальное событие, чтобы NavBar обновил отображаемое имя
      window.dispatchEvent(new Event('usernameUpdated'));
      // Обновляем профиль и сообщение
      setProfile(prev => ({ ...prev, username: newUsername }));
      setUsernameMessage('Имя пользователя успешно обновлено.');
    })
    .catch(err => {
      console.error(err);
      const errorMsg = (err.response && err.response.data) || 'Ошибка при обновлении имени пользователя.';
      setUsernameMessage(errorMsg);
    });
  };

  // Обновление email
  const handleEmailUpdate = (e) => {
    e.preventDefault();
    axiosInstance.put(
      '/user/update-email',
      { newEmail },
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
    .then(response => {
      // В ответ ожидается строка, подтверждающая, что email изменён
      setProfile(prev => ({ ...prev, email: newEmail }));
      setEmailMessage(response.data || 'Email успешно обновлен.');
    })
    .catch(err => {
      console.error(err);
      const errorMsg = (err.response && err.response.data) || 'Ошибка при обновлении Email.';
      setEmailMessage(errorMsg);
    });
  };

  // Обновление пароля
  const handlePasswordUpdate = (e) => {
    e.preventDefault();
    axiosInstance.put(
      '/user/update-password',
      { oldPassword, newPassword },
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    )
    .then(response => {
      // Ожидается строка, подтверждающая успешное обновление пароля
      setPasswordMessage(response.data || 'Пароль успешно обновлен.');
      // Очищаем поля пароля
      setOldPassword('');
      setNewPassword('');
    })
    .catch(err => {
      console.error(err);
      const errorMsg = (err.response && err.response.data) || 'Ошибка при обновлении пароля.';
      setPasswordMessage(errorMsg);
    });
  };

  if (loading) {
    return <div>Загрузка...</div>;
  }
  
  if (error) {
    return <div>Ошибка: {error.message}</div>;
  }
  
  return (
    <div>
      <h1>Профиль пользователя</h1>
      <div>
        <strong>Имя пользователя:</strong> {profile.username}
      </div>
      <div>
        <strong>Email:</strong> {profile.email}
      </div>
      <div>
        <strong>Дата регистрации:</strong> {profile.dateRegistration}
      </div>
      
      <hr />
      
      {/* Форма обновления имени */}
      <h2>Обновить имя пользователя</h2>
      {usernameMessage && <p>{usernameMessage}</p>}
      <form onSubmit={handleUsernameUpdate}>
        <div>
          <label>
            Новое имя:
            <input
              type="text"
              value={newUsername}
              onChange={(e) => setNewUsername(e.target.value)}
            />
          </label>
        </div>
        <button type="submit">Обновить имя</button>
      </form>
      
      <hr />
      
      {/* Форма обновления email */}
      <h2>Обновить Email</h2>
      {emailMessage && <p>{emailMessage}</p>}
      <form onSubmit={handleEmailUpdate}>
        <div>
          <label>
            Новый Email:
            <input
              type="email"
              value={newEmail}
              onChange={(e) => setNewEmail(e.target.value)}
            />
          </label>
        </div>
        <button type="submit">Обновить Email</button>
      </form>
      
      <hr />
      
      {/* Форма обновления пароля */}
      <h2>Обновить пароль</h2>
      {passwordMessage && <p>{passwordMessage}</p>}
      <form onSubmit={handlePasswordUpdate}>
        <div>
          <label>
            Старый пароль:
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
            />
          </label>
        </div>
        <div>
          <label>
            Новый пароль:
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </label>
        </div>
        <button type="submit">Обновить пароль</button>
      </form>
    </div>
  );
};

export default UserProfile;
