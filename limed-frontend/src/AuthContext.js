import React, { createContext, useContext, useState, useEffect } from "react";
import api from "./api";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const loginUser = async (credentials) => {
    const response = await api.post("/login", credentials); // Используем api для запроса
    const accessToken = response.data.accessToken;

    localStorage.setItem("accessToken", accessToken); // Сохраняем токен в LocalStorage
    setUser(response.data); // Устанавливаем данные пользователя
    return response.data;
  };

  const logoutUser = async () => {
    if (user?.accessToken) {
      try {
        await api.post("/logout", {}, {
          headers: {
            Authorization: `Bearer ${user.accessToken}`, // Передаём токен
          },
        });
        setUser(null);
        localStorage.removeItem("accessToken");
      } catch (error) {
        console.error("Ошибка при выходе:", error);
      }
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem("accessToken");
      if (storedToken) {
        setUser({ accessToken: storedToken }); // Восстанавливаем пользователя
      }
    };

    initializeAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ user, loginUser, logoutUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
