import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { StatusBadge } from '../components/StatusBadge';
import { User, Mail, Phone, Building, Calendar, Shield, FileText } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Profile = () => {
  const { user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [myClaims, setMyClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProfileData();
  }, []);

  const fetchProfileData = async () => {
    try {
      setLoading(true);
      if (user?.employeeId) {
        const [empData, claimsData] = await Promise.all([
          api.getEmployeeById(user.employeeId),
          api.getClaimsByEmployee(user.employeeId)
        ]);
        setEmployee(empData);
        setMyClaims(claimsData);
      }
    } catch (err) {
      console.error('Error loading profile:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">User Profile</h1>
          <p className="page-subtitle">Personal account, role privileges & benefit claims history</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '340px 1fr', gap: '1.5rem' }}>
        {/* Profile Card */}
        <div className="stat-card" style={{ flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: '2rem' }}>
          <div className="brand-icon" style={{ width: '72px', height: '72px', fontSize: '2.2rem', borderRadius: '50%', marginBottom: '1rem' }}>
            👤
          </div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>{user?.name || user?.username}</h2>
          <span className={`role-badge ${user?.role === 'ROLE_ADMIN' ? 'admin' : user?.role === 'ROLE_HR_OFFICER' ? 'hr_officer' : 'employee'}`} style={{ marginTop: '0.4rem' }}>
            {user?.role?.replace('ROLE_', '')}
          </span>

          <hr style={{ width: '100%', margin: '1.5rem 0', borderColor: 'var(--border-color)' }} />

          <div style={{ width: '100%', textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '0.8rem', fontSize: '0.9rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <Mail size={16} color="var(--primary)" /> <span>{user?.email}</span>
            </div>
            {employee && (
              <>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  <Phone size={16} color="var(--primary)" /> <span>{employee.phone}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  <Building size={16} color="var(--primary)" /> <span>Department: <strong>{employee.department}</strong></span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  <Calendar size={16} color="var(--primary)" /> <span>Joined: {employee.dateOfJoining}</span>
                </div>
              </>
            )}
          </div>
        </div>

        {/* Claims History */}
        <div className="card-table-container">
          <div className="table-toolbar">
            <h3 style={{ fontWeight: 700 }}>Personal Claims History ({myClaims.length})</h3>
            <Link to="/claims/new" className="btn btn-primary btn-sm">
              <FileText size={14} /> Submit Claim
            </Link>
          </div>

          <table className="custom-table">
            <thead>
              <tr>
                <th>Claim #</th>
                <th>Plan Name</th>
                <th>Category</th>
                <th>Amount</th>
                <th>Date</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '2rem' }}>Loading claims history...</td>
                </tr>
              ) : myClaims.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                    No personal claims filed yet.
                  </td>
                </tr>
              ) : (
                myClaims.map((claim) => (
                  <tr key={claim.id}>
                    <td style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>{claim.claimNumber}</td>
                    <td>{claim.planName}</td>
                    <td>{claim.claimType}</td>
                    <td style={{ fontWeight: 700 }}>${claim.claimAmount?.toLocaleString()}</td>
                    <td>{claim.claimDate}</td>
                    <td><StatusBadge status={claim.status} /></td>
                    <td>
                      <Link to={`/claims/${claim.id}`} className="btn btn-secondary btn-sm">View</Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
