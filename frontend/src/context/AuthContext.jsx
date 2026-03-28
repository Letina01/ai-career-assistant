import { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);

function readStoredUser() {
  const raw = localStorage.getItem('career-user');
  return raw ? JSON.parse(raw) : null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = (payload) => {
    localStorage.setItem('career-token', payload.token);
    localStorage.setItem('career-user', JSON.stringify(payload));
    setUser(payload);
  };

  const logout = () => {
    localStorage.removeItem('career-token');
    localStorage.removeItem('career-user');
    setUser(null);
  };

  const updateUser = (patch) => {
    setUser((current) => {
      if (!current) {
        return current;
      }
      const updated = { ...current, ...patch };
      localStorage.setItem('career-user', JSON.stringify(updated));
      return updated;
    });
  };

  const value = useMemo(() => ({ user, login, logout, updateUser }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
