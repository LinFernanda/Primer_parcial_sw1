import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { umlService } from '../features/uml-editor/services/umlService';
import { authService, UserProfile } from '../services/authService';
import { ProyectoUML } from '../features/uml-editor/models/uml.types';
import { FolderGit2, Plus, LogOut, ArrowRight, Trash2, User } from 'lucide-react';

export const ProjectsPage: React.FC = () => {
  const navigate = useNavigate();
  const [proyectos, setProyectos] = useState<ProyectoUML[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);

  // Formulario nuevo proyecto
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    // Si no está autenticado, usar usuario demo o redirigir
    const user = authService.getCurrentUser();
    if (!user && !authService.isAuthenticated()) {
      // Iniciar sesión silenciosa o redirigir a /login
      navigate('/login');
      return;
    }
    setCurrentUser(user);
    loadProjects();
  }, [navigate]);

  const loadProjects = async () => {
    setLoading(true);
    try {
      const list = await umlService.getProyectos();
      setProyectos(list);
    } catch (err) {
      console.error('Error al cargar proyectos:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nombre.trim()) return;

    setCreating(true);
    try {
      const nuevo = await umlService.createProyecto({
        nombre: nombre.trim(),
        descripcion: descripcion.trim(),
      });
      setNombre('');
      setDescripcion('');
      // Redirigir directamente al editor del nuevo proyecto
      navigate(`/projects/${nuevo.id}/editor`);
    } catch (err) {
      console.error('Error al crear proyecto:', err);
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteProject = async (id: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm('¿Seguro que deseas eliminar este proyecto y todos sus diagramas UML?')) return;
    try {
      await umlService.deleteProyecto(id);
      setProyectos((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      console.error('Error al eliminar proyecto:', err);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div style={{ maxWidth: '1100px', margin: '30px auto', padding: '0 20px' }}>
      {/* Barra superior de perfil */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #334155' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
            Proyectos de Modelado UML 2.5
          </h1>
          <p style={{ color: '#94a3b8', fontSize: '14px', marginTop: '4px' }}>
            Pizarra colaborativa para diseño de arquitectura conceptual y generación de software
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          {currentUser && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', background: '#1e293b', padding: '6px 12px', borderRadius: '20px', border: '1px solid #334155' }}>
              <User size={14} color="#38bdf8" />
              <span>{currentUser.nombreCompleto} ({currentUser.rol})</span>
            </div>
          )}
          <button className="uml-toolbar-btn" onClick={handleLogout} title="Cerrar sesión">
            <LogOut size={14} /> Salir
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '28px' }}>
        {/* Lista de Proyectos Existentes */}
        <div>
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: '#f8fafc', marginBottom: '16px' }}>
            Tus Proyectos Activos ({proyectos.length})
          </h2>

          {loading ? (
            <div style={{ color: '#94a3b8', padding: '20px' }}>Cargando proyectos...</div>
          ) : proyectos.length === 0 ? (
            <div style={{ background: '#1e293b', padding: '30px', borderRadius: '8px', border: '1px solid #334155', textAlign: 'center' }}>
              <FolderGit2 size={36} color="#64748b" style={{ margin: '0 auto 12px' }} />
              <div style={{ fontWeight: 600, color: '#e2e8f0' }}>No tienes proyectos UML todavía</div>
              <p style={{ color: '#94a3b8', fontSize: '13px', marginTop: '6px' }}>
                Crea tu primer proyecto en el panel derecho para empezar a diseñar diagramas de clases.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {proyectos.map((p) => (
                <div
                  key={p.id}
                  onClick={() => navigate(`/projects/${p.id}/editor`)}
                  style={{
                    background: '#1e293b',
                    border: '1px solid #334155',
                    borderRadius: '8px',
                    padding: '16px 20px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    transition: 'border-color 0.15s, background-color 0.15s',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#3b82f6')}
                  onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#334155')}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '16px', fontWeight: 600, color: '#f8fafc' }}>
                        {p.nombre}
                      </span>
                      <span style={{ fontSize: '11px', background: '#0284c7', color: '#fff', padding: '2px 8px', borderRadius: '10px' }}>
                        {p.estado}
                      </span>
                    </div>
                    {p.descripcion && (
                      <p style={{ color: '#94a3b8', fontSize: '13px', margin: '4px 0 0' }}>
                        {p.descripcion}
                      </p>
                    )}
                    <span style={{ fontSize: '11px', color: '#64748b', marginTop: '6px', display: 'inline-block' }}>
                      Modelos disponibles: {p.modelos?.length || 1}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <button
                      className="uml-del-btn"
                      onClick={(e) => handleDeleteProject(p.id, e)}
                      title="Eliminar proyecto"
                    >
                      <Trash2 size={16} />
                    </button>
                    <button className="uml-toolbar-btn primary" style={{ padding: '6px 14px' }}>
                      Abrir Editor <ArrowRight size={14} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Formulario para Nuevo Proyecto */}
        <div>
          <div style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '8px', padding: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#f8fafc', margin: '0 0 14px' }}>
              Crear Nuevo Proyecto UML
            </h3>
            <form onSubmit={handleCreateProject} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div className="uml-field-group">
                <label className="uml-field-label">Nombre del Proyecto *</label>
                <input
                  type="text"
                  className="uml-field-input"
                  placeholder="Ej. Sistema de Pagos, ERP Clínico"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  required
                />
              </div>

              <div className="uml-field-group">
                <label className="uml-field-label">Descripción</label>
                <textarea
                  className="uml-field-input"
                  rows={3}
                  placeholder="Objetivos de diseño conceptual del sistema..."
                  value={descripcion}
                  onChange={(e) => setDescripcion(e.target.value)}
                  style={{ resize: 'vertical' }}
                />
              </div>

              <button
                type="submit"
                className="uml-toolbar-btn primary"
                disabled={creating || !nombre.trim()}
                style={{ width: '100%', justifyContent: 'center', padding: '8px 16px' }}
              >
                <Plus size={16} /> {creating ? 'Creando...' : 'Crear y Abrir Editor'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
