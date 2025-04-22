// src/components/AdminPage.js
import React, { useState, useEffect } from 'react';
import axiosInstance from './axiosInstance'; 
import { Link } from 'react-router-dom';

const AdminPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // При монтировании компонента получаем список пользователей
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    axiosInstance
      .get('/admin/get-allusers', {
        headers: { Authorization: `Bearer ${token}` },
        withCredentials: true, // Этот параметр можно убрать, если он уже прописан в axiosInstance
      })
      .then(response => {
        setUsers(response.data);
        setLoading(false);
      })
      .catch(err => {
        setError(err);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>Загрузка пользователей...</div>;
  }

  if (error) {
    return <div>Ошибка: {error.message}</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>Административная страница</h1>
      <h2>Список пользователей</h2>
      <table
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          border: '1px solid #ccc',
          marginTop: '20px',
        }}
      >
        <thead>
          <tr>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>ID</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Username</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Email</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Status</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Last Activity</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Date Registration</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Roles</th>
            <th style={{ border: '1px solid #ccc', padding: '8px' }}>Blocking</th>
          </tr>
        </thead>
        <tbody>
          {users.map(user => {
            // Для блокировок фильтруем активные блокировки типа "ban" и "mut"
            const banBlock =
              Array.isArray(user.blocking) &&
              user.blocking.find(
                block =>
                  block.blockingType.toLowerCase() === 'ban' && !block.revokedBlock
              );
            const mutBlock =
              Array.isArray(user.blocking) &&
              user.blocking.find(
                block =>
                  block.blockingType.toLowerCase() === 'mut' && !block.revokedBlock
              );

            return (
              <tr key={user.id}>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {user.id}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {/* Имя пользователя в виде кнопки-перехода */}
                  <Link
                    to={`/admin/get-user/${user.id}`}
                    style={{
                      padding: '4px 8px',
                      backgroundColor: '#ffc107',
                      borderRadius: '4px',
                      textDecoration: 'none',
                      color: '#000',
                    }}
                  >
                    {user.username}
                  </Link>
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {user.email}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {user.status}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {user.lastActivity ? new Date(user.lastActivity).toLocaleString() : '-'}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {user.dateRegistration
                    ? new Date(user.dateRegistration).toLocaleDateString()
                    : '-'}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {Array.isArray(user.roles) ? user.roles.join(', ') : '-'}
                </td>
                <td style={{ border: '1px solid #ccc', padding: '8px' }}>
                  {banBlock || mutBlock ? (
                    <>
                      {banBlock && (
                        <div>
                          <strong>Ban:</strong>
                          <div>
                            Start: {banBlock.startTime ? new Date(banBlock.startTime).toLocaleString() : '-'}
                          </div>
                          <div>
                            End: {banBlock.endTime ? new Date(banBlock.endTime).toLocaleString() : '-'}
                          </div>
                          <div>Reason: {banBlock.reason}</div>
                        </div>
                      )}
                      {mutBlock && (
                        <div style={{ marginTop: '5px' }}>
                          <strong>Mut:</strong>
                          <div>
                            Start: {mutBlock.startTime ? new Date(mutBlock.startTime).toLocaleString() : '-'}
                          </div>
                          <div>
                            End: {mutBlock.endTime ? new Date(mutBlock.endTime).toLocaleString() : '-'}
                          </div>
                          <div>Reason: {mutBlock.reason}</div>
                        </div>
                      )}
                    </>
                  ) : (
                    '-'
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default AdminPage;
