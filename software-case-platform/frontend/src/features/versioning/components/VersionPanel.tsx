import React, { useEffect, useState } from 'react';
import {
  GitCommit,
  GitBranch,
  History,
  RotateCcw,
  Plus,
  RefreshCw,
  X,
  CheckCircle2,
  Calendar,
  User,
  AlertCircle,
  FileText,
} from 'lucide-react';
import { HistorialCambioDTO, VersionModeloDTO } from '../models/version.types';
import { versionService } from '../services/versionService';
import { CreateVersionModal } from './CreateVersionModal';
import { RestoreVersionModal } from './RestoreVersionModal';
import { ModeloUML } from '../../uml-editor/models/uml.types';

interface VersionPanelProps {
  isOpen: boolean;
  onClose: () => void;
  modeloId: number;
  nombreModelo: string;
  versionActual: string;
  onVersionRestored: (modeloRestaurado: ModeloUML) => void;
}

export const VersionPanel: React.FC<VersionPanelProps> = ({
  isOpen,
  onClose,
  modeloId,
  nombreModelo,
  versionActual,
  onVersionRestored,
}) => {
  const [activeTab, setActiveTab] = useState<'versiones' | 'historial'>('versiones');
  const [versiones, setVersiones] = useState<VersionModeloDTO[]>([]);
  const [historial, setHistorial] = useState<HistorialCambioDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedVersionToRestore, setSelectedVersionToRestore] = useState<VersionModeloDTO | null>(null);
  const [filtroCambios, setFiltroCambios] = useState('');

  const cargarDatos = async () => {
    if (!modeloId) return;
    setIsLoading(true);
    setError(null);
    try {
      const [versionesRes, historialRes] = await Promise.all([
        versionService.getVersionesPorModelo(modeloId),
        versionService.getHistorialPorModelo(modeloId),
      ]);
      setVersiones(versionesRes);
      setHistorial(historialRes);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Error al cargar versiones e historial');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      cargarDatos();
    }
  }, [isOpen, modeloId]);

  if (!isOpen) return null;

  const handleCreateVersion = async (nombre: string, numeroVersion: string, descripcion: string) => {
    const nueva = await versionService.crearVersion({
      modeloId,
      nombreVersion: nombre,
      numeroVersion: numeroVersion || undefined,
      descripcion: descripcion || undefined,
    });
    setSuccessMessage(`Versión '${nueva.nombreVersion}' (${nueva.numeroVersion}) creada exitosamente.`);
    setTimeout(() => setSuccessMessage(null), 5000);
    await cargarDatos();
  };

  const handleConfirmRestore = async (versionId: number, comentario: string) => {
    const modeloRestaurado = await versionService.restaurarVersion(versionId, { comentario });
    setSuccessMessage(`Modelo restaurado exitosamente a la versión seleccionada.`);
    setTimeout(() => setSuccessMessage(null), 5000);
    onVersionRestored(modeloRestaurado);
    await cargarDatos();
  };

  const getOperationBadge = (tipo: string) => {
    switch (tipo) {
      case 'CREATE':
        return { label: 'CREACIÓN', color: '#10b981', bg: 'rgba(16, 185, 129, 0.15)' };
      case 'UPDATE':
        return { label: 'EDICIÓN', color: '#f59e0b', bg: 'rgba(245, 158, 11, 0.15)' };
      case 'DELETE':
        return { label: 'ELIMINACIÓN', color: '#ef4444', bg: 'rgba(239, 68, 68, 0.15)' };
      case 'RESTORE':
        return { label: 'RESTAURACIÓN', color: '#3b82f6', bg: 'rgba(59, 130, 246, 0.15)' };
      default:
        return { label: tipo, color: '#94a3b8', bg: 'rgba(148, 163, 184, 0.15)' };
    }
  };

  const historialFiltrado = historial.filter((item) => {
    if (!filtroCambios) return true;
    const query = filtroCambios.toLowerCase();
    return (
      item.descripcionResumen.toLowerCase().includes(query) ||
      item.elementoModificado.toLowerCase().includes(query) ||
      item.usuarioNombre.toLowerCase().includes(query) ||
      item.usuarioEmail.toLowerCase().includes(query)
    );
  });

  return (
    <div className="uml-modal-overlay">
      <div className="uml-modal-content" style={{ maxWidth: '850px', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <div className="uml-modal-header" style={{ paddingBottom: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <History size={22} style={{ color: '#3b82f6' }} />
            <div>
              <h3 style={{ margin: 0, fontSize: '18px', color: '#f8fafc' }}>
                Historial y Control de Versiones UML
              </h3>
              <div style={{ fontSize: '12px', color: '#94a3b8', marginTop: '2px' }}>
                Modelo: <strong style={{ color: '#cbd5e1' }}>{nombreModelo}</strong> &bull; Versión actual: <strong style={{ color: '#38bdf8' }}>v{versionActual}</strong>
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button
              className="uml-toolbar-btn"
              onClick={cargarDatos}
              disabled={isLoading}
              title="Refrescar datos"
            >
              <RefreshCw size={14} className={isLoading ? 'animate-spin' : ''} />
            </button>
            <button className="uml-modal-close" onClick={onClose}>
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Notificaciones */}
        {successMessage && (
          <div style={{ margin: '12px 20px 0', padding: '10px 14px', backgroundColor: 'rgba(16, 185, 129, 0.15)', border: '1px solid #10b981', borderRadius: '6px', color: '#34d399', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CheckCircle2 size={16} />
            <span>{successMessage}</span>
          </div>
        )}

        {error && (
          <div style={{ margin: '12px 20px 0', padding: '10px 14px', backgroundColor: 'rgba(239, 68, 68, 0.15)', border: '1px solid #ef4444', borderRadius: '6px', color: '#f87171', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}

        {/* Tab Navigation */}
        <div style={{ display: 'flex', borderBottom: '1px solid #334155', margin: '12px 20px 0' }}>
          <button
            onClick={() => setActiveTab('versiones')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '10px 16px',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === 'versiones' ? '2px solid #3b82f6' : '2px solid transparent',
              color: activeTab === 'versiones' ? '#60a5fa' : '#94a3b8',
              fontWeight: 600,
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            <GitBranch size={16} /> Versiones del Modelo ({versiones.length})
          </button>
          <button
            onClick={() => setActiveTab('historial')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              padding: '10px 16px',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === 'historial' ? '2px solid #3b82f6' : '2px solid transparent',
              color: activeTab === 'historial' ? '#60a5fa' : '#94a3b8',
              fontWeight: 600,
              fontSize: '13px',
              cursor: 'pointer',
            }}
          >
            <GitCommit size={16} /> Registro de Cambios y Trazabilidad ({historial.length})
          </button>
        </div>

        {/* Body Content */}
        <div className="uml-modal-body" style={{ flex: 1, overflowY: 'auto', padding: '16px 20px' }}>
          {activeTab === 'versiones' ? (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                <span style={{ fontSize: '13px', color: '#94a3b8' }}>
                  Hitos congelados del diseño con snapshots completos de clases, relaciones y posiciones.
                </span>
                <button
                  className="uml-toolbar-btn primary"
                  onClick={() => setIsCreateModalOpen(true)}
                  style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <Plus size={14} /> Nueva Versión
                </button>
              </div>

              {versiones.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '36px 12px', color: '#64748b' }}>
                  <FileText size={32} style={{ margin: '0 auto 10px', opacity: 0.5 }} />
                  <p style={{ margin: 0, fontSize: '14px' }}>Aún no hay versiones formalizadas para este modelo.</p>
                  <p style={{ margin: '6px 0 0', fontSize: '12px' }}>
                    Haga clic en <strong>Nueva Versión</strong> para congelar el primer snapshot estable.
                  </p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {versiones.map((v) => (
                    <div
                      key={v.id}
                      style={{
                        backgroundColor: '#1e293b',
                        border: '1px solid #334155',
                        borderRadius: '8px',
                        padding: '14px 16px',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                      }}
                    >
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <span
                            style={{
                              backgroundColor: '#2563eb',
                              color: '#ffffff',
                              fontSize: '11px',
                              fontWeight: 700,
                              padding: '2px 8px',
                              borderRadius: '4px',
                            }}
                          >
                            {v.numeroVersion}
                          </span>
                          <span style={{ fontSize: '14px', fontWeight: 600, color: '#f8fafc' }}>
                            {v.nombreVersion}
                          </span>
                          <span
                            style={{
                              backgroundColor: 'rgba(16, 185, 129, 0.15)',
                              color: '#34d399',
                              fontSize: '11px',
                              padding: '2px 6px',
                              borderRadius: '4px',
                            }}
                          >
                            {v.estado}
                          </span>
                        </div>

                        {v.descripcion && (
                          <p style={{ margin: '4px 0 8px 0', fontSize: '12px', color: '#94a3b8' }}>
                            {v.descripcion}
                          </p>
                        )}

                        <div style={{ display: 'flex', gap: '16px', fontSize: '11px', color: '#64748b' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Calendar size={12} /> {new Date(v.fechaCreacion).toLocaleString()}
                          </span>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <User size={12} /> {v.usuarioCreadorNombre || v.usuarioCreadorEmail || 'Usuario'}
                          </span>
                        </div>
                      </div>

                      <div>
                        <button
                          className="uml-toolbar-btn"
                          onClick={() => setSelectedVersionToRestore(v)}
                          title="Restaurar el modelo conceptual al estado de esta versión"
                          style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px' }}
                        >
                          <RotateCcw size={13} style={{ color: '#f59e0b' }} />
                          <span>Restaurar</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div>
              {/* Barra de Búsqueda y Filtrado */}
              <div style={{ marginBottom: '14px' }}>
                <input
                  type="text"
                  className="uml-input"
                  placeholder="Buscar en el historial (ej. Juan, Cliente, eliminó...)"
                  value={filtroCambios}
                  onChange={(e) => setFiltroCambios(e.target.value)}
                  style={{ width: '100%', fontSize: '13px' }}
                />
              </div>

              {historialFiltrado.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '36px 12px', color: '#64748b' }}>
                  <History size={32} style={{ margin: '0 auto 10px', opacity: 0.5 }} />
                  <p style={{ margin: 0, fontSize: '14px' }}>No hay registros que coincidan con la búsqueda.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {historialFiltrado.map((h) => {
                    const badge = getOperationBadge(h.tipoOperacion);
                    return (
                      <div
                        key={h.id}
                        style={{
                          backgroundColor: '#1e293b',
                          border: '1px solid #334155',
                          borderRadius: '6px',
                          padding: '10px 14px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          gap: '12px',
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1 }}>
                          <span
                            style={{
                              backgroundColor: badge.bg,
                              color: badge.color,
                              fontSize: '10px',
                              fontWeight: 700,
                              padding: '2px 6px',
                              borderRadius: '4px',
                              minWidth: '70px',
                              textAlign: 'center',
                            }}
                          >
                            {badge.label}
                          </span>
                          <div>
                            <div style={{ fontSize: '13px', color: '#f1f5f9', fontWeight: 500 }}>
                              {h.descripcionResumen}
                            </div>
                            <div style={{ fontSize: '11px', color: '#64748b', marginTop: '2px' }}>
                              Elemento: <strong>{h.elementoModificado}</strong> {h.idElemento ? `[ID: ${h.idElemento}]` : ''}
                            </div>
                          </div>
                        </div>

                        <div style={{ textAlign: 'right', fontSize: '11px', color: '#64748b', whiteSpace: 'nowrap' }}>
                          <div>{new Date(h.fechaCambio).toLocaleDateString()}</div>
                          <div>{new Date(h.fechaCambio).toLocaleTimeString()}</div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Modales subordinados */}
        <CreateVersionModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSubmit={handleCreateVersion}
          currentVersion={versionActual}
        />

        <RestoreVersionModal
          isOpen={!!selectedVersionToRestore}
          version={selectedVersionToRestore}
          onClose={() => setSelectedVersionToRestore(null)}
          onConfirm={handleConfirmRestore}
        />
      </div>
    </div>
  );
};
