import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Modal } from '../components/Modal';
import { NotificationToast } from '../components/NotificationToast';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Plus, CheckCircle, Award, DollarSign } from 'lucide-react';

export const Plans = () => {
  const { user } = useAuth();
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [toast, setToast] = useState(null);

  const [formData, setFormData] = useState({
    planCode: '',
    planName: '',
    description: '',
    coverageAmount: '',
    premium: '',
    eligibility: 'All permanent employees',
    active: true
  });

  useEffect(() => {
    fetchPlans();
  }, []);

  const fetchPlans = async () => {
    try {
      setLoading(true);
      const data = await api.getPlans();
      setPlans(data);
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleEnroll = async (planId) => {
    if (!user?.employeeId) {
      setToast({ type: 'error', message: 'Employee profile not linked to current user account' });
      return;
    }
    try {
      await api.enrollEmployeeInPlan(planId, user.employeeId);
      setToast({ type: 'success', message: 'Successfully enrolled in healthcare plan!' });
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const handleSubmitPlan = async (e) => {
    e.preventDefault();
    try {
      await api.createPlan(formData);
      setToast({ type: 'success', message: 'Healthcare plan created successfully' });
      setIsModalOpen(false);
      fetchPlans();
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Healthcare Benefits Plans</h1>
          <p className="page-subtitle">Medical insurance schemes, OPD coverage & premium options</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
            <Plus size={18} /> Create New Plan
          </button>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {loading ? (
          <div style={{ colSpan: 'all', padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading available healthcare packages...
          </div>
        ) : (
          plans.map((plan) => (
            <div key={plan.id} className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1.25rem', position: 'relative' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--primary)', fontFamily: 'var(--font-mono)' }}>
                    {plan.planCode}
                  </span>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 800, marginTop: '0.2rem' }}>{plan.planName}</h3>
                </div>
                <span className={`status-pill ${plan.active ? 'APPROVED' : 'REJECTED'}`}>
                  {plan.active ? 'Active Plan' : 'Inactive'}
                </span>
              </div>

              <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', minHeight: '40px' }}>
                {plan.description}
              </p>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Coverage Limit</div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--success)' }}>
                    ${plan.coverageAmount?.toLocaleString()}
                  </div>
                </div>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Monthly Premium</div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--primary)' }}>
                    ${plan.premium?.toLocaleString()}
                  </div>
                </div>
              </div>

              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Award size={14} color="var(--primary)" />
                <strong>Eligibility:</strong> {plan.eligibility || 'All active employees'}
              </div>

              {user?.role === 'ROLE_EMPLOYEE' && (
                <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => handleEnroll(plan.id)}>
                  <CheckCircle size={16} /> Enroll in this Plan
                </button>
              )}
            </div>
          ))
        )}
      </div>

      {/* Admin Plan Creation Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create Healthcare Plan"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSubmitPlan}>Publish Healthcare Plan</button>
          </>
        }
      >
        <form onSubmit={handleSubmitPlan}>
          <div className="form-group">
            <label className="form-label">Plan Code</label>
            <input type="text" className="form-input" placeholder="PLAN-GOLD-01" value={formData.planCode} onChange={(e) => setFormData({ ...formData, planCode: e.target.value })} required />
          </div>

          <div className="form-group">
            <label className="form-label">Plan Name</label>
            <input type="text" className="form-input" placeholder="Comprehensive Executive Care" value={formData.planName} onChange={(e) => setFormData({ ...formData, planName: e.target.value })} required />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="form-textarea" rows="3" placeholder="Plan scope and benefit highlights..." value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })}></textarea>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Coverage Amount ($)</label>
              <input type="number" step="0.01" className="form-input" placeholder="50000" value={formData.coverageAmount} onChange={(e) => setFormData({ ...formData, coverageAmount: e.target.value })} required />
            </div>

            <div className="form-group">
              <label className="form-label">Monthly Premium ($)</label>
              <input type="number" step="0.01" className="form-input" placeholder="250" value={formData.premium} onChange={(e) => setFormData({ ...formData, premium: e.target.value })} required />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Eligibility Criteria</label>
            <input type="text" className="form-input" placeholder="Full-time staff after probation" value={formData.eligibility} onChange={(e) => setFormData({ ...formData, eligibility: e.target.value })} />
          </div>
        </form>
      </Modal>

      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
