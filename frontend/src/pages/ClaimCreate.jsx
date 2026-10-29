import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { NotificationToast } from '../components/NotificationToast';
import { FilePlus, Upload, ShieldCheck, DollarSign } from 'lucide-react';

export const ClaimCreate = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [plans, setPlans] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  const [formData, setFormData] = useState({
    employeeId: user?.employeeId || '',
    planId: '',
    claimAmount: '',
    claimDate: new Date().toISOString().split('T')[0],
    claimType: 'OPD',
    description: '',
    supportingDocumentUrl: '/docs/sample_receipt.pdf'
  });

  const [selectedPlan, setSelectedPlan] = useState(null);

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      const activePlans = await api.getActivePlans();
      setPlans(activePlans);
      if (activePlans.length > 0) {
        setFormData(prev => ({ ...prev, planId: activePlans[0].id }));
        setSelectedPlan(activePlans[0]);
      }

      if (user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_HR_OFFICER') {
        const empList = await api.getEmployees();
        setEmployees(empList);
        if (empList.length > 0 && !formData.employeeId) {
          setFormData(prev => ({ ...prev, employeeId: empList[0].id }));
        }
      }
    } catch (err) {
      setToast({ type: 'error', message: 'Failed to load plans or employees' });
    }
  };

  const handlePlanChange = (planId) => {
    const id = Number(planId);
    setFormData({ ...formData, planId: id });
    const p = plans.find(item => item.id === id);
    setSelectedPlan(p);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.employeeId) {
      setToast({ type: 'error', message: 'Please select an employee profile' });
      return;
    }

    if (!formData.claimAmount || Number(formData.claimAmount) <= 0) {
      setToast({ type: 'error', message: 'Claim amount must be greater than zero' });
      return;
    }

    if (selectedPlan && Number(formData.claimAmount) > Number(selectedPlan.coverageAmount)) {
      setToast({ type: 'error', message: `Claim amount ($${formData.claimAmount}) exceeds plan maximum coverage ($${selectedPlan.coverageAmount})` });
      return;
    }

    try {
      setLoading(true);
      const payload = {
        employeeId: Number(formData.employeeId),
        planId: Number(formData.planId),
        claimAmount: Number(formData.claimAmount),
        claimDate: formData.claimDate,
        claimType: formData.claimType,
        description: formData.description,
        supportingDocumentUrl: formData.supportingDocumentUrl
      };

      const result = await api.submitClaim(payload);
      setToast({ type: 'success', message: `Claim ${result.claimNumber} submitted successfully!` });
      setTimeout(() => navigate('/claims'), 1200);
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Submit Healthcare Claim</h1>
          <p className="page-subtitle">File medical reimbursement request with supporting receipt documents</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '1.5rem' }}>
        <div className="form-card" style={{ maxWidth: '100%' }}>
          <form onSubmit={handleSubmit}>
            {(user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_HR_OFFICER') && (
              <div className="form-group">
                <label className="form-label">Select Employee</label>
                <select
                  className="form-select"
                  value={formData.employeeId}
                  onChange={(e) => setFormData({ ...formData, employeeId: e.target.value })}
                  required
                >
                  {employees.map(emp => (
                    <option key={emp.id} value={emp.id}>
                      {emp.name} ({emp.employeeCode}) - {emp.department}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Healthcare Plan</label>
              <select
                className="form-select"
                value={formData.planId}
                onChange={(e) => handlePlanChange(e.target.value)}
                required
              >
                {plans.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.planName} ({p.planCode}) - Max Coverage: ${p.coverageAmount?.toLocaleString()}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">Claim Type</label>
                <select
                  className="form-select"
                  value={formData.claimType}
                  onChange={(e) => setFormData({ ...formData, claimType: e.target.value })}
                >
                  <option value="OPD">OPD Consultation & Diagnostics</option>
                  <option value="HOSPITALIZATION">In-patient Hospitalization</option>
                  <option value="DENTAL">Dental Surgery & Care</option>
                  <option value="VISION">Vision Care & Optical</option>
                  <option value="PHARMACY">Prescription Pharmacy</option>
                  <option value="EMERGENCY">Emergency Room Services</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Claim Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  className="form-input"
                  placeholder="e.g. 1250.00"
                  value={formData.claimAmount}
                  onChange={(e) => setFormData({ ...formData, claimAmount: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Date of Service / Treatment</label>
              <input
                type="date"
                className="form-input"
                value={formData.claimDate}
                onChange={(e) => setFormData({ ...formData, claimDate: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Detailed Description / Medical Reason</label>
              <textarea
                className="form-textarea"
                rows="4"
                placeholder="Provide medical diagnosis context, hospital/clinic name, attending physician details..."
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
              ></textarea>
            </div>

            <div className="form-group">
              <label className="form-label">Attach Supporting Receipt / Invoice PDF</label>
              <div style={{ border: '2px dashed var(--border-color)', padding: '1.5rem', borderRadius: 'var(--radius-md)', textAlign: 'center', background: '#f8fafc' }}>
                <Upload size={24} color="var(--primary)" style={{ margin: '0 auto 0.5rem' }} />
                <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>Attached Receipt File: sample_hospital_invoice.pdf</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Verified 100% digital proof of expense</div>
              </div>
            </div>

            <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={loading}>
              <FilePlus size={18} /> {loading ? 'Filing Claim...' : 'Submit Claim Request'}
            </button>
          </form>
        </div>

        {/* Dynamic Plan Summary Widget */}
        <div>
          <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--primary)' }}>
              <ShieldCheck size={22} />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Plan Policy Verification</h3>
            </div>
            {selectedPlan ? (
              <div>
                <div style={{ fontWeight: 700, fontSize: '1rem' }}>{selectedPlan.planName}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{selectedPlan.planCode}</div>

                <hr style={{ margin: '0.8rem 0', borderColor: 'var(--border-color)' }} />

                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                  <span>Coverage Ceiling:</span>
                  <strong>${selectedPlan.coverageAmount?.toLocaleString()}</strong>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                  <span>Monthly Premium:</span>
                  <strong>${selectedPlan.premium?.toLocaleString()}</strong>
                </div>

                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.8rem' }}>
                  💡 <em>Duplicate claim validation will prevent submitting active duplicate requests for identical amounts.</em>
                </div>
              </div>
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Select a plan to view policy limits.</div>
            )}
          </div>
        </div>
      </div>

      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
