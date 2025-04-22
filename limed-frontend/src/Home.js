import React from "react";
import { useAuth } from "./AuthContext";

export default function Home() {
  const { user, logoutUser } = useAuth();

  const handleLogout = async () => {
    try {
      await logoutUser();
    } catch (error) {
      console.error("Ошибка при выходе:", error);
    }
  };

  return (
    <div>
      {user ? (
        <>
          <h2>Добро пожаловать!</h2>
          <p>Вы авторизованы.</p>
          <button onClick={handleLogout}>Выйти</button>
        </>
      ) : (
        <p>Пожалуйста, войдите в систему.</p>
      )}
    </div>
  );
}
