import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Employees } from './pages/Employees';
import { Plans } from './pages/Plans';
import { Claims } from './pages/Claims';
import { ClaimCreate } from './pages/ClaimCreate';
import { ClaimDetail } from './pages/ClaimDetail';
import { Profile } from './pages/Profile';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Application Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/employees" element={<Employees />} />
            <Route path="/plans" element={<Plans />} />
            <Route path="/claims" element={<Claims />} />
            <Route path="/claims/new" element={<ClaimCreate />} />
            <Route path="/claims/:id" element={<ClaimDetail />} />
            <Route path="/profile" element={<Profile />} />
          </Route>

          {/* Default Fallback Redirect */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
