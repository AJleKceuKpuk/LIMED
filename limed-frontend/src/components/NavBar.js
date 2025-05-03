import React, { useEffect, useState, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import axiosInstance from './axiosInstance';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const NavBar = () => {
  // Считываем username и token из localStorage; userId будет получен из профиля
  const [username, setUsername] = useState(localStorage.getItem('username'));
  const [token, setToken] = useState(localStorage.getItem('accessToken'));
  const [userId, setUserId] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [userStatus, setUserStatus] = useState('offline');

  const location = useLocation();
  const navigate = useNavigate();

  // Rеф для хранения экземпляра STOMP-клиента
  const stompClientRef = useRef(null);
  // Флаг, чтобы подписка происходила только один раз
  const hasSubscribedRef = useRef(false);
  // Реф для актуального значения userId, используемый глобальным обработчиком кликов.
  const userIdRef = useRef(userId);
  useEffect(() => {
    userIdRef.current = userId;
  }, [userId]);

  // Обновляем username и token при изменении маршрута
  useEffect(() => {
    setUsername(localStorage.getItem('username'));
    setToken(localStorage.getItem('accessToken'));
  }, [location]);

  // Обработчик события, если вы используете глобальное обновление данных (например, через событие "usernameUpdated")
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

  // Если токен присутствует, но userId еще не получен, запрашиваем профиль
  useEffect(() => {
    if (token && !userId) {
      axiosInstance
        .get('/user/profile', {
          headers: { Authorization: `Bearer ${token}` }
        })
        .then(response => {
          // Предполагается, что профиль содержит поле id (числовой)
          if (response.data && response.data.id) {
            setUserId(response.data.id);
            localStorage.setItem('userId', response.data.id); // опционально
          }
        })
        .catch(() => {
          // обработка ошибки, если потребуется
        });
    }
  }, [token, userId]);

  // Проверка прав администратора
  useEffect(() => {
    if (token) {
      axiosInstance
        .get('/admin/get-allusers', {
          headers: { Authorization: `Bearer ${token}` }
        })
        .then(response => setIsAdmin(response.status === 200))
        .catch(() => setIsAdmin(false));
    } else {
      setIsAdmin(false);
    }
  }, [token, username]);

  // Подключение к WebSocket через SockJS и STOMP, если userId получен и подписка еще не создана
  useEffect(() => {
    if (!userId) return;
    if (hasSubscribedRef.current) return;
    
    const socket = new SockJS(`https://localhost:8443/ws/online?userId=${userId}`);
    const stompClient = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        stompClientRef.current = stompClient;
        stompClient.subscribe(`/ws/online/users/${userId}`, (message) => {
          if (message.body) {
            try {
              const payload = JSON.parse(message.body);
              if (payload.status) {
                setUserStatus(payload.status);
              }
            } catch (error) {
              // ошибка разбора без вывода в консоль
            }
          }
        });
        hasSubscribedRef.current = true;
        const onlineMessage = { userId, status: "online" };
        stompClient.publish({
          destination: '/ws/online/update',
          body: JSON.stringify(onlineMessage)
        });
      },
      onStompError: () => {
        // обработка ошибки без вывода в консоль
      }
    });
    stompClient.activate();

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        hasSubscribedRef.current = false;
      }
    };
  }, [userId]);

  // Глобальный обработчик кликов – регистрируется один раз при монтировании,
  // использует актуальное значение userId из userIdRef
  useEffect(() => {
    const handleClick = () => {
      if (stompClientRef.current && stompClientRef.current.connected) {
        const onlineMessage = { userId: userIdRef.current, status: "online" };
        stompClientRef.current.publish({
          destination: '/ws/online/update',
          body: JSON.stringify(onlineMessage)
        });
      }
    };

    document.addEventListener('click', handleClick);
    return () => {
      document.removeEventListener('click', handleClick);
    };
  }, []);

  const handleLogout = async () => {
    try {
      if (!token) return;
      await axiosInstance.post('/logout', {}, { headers: { Authorization: `Bearer ${token}` } });
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      setUsername(null);
      setUserId(null);
      setToken(null);
      navigate('/login');
    } catch (err) {
      // обработка ошибки без вывода в консоль
    }
  };

  return (
    <nav style={{
      display: 'flex',
      justifyContent: 'space-between',
      padding: '10px 20px',
      backgroundColor: '#f0f0f0'
    }}>
      <div>
        {username ? (
          <>
            <Link
              to="/user/profile"
              style={{
                marginRight: '10px',
                padding: '8px 12px',
                backgroundColor: '#007bff',
                color: '#fff',
                borderRadius: '4px',
                textDecoration: 'none'
              }}
            >
              Профиль
            </Link>
            {isAdmin && (
              <Link
                to="/admin"
                style={{
                  marginRight: '10px',
                  padding: '8px 12px',
                  backgroundColor: '#28a745',
                  color: '#fff',
                  borderRadius: '4px',
                  textDecoration: 'none'
                }}
              >
                Админ
              </Link>
            )}
            <button onClick={handleLogout}>Выйти</button>
          </>
        ) : (
          <>
            <Link to="/login" style={{ marginRight: '10px' }}>Вход</Link>
            <Link to="/registration">Регистрация</Link>
          </>
        )}
      </div>
      {username && (
        <div style={{ fontWeight: 'bold' }}>
          {username} ({userStatus})
        </div>
      )}
    </nav>
  );
};

export default NavBar;
