import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Search, UserCircle } from 'lucide-react';

export const Navbar = ({ onSearch }) => {
  const { user } = useAuth();

  const getRoleFormatted = (role) => {
    switch (role) {
      case 'ROLE_ADMIN': return 'ADMIN';
      case 'ROLE_HR_OFFICER': return 'HR OFFICER';
      default: return 'EMPLOYEE';
    }
  };

  const getRoleClass = (role) => {
    switch (role) {
      case 'ROLE_ADMIN': return 'admin';
      case 'ROLE_HR_OFFICER': return 'hr_officer';
      default: return 'employee';
    }
  };

  return (
    <header className="topbar">
      <div className="search-box">
        <Search size={16} color="var(--text-muted)" />
        <input
          type="text"
          placeholder="Search claims, plans, or employees..."
          onChange={(e) => onSearch && onSearch(e.target.value)}
        />
      </div>

      <div className="user-profile-widget">
        <span className={`role-badge ${getRoleClass(user?.role)}`}>
          {getRoleFormatted(user?.role)}
        </span>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontWeight: 700, fontSize: '0.9rem' }}>{user?.name || user?.username || 'User'}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</div>
        </div>
        <UserCircle size={36} color="var(--primary)" />
      </div>
    </header>
  );
};
