import React, { useState, useEffect } from 'react';
import axiosInstance from './axiosInstance'; 
import { useParams, Link } from 'react-router-dom';

const UserDetail = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading]  = useState(true);
  const [error, setError]      = useState(null);
  // Общее сообщение для уведомлений (успех/ошибка)
  const [message, setMessage]  = useState('');
  
  const token = localStorage.getItem('accessToken');

  // Получаем данные пользователя по id
  useEffect(() => {
    axiosInstance.get(`/admin/get-user/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
      // withCredentials уже прописан в axiosInstance
    })
    .then(response => {
      setUser(response.data);
      setLoading(false);
    })
    .catch(err => {
      setError(err);
      setLoading(false);
    });
  }, [id, token]);

  if (loading) {
    return <div>Загрузка пользователя...</div>;
  }
  
  if (error) {
    return <div>Ошибка: {error.message}</div>;
  }

  // Обработчик изменения имени пользователя
  const handleEditUsername = async () => {
    const newUsername = prompt("Введите новое имя пользователя:", user.username);
    if (!newUsername || newUsername === user.username) return;
    try {
      const response = await axiosInstance.put(
        `/admin/edit-username/${user.id}`,
        { newUsername },
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true, // уже задан в axiosInstance
        }
      );
      setMessage(response.data); // Ответ сервера (строка)
      setUser({ ...user, username: newUsername });
    } catch (err) {
      console.error(err);
      setMessage("Ошибка при изменении имени пользователя");
    }
  };

  // Обработчик изменения Email
  const handleEditEmail = async () => {
    const newEmail = prompt("Введите новый Email:", user.email);
    if (!newEmail || newEmail === user.email) return;
    try {
      const response = await axiosInstance.put(
        `/admin/edit-email/${user.id}`,
        { newEmail },
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true,
        }
      );
      setMessage(response.data);
      setUser({ ...user, email: newEmail });
    } catch (err) {
      console.error(err);
      setMessage("Ошибка при изменении Email");
    }
  };

  // Обработчик изменения ролей
  const handleEditRole = async () => {
    const input = prompt(
      "Введите роли через запятую (например, ROLE_USER, ROLE_ADMIN):",
      user.roles ? user.roles.join(', ') : ''
    );
    if (input === null) return;
    const rolesArray = input.split(',').map(r => r.trim()).filter(r => r);
    if (rolesArray.length === 0) return;
    try {
      const response = await axiosInstance.put(
        `/admin/edit-role/${user.id}`,
        { userId: user.id, roles: rolesArray },
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true,
        }
      );
      setMessage(response.data);
      setUser({ ...user, roles: rolesArray });
    } catch (err) {
      console.error(err);
      setMessage("Ошибка при изменении ролей");
    }
  };

  // Обработчик выдачи блокировки
  const handleGiveBlock = async () => {
    const blockingType = prompt("Введите тип блокировки (например, ban или mut):", "");
    if (!blockingType) return;
    const duration = prompt("Введите длительность блокировки (например, 24h, 7d):", "");
    if (!duration) return;
    const reason = prompt("Введите причину блокировки:", "");
    if (reason === null) return;
    try {
      const response = await axiosInstance.post(
        `/admin/give-blocked`,
        { username: user.username, blockingType, duration, reason },
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true,
        }
      );
      setMessage(response.data);
    } catch (err) {
      console.error(err);
      setMessage("Ошибка при выдаче блокировки");
    }
  };

  // Обработчик разблокировки
  const handleUnblock = async () => {
    const blockingType = prompt("Введите тип блокировки для разблокировки (например, ban или mut):", "");
    if (!blockingType) return;
    try {
      const response = await axiosInstance.post(
        `/admin/unblock`,
        { username: user.username, blockingType },
        {
          headers: { Authorization: `Bearer ${token}` },
          // withCredentials: true,
        }
      );
      setMessage(response.data);
    } catch (err) {
      console.error(err);
      setMessage("Ошибка при разблокировке пользователя");
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Детали пользователя</h1>
      {message && <div style={{ color: 'blue', marginBottom: '10px' }}>{message}</div>}
      <p><strong>ID:</strong> {user.id}</p>
      <p>
        <strong>Имя пользователя:</strong> {user.username}{" "}
        <button 
          onClick={handleEditUsername}
          style={{ padding: '4px 8px', marginLeft: '10px', backgroundColor: '#ffc107', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Изменить имя
        </button>
      </p>
      <p>
        <strong>Email:</strong> {user.email}{" "}
        <button 
          onClick={handleEditEmail}
          style={{ padding: '4px 8px', marginLeft: '10px', backgroundColor: '#ffc107', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Изменить Email
        </button>
      </p>
      <p>
        <strong>Status:</strong> {user.status}
      </p>
      <p>
        <strong>Last Activity:</strong>{" "}
        {user.lastActivity ? new Date(user.lastActivity).toLocaleString() : '-'}
      </p>
      <p>
        <strong>Date Registration:</strong>{" "}
        {user.dateRegistration ? new Date(user.dateRegistration).toLocaleDateString() : '-'}
      </p>
      <p>
        <strong>Roles:</strong>{" "}
        {user.roles && user.roles.length > 0 ? user.roles.join(', ') : '-'}{" "}
        <button 
          onClick={handleEditRole}
          style={{ padding: '4px 8px', marginLeft: '10px', backgroundColor: '#ffc107', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Редактировать роли
        </button>
      </p>
      <div>
        <strong>Blocking:</strong>
        {user.blocking && user.blocking.length > 0 ? (
          user.blocking.map((block, index) => (
            <div 
              key={index} 
              style={{ marginBottom: '8px', padding: '8px', border: '1px solid #ccc' }}
            >
              <p><strong>ID:</strong> {block.id}</p>
              <p><strong>Тип:</strong> {block.blockingType}</p>
              <p>
                <strong>Start Time:</strong>{" "}
                {block.startTime ? new Date(block.startTime).toLocaleString() : '-'}
              </p>
              <p>
                <strong>End Time:</strong>{" "}
                {block.endTime ? new Date(block.endTime).toLocaleString() : '-'}
              </p>
              <p><strong>Reason:</strong> {block.reason}</p>
            </div>
          ))
        ) : (
          <p>-</p>
        )}
      </div>
      <div style={{ marginTop: '20px' }}>
        <button 
          onClick={handleGiveBlock}
          style={{ padding: '8px 12px', marginRight: '10px', backgroundColor: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Выдать блокировку
        </button>
        <button 
          onClick={handleUnblock}
          style={{ padding: '8px 12px', backgroundColor: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          Разблокировать пользователя
        </button>
      </div>
      <Link 
        to="/admin" 
        style={{
          display: 'inline-block',
          marginTop: '20px',
          padding: '8px 12px',
          backgroundColor: '#007bff',
          color: '#fff',
          borderRadius: '4px',
          textDecoration: 'none'
        }}
      >
        Назад к администрированию
      </Link>
    </div>
  );
};

export default UserDetail;
