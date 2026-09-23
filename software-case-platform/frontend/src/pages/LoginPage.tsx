import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { Lock, Mail, User, ShieldCheck } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);

  const [nombreCompleto, setNombreCompleto] = useState('');
  const [email, setEmail] = useState('admin@caseplatform.com');
  const [password, setPassword] = useState('Admin123*');
  const [rol, setRol] = useState<'ADMIN' | 'INGENIERO' | 'ARQUITECTO'>('ARQUITECTO');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (isRegister) {
        await authService.register(nombreCompleto, email, password, rol);
        // Login automático tras registro
        await authService.login(email, password);
      } else {
        await authService.login(email, password);
      }
      navigate('/projects');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Error de autenticación. Verifica tus credenciales.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '80vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
      <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', width: '100%', maxWidth: '420px', padding: '30px', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)' }}>
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div style={{ display: 'inline-flex', padding: '10px', background: '#0284c7', borderRadius: '50%', marginBottom: '10px' }}>
            <ShieldCheck size={28} color="#ffffff" />
          </div>
          <h2 style={{ fontSize: '20px', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
            {isRegister ? 'Registro en CASE Platform' : 'Acceso a CASE Platform'}
          </h2>
          <p style={{ fontSize: '13px', color: '#94a3b8', marginTop: '6px' }}>
            {isRegister ? 'Crea tu cuenta de ingeniero de software' : 'Inicia sesión para acceder a tus diagramas UML 2.5'}
          </p>
        </div>

        {error && (
          <div style={{ background: '#450a0a', color: '#f87171', border: '1px solid #7f1d1d', borderRadius: '6px', padding: '10px', fontSize: '13px', marginBottom: '16px' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {isRegister && (
            <div className="uml-field-group">
              <label className="uml-field-label">Nombre Completo</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="uml-field-input"
                  style={{ width: '100%', paddingLeft: '32px' }}
                  placeholder="Juan Perez"
                  value={nombreCompleto}
                  onChange={(e) => setNombreCompleto(e.target.value)}
                  required
                />
                <User size={14} color="#64748b" style={{ position: 'absolute', left: '10px', top: '12px' }} />
              </div>
            </div>
          )}

          <div className="uml-field-group">
            <label className="uml-field-label">Correo Electrónico</label>
            <div style={{ position: 'relative' }}>
              <input
                type="email"
                className="uml-field-input"
                style={{ width: '100%', paddingLeft: '32px' }}
                placeholder="usuario@caseplatform.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
              <Mail size={14} color="#64748b" style={{ position: 'absolute', left: '10px', top: '12px' }} />
            </div>
          </div>

          <div className="uml-field-group">
            <label className="uml-field-label">Contraseña</label>
            <div style={{ position: 'relative' }}>
              <input
                type="password"
                className="uml-field-input"
                style={{ width: '100%', paddingLeft: '32px' }}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <Lock size={14} color="#64748b" style={{ position: 'absolute', left: '10px', top: '12px' }} />
            </div>
          </div>

          {isRegister && (
            <div className="uml-field-group">
              <label className="uml-field-label">Rol del Sistema</label>
              <select
                className="uml-field-select"
                value={rol}
                onChange={(e) => setRol(e.target.value as 'ADMIN' | 'INGENIERO' | 'ARQUITECTO')}
              >
                <option value="ARQUITECTO">ARQUITECTO (Diseño y Modelado UML)</option>
                <option value="INGENIERO">INGENIERO (Desarrollo y Modelado UML)</option>
                <option value="ADMIN">ADMIN (Administración General)</option>
              </select>
            </div>
          )}

          <button
            type="submit"
            className="uml-toolbar-btn primary"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', padding: '10px', marginTop: '6px', fontSize: '14px' }}
          >
            {loading ? 'Procesando...' : isRegister ? 'Crear Cuenta' : 'Iniciar Sesión'}
          </button>
        </form>

        {!isRegister && (
          <div style={{ marginTop: '16px', padding: '12px', background: '#0f172a', borderRadius: '8px', border: '1px solid #334155' }}>
            <div style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '8px', fontWeight: 600 }}>
              Credenciales de prueba preconfiguradas:
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '6px' }}>
              <button
                type="button"
                className="uml-toolbar-btn"
                style={{ fontSize: '11px', justifyContent: 'center', padding: '6px 4px' }}
                onClick={() => {
                  setEmail('admin@caseplatform.com');
                  setPassword('Admin123*');
                }}
              >
                👑 Admin
              </button>
              <button
                type="button"
                className="uml-toolbar-btn"
                style={{ fontSize: '11px', justifyContent: 'center', padding: '6px 4px' }}
                onClick={() => {
                  setEmail('arquitecto@caseplatform.com');
                  setPassword('Arquitecto123*');
                }}
              >
                🏗️ Arquitecto
              </button>
              <button
                type="button"
                className="uml-toolbar-btn"
                style={{ fontSize: '11px', justifyContent: 'center', padding: '6px 4px' }}
                onClick={() => {
                  setEmail('ingeniero@caseplatform.com');
                  setPassword('Ingeniero123*');
                }}
              >
                🛠️ Ingeniero
              </button>
            </div>
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: '20px', paddingTop: '16px', borderTop: '1px solid #334155', fontSize: '13px', color: '#94a3b8' }}>
          {isRegister ? (
            <span>
              ¿Ya tienes cuenta?{' '}
              <button
                type="button"
                onClick={() => setIsRegister(false)}
                style={{ background: 'none', border: 'none', color: '#38bdf8', cursor: 'pointer', fontWeight: 600 }}
              >
                Inicia sesión
              </button>
            </span>
          ) : (
            <span>
              ¿No tienes cuenta?{' '}
              <button
                type="button"
                onClick={() => setIsRegister(true)}
                style={{ background: 'none', border: 'none', color: '#38bdf8', cursor: 'pointer', fontWeight: 600 }}
              >
                Regístrate aquí
              </button>
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
