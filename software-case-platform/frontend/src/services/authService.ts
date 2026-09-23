import { apiClient } from './api';

export interface UserProfile {
  id: number;
  nombreCompleto: string;
  email: string;
  rol: 'ADMIN' | 'INGENIERO' | 'ARQUITECTO';
  estado: string;
}

export interface AuthResponse {
  token: string;
  usuario: UserProfile;
  rol: string;
  tipoToken: string;
}

export const authService = {
  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await apiClient.post<AuthResponse>('/api/auth/login', {
      email,
      password,
    });
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.usuario));
      if (response.data.usuario?.email) {
        localStorage.setItem('userEmail', response.data.usuario.email);
      }
      if (response.data.usuario?.nombreCompleto) {
        localStorage.setItem('userName', response.data.usuario.nombreCompleto);
      }
    }
    return response.data;
  },

  async register(
    nombreCompleto: string,
    email: string,
    password: string,
    rol: 'ADMIN' | 'INGENIERO' | 'ARQUITECTO' = 'ARQUITECTO'
  ): Promise<UserProfile> {
    const response = await apiClient.post<UserProfile>('/api/auth/register', {
      nombreCompleto,
      email,
      password,
      rol,
    });
    return response.data;
  },

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
  },

  getCurrentUser(): UserProfile | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  getToken(): string | null {
    return localStorage.getItem('token');
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  },
};
