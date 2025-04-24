import React, { useState } from "react";
import { useAuth } from "./AuthContext";
import { useNavigate } from "react-router-dom";

export default function Register() {
  const { registerUser } = useAuth();
  const navigate = useNavigate();

  const [credentials, setCredentials] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // Сбрасываем ошибку перед отправкой
    try {
      await registerUser(credentials);
      navigate("/");
    } catch (error) {
      // Обрабатываем ошибку, возвращённую сервером
      if (error.response && error.response.data) {
        setError(error.response.data); // Используем сообщение из тела ответа
      } else {
        setError("Что-то пошло не так. Попробуйте ещё раз."); // Общая ошибка
      }
    }
  };

  return (
    <div>
      <h2>Регистрация</h2>
      {/* Выводим сообщение об ошибке, если оно есть */}
      {error && <p style={{ color: "red" }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        <div>
          <label>Имя пользователя:</label>
          <input
            type="text"
            value={credentials.username}
            onChange={(e) =>
              setCredentials({ ...credentials, username: e.target.value })
            }
            required
          />
        </div>
        <div>
          <label>Email:</label>
          <input
            type="email"
            value={credentials.email}
            onChange={(e) =>
              setCredentials({ ...credentials, email: e.target.value })
            }
            required
          />
        </div>
        <div>
          <label>Пароль:</label>
          <input
            type="password"
            value={credentials.password}
            onChange={(e) =>
              setCredentials({ ...credentials, password: e.target.value })
            }
            required
          />
        </div>
        <button type="submit">Зарегистрироваться</button>
      </form>
    </div>
  );
}
