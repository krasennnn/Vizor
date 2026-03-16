import { createContext, useContext, useState, useEffect, useCallback } from "react";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { login, register } from "../api/auth";
import type { LoginRequest, RegisterRequest, User } from "../types/Auth";
import { getUserFromStorage, saveAuth, clearAuth, getAuthToken } from "../lib/auth";
import { setLogoutHandler, clearLogoutHandler } from "../lib/api";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  getToken: () => string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(getUserFromStorage);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  // Load user from storage on mount
  useEffect(() => {
    const storedUser = getUserFromStorage();
    if (storedUser && !user) {
      setUser(storedUser);
    }
  }, []);

  const handleLogin = useCallback(
    async (credentials: LoginRequest) => {
      setIsLoading(true);
      try {
        const response = await login(credentials);
        const userData: User = {
          userId: response.userId,
          email: response.email,
          username: response.username,
          roles: response.roles,
        };
        saveAuth(response.token, userData);
        setUser(userData);
        window.showAppAlert?.("success", "Success", "Logged in successfully");
        navigate("/");
      } catch (error) {
        // Error already handled in API call
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [navigate]
  );

  const handleRegister = useCallback(
    async (userData: RegisterRequest) => {
      setIsLoading(true);
      try {
        await register(userData);
        window.showAppAlert?.("success", "Success", "Registration successful! Please login.");
        navigate("/login");
      } catch (error) {
        // Error already handled in API call
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [navigate]
  );

  const handleLogout = useCallback((isAutomatic: boolean = false) => {
    clearAuth();
    setUser(null);
    if (isAutomatic) {
      window.showAppAlert?.("error", "Session Expired", "Your session has expired. Please log in again.");
    } else {
      window.showAppAlert?.("success", "Success", "Logged out successfully");
    }
    navigate("/login");
  }, [navigate]);

  // Register logout handler for automatic logout on token expiration
  useEffect(() => {
    setLogoutHandler(handleLogout);
    return () => {
      clearLogoutHandler();
    };
  }, [handleLogout]);

  const getToken = useCallback(() => {
    return getAuthToken();
  }, []);

  // Public logout function (without the internal isAutomatic parameter)
  const logout = useCallback(() => {
    handleLogout(false);
  }, [handleLogout]);

  const isAuthenticated = user !== null;

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        login: handleLogin,
        register: handleRegister,
        logout,
        getToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}

