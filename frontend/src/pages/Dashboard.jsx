import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { StatCard } from '../components/StatCard';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { Users, ShieldCheck, Clock, CheckCircle2, XCircle, DollarSign, FileText, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentClaims, setRecentClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [statsData, claimsData] = await Promise.all([
        api.getDashboardStats(),
        api.getClaims()
      ]);
      setStats(statsData);
      setRecentClaims(claimsData.slice(0, 5));
    } catch (err) {
      console.error('Error loading dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <div className="brand-icon" style={{ margin: '0 auto 1rem', animation: 'spin 1s linear infinite' }}>⏳</div>
        <div>Loading Executive Dashboard Analytics...</div>
      </div>
    );
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Executive Dashboard</h1>
          <p className="page-subtitle">Healthcare Benefits & Claims Operational Overview</p>
        </div>
        <Link to="/claims/new" className="btn btn-primary">
          <FileText size={18} />
          Submit New Claim
        </Link>
      </div>

      {/* Stats Cards Grid */}
      <div className="stats-grid">
        <StatCard
          icon={<Users />}
          label="Total Employees"
          value={stats?.totalEmployees || 0}
          color="blue"
        />
        <StatCard
          icon={<ShieldCheck />}
          label="Active Healthcare Plans"
          value={stats?.activePlans || 0}
          color="teal"
        />
        <StatCard
          icon={<Clock />}
          label="Pending Claims"
          value={stats?.pendingClaims || 0}
          color="amber"
        />
        <StatCard
          icon={<CheckCircle2 />}
          label="Approved Claims"
          value={stats?.approvedClaims || 0}
          color="green"
        />
        <StatCard
          icon={<XCircle />}
          label="Rejected Claims"
          value={stats?.rejectedClaims || 0}
          color="red"
        />
        <StatCard
          icon={<DollarSign />}
          label="Total Claim Amount"
          value={`$${(stats?.totalClaimAmount || 0).toLocaleString()}`}
          color="blue"
        />
      </div>

      {/* Recent Claims Overview */}
      <div className="card-table-container">
        <div className="table-toolbar">
          <div>
            <h3 style={{ fontWeight: 700 }}>Recent Claim Filings</h3>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Latest healthcare reimbursement requests requiring review or updates</p>
          </div>
          <Link to="/claims" className="btn btn-secondary btn-sm">
            View All Claims <ArrowRight size={14} />
          </Link>
        </div>

        <table className="custom-table">
          <thead>
            <tr>
              <th>Claim #</th>
              <th>Employee</th>
              <th>Healthcare Plan</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Claim Date</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {recentClaims.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
                  No claims submitted yet.
                </td>
              </tr>
            ) : (
              recentClaims.map((claim) => (
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
                  <td>
                    <Link to={`/claims/${claim.id}`} className="btn btn-secondary btn-sm">
                      Details
                    </Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
