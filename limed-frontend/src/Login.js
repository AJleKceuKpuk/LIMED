import React, { useState } from "react";
import { useAuth } from "./AuthContext";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [credentials, setCredentials] = useState({ username: "", password: "" });
  const [error, setError] = useState(null);
  const { loginUser } = useAuth(); // Берём loginUser из контекста
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // Сброс ошибки
    try {
      await loginUser(credentials); // Логиним пользователя
      navigate("/"); // Перенаправляем на главную страницу
    } catch (error) {
      setError("Ошибка авторизации. Проверьте логин и пароль."); // Показываем ошибку
    }
  };

  return (
    <div>
      <h2>Авторизация</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Имя пользователя:</label>
          <input
            type="text"
            value={credentials.username}
            onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
            required
          />
        </div>
        <div>
          <label>Пароль:</label>
          <input
            type="password"
            value={credentials.password}
            onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
            required
          />
        </div>
        <button type="submit">Войти</button>
      </form>
    </div>
  );
}
