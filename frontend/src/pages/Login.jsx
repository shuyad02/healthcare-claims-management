import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NotificationToast } from '../components/NotificationToast';

export const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [toast, setToast] = useState(null);
  const { login, loading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const handleQuickSelect = (u, p) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div className="brand-icon" style={{ margin: '0 auto 1rem', width: '48px', height: '48px', fontSize: '1.5rem' }}>🏥</div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>HealthClaim Pro</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Healthcare Benefits & Claims Portal</p>
        </div>

        <div>
          <label style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)' }}>Quick Demo Role Fill:</label>
          <div className="quick-roles">
            <button className="quick-role-btn" type="button" onClick={() => handleQuickSelect('admin', 'AdminPass123!')}>🔑 Admin</button>
            <button className="quick-role-btn" type="button" onClick={() => handleQuickSelect('hrofficer', 'OfficerPass123!')}>👔 HR Officer</button>
            <button className="quick-role-btn" type="button" onClick={() => handleQuickSelect('employee1', 'EmpPass123!')}>👨‍💻 Employee</button>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Username or Email</label>
            <input
              type="text"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. admin or john.doe@healthcare.com"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In to Portal'}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Don't have an employee account? <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 600 }}>Register Now</Link>
        </div>
      </div>
      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
