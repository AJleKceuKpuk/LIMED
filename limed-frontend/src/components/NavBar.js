// src/components/NavBar.js
import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from './axiosInstance'; 

const NavBar = () => {
  // Изначально берем значения из localStorage
  const [username, setUsername] = useState(localStorage.getItem('username'));
  const [token, setToken] = useState(localStorage.getItem('accessToken'));
  // Состояние для проверки прав администратора
  const [isAdmin, setIsAdmin] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  // Обновляем username и token при изменении маршрута
  useEffect(() => {
    setUsername(localStorage.getItem('username'));
    setToken(localStorage.getItem('accessToken'));
  }, [location]);

  // Слушаем глобальное событие "usernameUpdated" при обновлении имени или токена
  useEffect(() => {
    const handleUsernameUpdated = () => {
      setUsername(localStorage.getItem('username'));
      setToken(localStorage.getItem('accessToken'));
    };

    window.addEventListener('usernameUpdated', handleUsernameUpdated);
    return () => {
      window.removeEventListener('usernameUpdated', handleUsernameUpdated);
    };
  }, []);

  // Проверяем доступ к эндпоинту /admin/get-allusers при изменении token или username
  useEffect(() => {
    if (token) {
      axiosInstance
        .get('/admin/get-allusers', {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true, // этот флаг уже установлен в axiosInstance по умолчанию
        })
        .then(response => {
          if (response.status === 200) {
            setIsAdmin(true);
          } else {
            setIsAdmin(false);
          }
        })
        .catch(error => {
          // Если произошла ошибка, доступ не установлен
          setIsAdmin(false);
        });
    } else {
      setIsAdmin(false);
    }
  }, [token, username]); // добавлена зависимость от username

  const handleLogout = async () => {
    try {
      if (!token) return;
      await axiosInstance.post(
        '/logout',
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true, // не обязательно, поскольку уже прописано в axiosInstance
        }
      );

      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      setUsername(null);
      setToken(null);
      navigate('/login');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <nav
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        padding: '10px 20px',
        backgroundColor: '#f0f0f0',
      }}
    >
      <div>
        {username ? (
          <>
            {/* Кнопка для перехода на страницу профиля */}
            <Link
              to="/user/profile"
              style={{
                marginRight: '10px',
                padding: '8px 12px',
                backgroundColor: '#007bff',
                color: '#fff',
                borderRadius: '4px',
                textDecoration: 'none',
              }}
            >
              Профиль
            </Link>
            {/* Кнопка "Админ" отрисовывается только если проверка доступа успешна */}
            {isAdmin && (
              <Link
                to="/admin"
                style={{
                  marginRight: '10px',
                  padding: '8px 12px',
                  backgroundColor: '#28a745',
                  color: '#fff',
                  borderRadius: '4px',
                  textDecoration: 'none',
                }}
              >
                Админ
              </Link>
            )}
            <button onClick={handleLogout}>Выйти</button>
          </>
        ) : (
          <>
            <Link to="/login" style={{ marginRight: '10px' }}>
              Вход
            </Link>
            <Link to="/registration">Регистрация</Link>
          </>
        )}
      </div>
      {username && <div style={{ fontWeight: 'bold' }}>{username}</div>}
    </nav>
  );
};

export default NavBar;
