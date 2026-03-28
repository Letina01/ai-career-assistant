import { useState, useEffect } from 'react';
import { Check, AlertCircle, Info, X } from 'lucide-react';

export function useToast() {
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'info', duration = 4000) => {
    setToast({ message, type });
    setTimeout(() => setToast(null), duration);
  };

  return { toast, showToast };
}

export function Toast({ message, type = 'info', onClose }) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false);
      onClose?.();
    }, 4000);

    return () => clearTimeout(timer);
  }, [onClose]);

  if (!isVisible) return null;

  const icons = {
    success: <Check size={20} />,
    error: <AlertCircle size={20} />,
    info: <Info size={20} />,
    warning: <AlertCircle size={20} />
  };

  return (
    <div className={`toast toast-${type}`}>
      <div className="toast-content">
        {icons[type]}
        <span>{message}</span>
      </div>
      <button
        className="toast-close"
        onClick={() => {
          setIsVisible(false);
          onClose?.();
        }}
      >
        <X size={18} />
      </button>
    </div>
  );
}

export function ToastContainer({ toasts }) {
  return (
    <div className="toast-container">
      {toasts.map((toast, idx) => (
        <Toast key={idx} {...toast} />
      ))}
    </div>
  );
}
