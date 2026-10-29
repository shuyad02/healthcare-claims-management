import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Modal } from '../components/Modal';
import { NotificationToast } from '../components/NotificationToast';
import { useAuth } from '../context/AuthContext';
import { UserPlus, Search, Edit2, Trash2, Mail, Phone, Calendar } from 'lucide-react';

export const Employees = () => {
  const { user } = useAuth();
  const [employees, setEmployees] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [toast, setToast] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    department: 'Engineering',
    dateOfJoining: new Date().toISOString().split('T')[0],
    status: 'ACTIVE'
  });

  const [selectedId, setSelectedId] = useState(null);

  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const data = await api.getEmployees();
      setEmployees(data);
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAddModal = () => {
    setSelectedId(null);
    setFormData({
      name: '',
      email: '',
      phone: '',
      department: 'Engineering',
      dateOfJoining: new Date().toISOString().split('T')[0],
      status: 'ACTIVE'
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (emp) => {
    setSelectedId(emp.id);
    setFormData({
      name: emp.name,
      email: emp.email,
      phone: emp.phone,
      department: emp.department,
      dateOfJoining: emp.dateOfJoining,
      status: emp.status
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (selectedId) {
        await api.updateEmployee(selectedId, formData);
        setToast({ type: 'success', message: 'Employee profile updated successfully' });
      } else {
        await api.createEmployee(formData);
        setToast({ type: 'success', message: 'New employee registered successfully' });
      }
      setIsModalOpen(false);
      fetchEmployees();
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to remove this employee?')) return;
    try {
      await api.deleteEmployee(id);
      setToast({ type: 'success', message: 'Employee record removed' });
      fetchEmployees();
    } catch (err) {
      setToast({ type: 'error', message: err.message });
    }
  };

  const filteredEmployees = employees.filter(emp =>
    emp.name.toLowerCase().includes(search.toLowerCase()) ||
    emp.email.toLowerCase().includes(search.toLowerCase()) ||
    emp.employeeCode.toLowerCase().includes(search.toLowerCase()) ||
    emp.department.toLowerCase().includes(search.toLowerCase())
  );

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Employee Directory</h1>
          <p className="page-subtitle">Manage workforce organization & healthcare enrollment</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={handleOpenAddModal}>
            <UserPlus size={18} /> Add New Employee
          </button>
        )}
      </div>

      <div className="card-table-container">
        <div className="table-toolbar">
          <div className="search-box" style={{ width: '300px' }}>
            <Search size={16} color="var(--text-muted)" />
            <input
              type="text"
              placeholder="Search by name, code, email, dept..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Showing <strong>{filteredEmployees.length}</strong> employees
          </div>
        </div>

        <table className="custom-table">
          <thead>
            <tr>
              <th>Employee Code</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Department</th>
              <th>Joining Date</th>
              <th>Status</th>
              {isAdmin && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  Loading employee database...
                </td>
              </tr>
            ) : filteredEmployees.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  No matching employee records found.
                </td>
              </tr>
            ) : (
              filteredEmployees.map((emp) => (
                <tr key={emp.id}>
                  <td style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>{emp.employeeCode}</td>
                  <td style={{ fontWeight: 600 }}>{emp.name}</td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Mail size={14} color="var(--text-muted)" /> {emp.email}
                    </span>
                  </td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Phone size={14} color="var(--text-muted)" /> {emp.phone}
                    </span>
                  </td>
                  <td>
                    <span style={{ background: '#f1f5f9', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.85rem' }}>
                      {emp.department}
                    </span>
                  </td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <Calendar size={14} color="var(--text-muted)" /> {emp.dateOfJoining}
                    </span>
                  </td>
                  <td>
                    <span className={`status-pill ${emp.status === 'ACTIVE' ? 'APPROVED' : 'REJECTED'}`}>
                      {emp.status}
                    </span>
                  </td>
                  {isAdmin && (
                    <td>
                      <div style={{ display: 'flex', gap: '0.4rem' }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEditModal(emp)}>
                          <Edit2 size={14} />
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(emp.id)}>
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Add / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={selectedId ? "Edit Employee Details" : "Register New Employee"}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSubmit}>Save Employee</button>
          </>
        }
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input type="text" className="form-input" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} required />
          </div>

          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input type="email" className="form-input" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} required />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number (10 digits)</label>
            <input type="text" className="form-input" value={formData.phone} onChange={(e) => setFormData({ ...formData, phone: e.target.value })} required />
          </div>

          <div className="form-group">
            <label className="form-label">Department</label>
            <select className="form-select" value={formData.department} onChange={(e) => setFormData({ ...formData, department: e.target.value })}>
              <option value="Engineering">Engineering</option>
              <option value="Quality Assurance">Quality Assurance</option>
              <option value="Human Resources">Human Resources</option>
              <option value="Finance">Finance</option>
              <option value="Operations">Operations</option>
              <option value="Management">Management</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Date of Joining</label>
            <input type="date" className="form-input" value={formData.dateOfJoining} onChange={(e) => setFormData({ ...formData, dateOfJoining: e.target.value })} required />
          </div>
        </form>
      </Modal>

      <NotificationToast toast={toast} onClose={() => setToast(null)} />
    </div>
  );
};
