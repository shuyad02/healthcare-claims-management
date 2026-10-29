import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LayoutDashboard, Users, ShieldPlus, FileText, PlusCircle, User, LogOut } from 'lucide-react';

export const Sidebar = () => {
  const { user, logout } = useAuth();
  const role = user?.role || 'ROLE_EMPLOYEE';

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="brand-icon">🏥</div>
        <div>
          <div className="brand-title">HealthClaim</div>
          <div style={{ fontSize: '0.7rem', color: '#94a3b8', fontWeight: 600 }}>PRO ENTERPRISE</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <LayoutDashboard size={18} />
          <span>Dashboard</span>
        </NavLink>

        {(role === 'ROLE_ADMIN' || role === 'ROLE_HR_OFFICER') && (
          <NavLink to="/employees" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Users size={18} />
            <span>Employees</span>
          </NavLink>
        )}

        <NavLink to="/plans" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <ShieldPlus size={18} />
          <span>Healthcare Plans</span>
        </NavLink>

        <NavLink to="/claims" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <FileText size={18} />
          <span>Claims Directory</span>
        </NavLink>

        <NavLink to="/claims/new" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <PlusCircle size={18} />
          <span>Submit Claim</span>
        </NavLink>

        <NavLink to="/profile" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <User size={18} />
          <span>My Profile</span>
        </NavLink>

        <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid var(--border-dark)' }}>
          <button onClick={logout} className="nav-item" style={{ width: '100%', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left' }}>
            <LogOut size={18} />
            <span>Sign Out</span>
          </button>
        </div>
      </nav>
    </aside>
  );
};
