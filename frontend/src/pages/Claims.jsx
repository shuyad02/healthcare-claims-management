import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { NotificationToast } from '../components/NotificationToast';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { Plus, Search, Filter, Eye, CheckCircle, XCircle, SearchCheck } from 'lucide-react';

export const Claims = () => {
  const { user } = useAuth();
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [search, setSearch] = useState('');
  const [toast, setToast] = useState(null);

  // Review / Approve / Reject Modal State
  const [activeClaim, setActiveClaim] = useState(null);
  const [actionType, setActionType] = useState(null); // 'REVIEW' | 'APPROVE' | 'REJECT'
  const [officerRemarks, setOfficerRemarks] = useState('');

  useEffect(() => {
    fetchClaims();
  }, [statusFilter]);

  const fetchClaims = async () => {
    try {
      setLoading(true);
      let data;
      if (user?.role === 'ROLE_EMPLOYEE' && user?.employeeId) {
        data = await api.getClaimsByEmployee(user.employeeId);
      } else {
        data = await api.getClaims(statusFilter || null);
      }
      setClaims(data);
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenActionModal = (claim, action) => {
    setActiveClaim(claim);
    setActionType(action);
    setOfficerRemarks('');
  };

  const handleExecuteAction = async () => {
    if (actionType === 'REJECT' && !officerRemarks.trim()) {
      setToast({ type: 'error', message: 'Rejection reason is required in officer remarks!' });
      return;
    }

    try {
      if (actionType === 'REVIEW') {
        await api.reviewClaim(activeClaim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim status updated to UNDER_REVIEW' });
      } else if (actionType === 'APPROVE') {
        await api.approveClaim(activeClaim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim APPROVED successfully' });
      } else if (actionType === 'REJECT') {
        await api.rejectClaim(activeClaim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim REJECTED with officer remarks' });
      }

      setActiveClaim(null);
      setActionType(null);
      fetchClaims();
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const filteredClaims = claims.filter(c =>
    c.claimNumber.toLowerCase().includes(search.toLowerCase()) ||
    c.employeeName?.toLowerCase().includes(search.toLowerCase()) ||
    c.claimType.toLowerCase().includes(search.toLowerCase()) ||
    c.planName?.toLowerCase().includes(search.toLowerCase())
  );

  const canReviewOrApprove = user?.role === 'ROLE_HR_OFFICER' || user?.role === 'ROLE_ADMIN';

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Healthcare Claims Directory</h1>
          <p className="page-subtitle">Track, audit, review and process employee benefit claims</p>
        </div>
        <Link to="/claims/new" className="btn btn-primary">
          <Plus size={18} /> Submit Claim
        </Link>
      </div>

      <div className="card-table-container">
        <div className="table-toolbar" style={{ flexWrap: 'wrap' }}>
          <div className="search-box" style={{ width: '280px' }}>
            <Search size={16} color="var(--text-muted)" />
            <input
              type="text"
              placeholder="Filter by claim #, employee, plan..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Filter size={16} color="var(--text-muted)" />
            <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Status Filter:</span>
            <select
              className="form-select"
              style={{ width: '160px', padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="PENDING">PENDING</option>
              <option value="UNDER_REVIEW">UNDER_REVIEW</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>
          </div>
        </div>

        <table className="custom-table">
          <thead>
            <tr>
              <th>Claim #</th>
              <th>Employee Details</th>
              <th>Healthcare Plan</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Date</th>
              <th>Status</th>
              <th>Officer Remarks</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="9" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  Loading claims database...
                </td>
              </tr>
            ) : filteredClaims.length === 0 ? (
              <tr>
                <td colSpan="9" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  No claims found matching the criteria.
                </td>
              </tr>
            ) : (
              filteredClaims.map((claim) => (
                <tr key={claim.id}>
                  <td style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>{claim.claimNumber}</td>
                  <td>
                    <div style={{ fontWeight: 600 }}>{claim.employeeName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{claim.employeeCode} • {claim.employeeDepartment}</div>
                  </td>
                  <td>{claim.planName}</td>
                  <td><span style={{ fontSize: '0.8rem', background: '#f1f5f9', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>{claim.claimType}</span></td>
                  <td style={{ fontWeight: 700 }}>${claim.claimAmount?.toLocaleString()}</td>
                  <td>{claim.claimDate}</td>
                  <td><StatusBadge status={claim.status} /></td>
                  <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)', maxWidth: '180px' }}>
                    {claim.officerRemarks || <em style={{ color: '#cbd5e1' }}>No remarks</em>}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.3rem' }}>
                      <Link to={`/claims/${claim.id}`} className="btn btn-secondary btn-sm" title="View Full Details">
                        <Eye size={14} />
                      </Link>

                      {canReviewOrApprove && claim.status === 'PENDING' && (
                        <button className="btn btn-warning btn-sm" onClick={() => handleOpenActionModal(claim, 'REVIEW')} title="Mark Under Review">
                          <SearchCheck size={14} /> Review
                        </button>
                      )}

                      {canReviewOrApprove && (claim.status === 'UNDER_REVIEW' || claim.status === 'PENDING') && (
                        <button className="btn btn-success btn-sm" onClick={() => handleOpenActionModal(claim, 'APPROVE')} title="Approve Claim">
                          <CheckCircle size={14} /> Approve
                        </button>
                      )}

                      {canReviewOrApprove && (claim.status !== 'APPROVED' && claim.status !== 'REJECTED') && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleOpenActionModal(claim, 'REJECT')} title="Reject Claim">
                          <XCircle size={14} /> Reject
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Workflow Review / Action Modal */}
      <Modal
        isOpen={!!actionType}
        onClose={() => setActionType(null)}
        title={`${actionType === 'REVIEW' ? 'Move Claim to Under Review' : actionType === 'APPROVE' ? 'Approve Claim Filing' : 'Reject Claim Filing'} - ${activeClaim?.claimNumber}`}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setActionType(null)}>Cancel</button>
            <button
              className={`btn ${actionType === 'APPROVE' ? 'btn-success' : actionType === 'REJECT' ? 'btn-danger' : 'btn-warning'}`}
              onClick={handleExecuteAction}
            >
              Confirm {actionType}
            </button>
          </>
        }
      >
        <div>
          <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
              <span><strong>Employee:</strong> {activeClaim?.employeeName}</span>
              <span><strong>Claim Amount:</strong> ${activeClaim?.claimAmount?.toLocaleString()}</span>
            </div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.3rem' }}>
              <strong>Type:</strong> {activeClaim?.claimType} | <strong>Current Status:</strong> {activeClaim?.status}
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              Officer Remarks {actionType === 'REJECT' && <span style={{ color: 'var(--danger)' }}>* (Required for Rejection)</span>}
            </label>
            <textarea
              className="form-textarea"
              rows="3"
              placeholder={actionType === 'REJECT' ? "Specify exact reason for rejection (e.g. Ineligible medical expense item per Policy 4.2)..." : "Add internal review findings or approval notes..."}
              value={officerRemarks}
              onChange={(e) => setOfficerRemarks(e.target.value)}
              required={actionType === 'REJECT'}
            ></textarea>
          </div>
        </div>
      </Modal>

      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
