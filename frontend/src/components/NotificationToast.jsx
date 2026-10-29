import React from 'react';

export const NotificationToast = ({ toast, onClose }) => {
  if (!toast) return null;

  return (
    <div className="toast-container">
      <div className={`toast ${toast.type || 'info'}`}>
        <span>{toast.type === 'error' ? '❌' : toast.type === 'success' ? '✅' : 'ℹ️'}</span>
        <span>{toast.message}</span>
        <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer', marginLeft: 'auto' }}>✕</button>
      </div>
    </div>
  );
};
