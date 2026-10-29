import React from 'react';

export const StatusBadge = ({ status }) => {
  const getLabel = (s) => {
    switch (s) {
      case 'PENDING': return 'Pending';
      case 'UNDER_REVIEW': return 'Under Review';
      case 'APPROVED': return 'Approved';
      case 'REJECTED': return 'Rejected';
      default: return s || 'Unknown';
    }
  };

  return (
    <span className={`status-pill ${status}`}>
      ● {getLabel(status)}
    </span>
  );
};
