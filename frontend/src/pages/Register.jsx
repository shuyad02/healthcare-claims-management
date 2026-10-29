import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NotificationToast } from '../components/NotificationToast';

export const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    name: '',
    phone: '',
    department: 'Engineering',
    dateOfJoining: new Date().toISOString().split('T')[0]
  });

  const [toast, setToast] = useState(null);
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.phone.match(/^[0-9]{10}$/)) {
      setToast({ type: 'error', message: 'Phone number must contain exactly 10 digits' });
      return;
    }

    try {
      await register(formData);
      navigate('/dashboard');
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: '520px' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Employee Registration</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Register for Healthcare Benefits Account</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input type="text" name="name" className="form-input" value={formData.name} onChange={handleChange} placeholder="John Doe" required />
            </div>
            <div className="form-group">
              <label className="form-label">Username</label>
              <input type="text" name="username" className="form-input" value={formData.username} onChange={handleChange} placeholder="johndoe" required />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Work Email</label>
            <input type="email" name="email" className="form-input" value={formData.email} onChange={handleChange} placeholder="john.doe@healthcare.com" required />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input type="password" name="password" className="form-input" value={formData.password} onChange={handleChange} placeholder="••••••••" required />
            </div>
            <div className="form-group">
              <label className="form-label">Phone (10 digits)</label>
              <input type="text" name="phone" className="form-input" value={formData.phone} onChange={handleChange} placeholder="9876543210" required />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Department</label>
              <select name="department" className="form-select" value={formData.department} onChange={handleChange}>
                <option value="Engineering">Engineering</option>
                <option value="Quality Assurance">Quality Assurance</option>
                <option value="Human Resources">Human Resources</option>
                <option value="Finance">Finance</option>
                <option value="Operations">Operations</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Date of Joining</label>
              <input type="date" name="dateOfJoining" className="form-input" value={formData.dateOfJoining} onChange={handleChange} required />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={loading}>
            {loading ? 'Creating Account...' : 'Complete Registration'}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Already registered? <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600 }}>Sign In</Link>
        </div>
      </div>
      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
