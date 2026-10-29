import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { NotificationToast } from '../components/NotificationToast';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, CheckCircle, XCircle, SearchCheck, FileText, User, Shield, Calendar, DollarSign, MessageSquare } from 'lucide-react';

export const ClaimDetail = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionType, setActionType] = useState(null);
  const [officerRemarks, setOfficerRemarks] = useState('');
  const [toast, setToast] = useState(null);

  useEffect(() => {
    fetchClaimDetails();
  }, [id]);

  const fetchClaimDetails = async () => {
    try {
      setLoading(true);
      const data = await api.getClaimById(id);
      setClaim(data);
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleExecuteAction = async () => {
    if (actionType === 'REJECT' && !officerRemarks.trim()) {
      setToast({ type: 'error', message: 'Rejection reason is required in officer remarks!' });
      return;
    }

    try {
      if (actionType === 'REVIEW') {
        await api.reviewClaim(claim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim status updated to UNDER_REVIEW' });
      } else if (actionType === 'APPROVE') {
        await api.approveClaim(claim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim APPROVED successfully' });
      } else if (actionType === 'REJECT') {
        await api.rejectClaim(claim.id, { officerRemarks });
        setToast({ type: 'success', message: 'Claim REJECTED with officer remarks' });
      }

      setActionType(null);
      fetchClaimDetails();
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        Loading claim verification details...
      </div>
    );
  }

  if (!claim) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center' }}>
        <h2>Claim Not Found</h2>
        <Link to="/claims" className="btn btn-secondary" style={{ marginTop: '1rem' }}>
          Back to Claims Directory
        </Link>
      </div>
    );
  }

  const canReviewOrApprove = user?.role === 'ROLE_HR_OFFICER' || user?.role === 'ROLE_ADMIN';

  return (
    <div>
      <div style={{ marginBottom: '1rem' }}>
        <Link to="/claims" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)', fontWeight: 600, textDecoration: 'none' }}>
          <ArrowLeft size={16} /> Back to Claims Directory
        </Link>
      </div>

      <div className="page-header">
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <h1 className="page-title">Claim #{claim.claimNumber}</h1>
            <StatusBadge status={claim.status} />
          </div>
          <p className="page-subtitle">Submitted on {claim.createdAt ? new Date(claim.createdAt).toLocaleString() : claim.claimDate}</p>
        </div>

        {canReviewOrApprove && (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            {claim.status === 'PENDING' && (
              <button className="btn btn-warning" onClick={() => { setActionType('REVIEW'); setOfficerRemarks(''); }}>
                <SearchCheck size={16} /> Mark Under Review
              </button>
            )}

            {(claim.status === 'UNDER_REVIEW' || claim.status === 'PENDING') && (
              <button className="btn btn-success" onClick={() => { setActionType('APPROVE'); setOfficerRemarks(''); }}>
                <CheckCircle size={16} /> Approve Claim
              </button>
            )}

            {(claim.status !== 'APPROVED' && claim.status !== 'REJECTED') && (
              <button className="btn btn-danger" onClick={() => { setActionType('REJECT'); setOfficerRemarks(''); }}>
                <XCircle size={16} /> Reject Claim
              </button>
            )}
          </div>
        )}
      </div>

      {/* Claim Detail Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: '1.5rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Claim Overview & Financials</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Claim Amount</div>
                <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--primary)' }}>${claim.claimAmount?.toLocaleString()}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Claim Category</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{claim.claimType}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Service Date</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{claim.claimDate}</div>
              </div>
            </div>

            <div>
              <h4 style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '0.4rem' }}>Claim Description / Diagnosis Context</h4>
              <p style={{ fontSize: '0.9rem', color: 'var(--text-main)', background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
                {claim.description}
              </p>
            </div>

            <div>
              <h4 style={{ fontWeight: 700, fontSize: '0.95rem', marginBottom: '0.4rem' }}>Supporting Document Attachment</h4>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', border: '1px solid var(--border-color)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)' }}>
                <FileText size={24} color="var(--primary)" />
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>Official_Medical_Bill_Receipt.pdf</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Digital signature & hospital seal attached</div>
                </div>
                <a href={claim.supportingDocumentUrl} target="_blank" rel="noreferrer" className="btn btn-secondary btn-sm">
                  View Document
                </a>
              </div>
            </div>
          </div>

          {/* Workflow Remarks Log */}
          <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <MessageSquare size={20} color="var(--primary)" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>HR Claims Officer Remarks</h3>
            </div>
            {claim.officerRemarks ? (
              <div style={{ background: claim.status === 'REJECTED' ? 'var(--danger-light)' : claim.status === 'APPROVED' ? 'var(--success-light)' : '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-md)', borderLeft: `4px solid ${claim.status === 'REJECTED' ? 'var(--danger)' : 'var(--success)'}` }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', marginBottom: '0.2rem' }}>
                  {claim.status === 'REJECTED' ? 'Rejection Reason:' : 'Approval & Review Notes:'}
                </div>
                <div style={{ fontSize: '0.9rem' }}>{claim.officerRemarks}</div>
              </div>
            ) : (
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>No officer remarks logged yet. Pending review by HR Officer.</div>
            )}
          </div>
        </div>

        {/* Sidebar Context */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Employee Details</h3>
            <div style={{ fontSize: '0.9rem' }}>
              <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{claim.employeeName}</div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>Code: {claim.employeeCode}</div>
              <div style={{ marginTop: '0.5rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Department: <strong>{claim.employeeDepartment}</strong>
              </div>
            </div>
          </div>

          <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'stretch', gap: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Enrolled Healthcare Plan</h3>
            <div>
              <div style={{ fontWeight: 700, fontSize: '1rem' }}>{claim.planName}</div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontFamily: 'var(--font-mono)' }}>Code: {claim.planCode}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Action Modal */}
      <Modal
        isOpen={!!actionType}
        onClose={() => setActionType(null)}
        title={`${actionType === 'REVIEW' ? 'Move Claim to Under Review' : actionType === 'APPROVE' ? 'Approve Claim' : 'Reject Claim'}`}
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
        <div className="form-group">
          <label className="form-label">
            Officer Remarks {actionType === 'REJECT' && <span style={{ color: 'var(--danger)' }}>* (Required for Rejection)</span>}
          </label>
          <textarea
            className="form-textarea"
            rows="3"
            placeholder={actionType === 'REJECT' ? "State the specific reason for rejecting this claim..." : "Add review remarks..."}
            value={officerRemarks}
            onChange={(e) => setOfficerRemarks(e.target.value)}
            required={actionType === 'REJECT'}
          ></textarea>
        </div>
      </Modal>

      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
