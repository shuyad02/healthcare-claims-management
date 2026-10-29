const API_BASE_URL = 'http://localhost:8080/api';

const getHeaders = () => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

export const api = {
  // Auth
  login: async (credentials) => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Login failed');
    }
    return res.json();
  },

  register: async (userData) => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(userData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Registration failed');
    }
    return res.json();
  },

  // Stats
  getDashboardStats: async () => {
    const res = await fetch(`${API_BASE_URL}/stats/dashboard`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch dashboard statistics');
    return res.json();
  },

  // Employees
  getEmployees: async () => {
    const res = await fetch(`${API_BASE_URL}/employees`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch employees');
    return res.json();
  },

  getEmployeeById: async (id) => {
    const res = await fetch(`${API_BASE_URL}/employees/${id}`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch employee details');
    return res.json();
  },

  createEmployee: async (employeeData) => {
    const res = await fetch(`${API_BASE_URL}/employees`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(employeeData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to create employee');
    }
    return res.json();
  },

  updateEmployee: async (id, employeeData) => {
    const res = await fetch(`${API_BASE_URL}/employees/${id}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(employeeData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to update employee');
    }
    return res.json();
  },

  deleteEmployee: async (id) => {
    const res = await fetch(`${API_BASE_URL}/employees/${id}`, {
      method: 'DELETE',
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to delete employee');
    return true;
  },

  // Healthcare Plans
  getPlans: async () => {
    const res = await fetch(`${API_BASE_URL}/plans`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch healthcare plans');
    return res.json();
  },

  getActivePlans: async () => {
    const res = await fetch(`${API_BASE_URL}/plans/active`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch active plans');
    return res.json();
  },

  createPlan: async (planData) => {
    const res = await fetch(`${API_BASE_URL}/plans`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(planData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to create plan');
    }
    return res.json();
  },

  updatePlan: async (id, planData) => {
    const res = await fetch(`${API_BASE_URL}/plans/${id}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(planData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to update plan');
    }
    return res.json();
  },

  deletePlan: async (id) => {
    const res = await fetch(`${API_BASE_URL}/plans/${id}`, {
      method: 'DELETE',
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to delete plan');
    return true;
  },

  enrollEmployeeInPlan: async (planId, employeeId) => {
    const res = await fetch(`${API_BASE_URL}/plans/${planId}/enroll/${employeeId}`, {
      method: 'POST',
      headers: getHeaders()
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to enroll employee in plan');
    }
    return true;
  },

  // Claims
  getClaims: async (status, employeeId) => {
    let url = `${API_BASE_URL}/claims`;
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (employeeId) params.append('employeeId', employeeId);
    if (params.toString()) url += `?${params.toString()}`;

    const res = await fetch(url, { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch claims');
    return res.json();
  },

  getClaimById: async (id) => {
    const res = await fetch(`${API_BASE_URL}/claims/${id}`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch claim details');
    return res.json();
  },

  getClaimsByEmployee: async (employeeId) => {
    const res = await fetch(`${API_BASE_URL}/claims/employee/${employeeId}`, {
      headers: getHeaders()
    });
    if (!res.ok) throw new Error('Failed to fetch employee claims');
    return res.json();
  },

  submitClaim: async (claimData) => {
    const res = await fetch(`${API_BASE_URL}/claims`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(claimData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to submit claim');
    }
    return res.json();
  },

  reviewClaim: async (id, remarksData) => {
    const res = await fetch(`${API_BASE_URL}/claims/${id}/review`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(remarksData || {})
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to review claim');
    }
    return res.json();
  },

  approveClaim: async (id, remarksData) => {
    const res = await fetch(`${API_BASE_URL}/claims/${id}/approve`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(remarksData || {})
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to approve claim');
    }
    return res.json();
  },

  rejectClaim: async (id, remarksData) => {
    const res = await fetch(`${API_BASE_URL}/claims/${id}/reject`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(remarksData || {})
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to reject claim');
    }
    return res.json();
  }
};
