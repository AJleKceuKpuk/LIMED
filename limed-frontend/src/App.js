import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import NavBar from './components/NavBar';
import Login from './components/Login';
import Registration from './components/Registration';
import Game from './components/Game';
import UserProfile from './components/UserProfile';
import AdminPage from './components/AdminPage';
import UserDetail from './components/UserDetail';

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <NavBar />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/registration" element={<Registration />} />
          <Route path="/game" element={<Game />} />
          <Route path="/user/profile" element={<UserProfile />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/admin/get-user/:id" element={<UserDetail />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
