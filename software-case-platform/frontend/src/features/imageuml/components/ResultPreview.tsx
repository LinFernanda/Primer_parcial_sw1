import React, { useState } from 'react';
import {
  ImageUMLDetectedDTO,
  ClaseDetectadaDTO,
  RelacionDetectadaDTO,
  AtributoDetectadoDTO,
  MetodoDetectadoDTO,
  TipoRelacionUML,
  VisibilidadUML,
} from '../models/imageuml.types';
import {
  AlertTriangle,
  CheckCircle2,
  Cpu,
  Plus,
  Trash2,
  Box,
  Link2,
  ChevronDown,
  ChevronRight,
  Eye,
  RefreshCw,
} from 'lucide-react';

interface ResultPreviewProps {
  previewUml: ImageUMLDetectedDTO;
  filePreviewUrl: string | null;
  limpiarModeloExistente: boolean;
  setLimpiarModeloExistente: (val: boolean) => void;
  onUpdateClass: (index: number, updated: Partial<ClaseDetectadaDTO>) => void;
  onDeleteClass: (index: number) => void;
  onAddClass: (clase: ClaseDetectadaDTO) => void;
  onUpdateAttribute: (claseIndex: number, attrIndex: number, updated: Partial<AtributoDetectadoDTO>) => void;
  onDeleteAttribute: (claseIndex: number, attrIndex: number) => void;
  onAddAttribute: (claseIndex: number, attr: AtributoDetectadoDTO) => void;
  onUpdateMethod: (claseIndex: number, metodoIndex: number, updated: Partial<MetodoDetectadoDTO>) => void;
  onDeleteMethod: (claseIndex: number, metodoIndex: number) => void;
  onAddMethod: (claseIndex: number, metodo: MetodoDetectadoDTO) => void;
  onUpdateRelation: (index: number, updated: Partial<RelacionDetectadaDTO>) => void;
  onDeleteRelation: (index: number) => void;
  onAddRelation: (relacion: RelacionDetectadaDTO) => void;
  onApply: () => void;
  onCancel: () => void;
  isLoading: boolean;
}

export const ResultPreview: React.FC<ResultPreviewProps> = ({
  previewUml,
  filePreviewUrl,
  limpiarModeloExistente,
  setLimpiarModeloExistente,
  onUpdateClass,
  onDeleteClass,
  onAddClass,
  onUpdateAttribute,
  onDeleteAttribute,
  onAddAttribute,
  onUpdateMethod,
  onDeleteMethod,
  onAddMethod,
  onUpdateRelation,
  onDeleteRelation,
  onAddRelation,
  onApply,
  onCancel,
  isLoading,
}) => {
  const [expandedClasses, setExpandedClasses] = useState<Record<number, boolean>>({ 0: true, 1: true });
  const [showImageComparison, setShowImageComparison] = useState(true);

  const toggleClassExpand = (index: number) => {
    setExpandedClasses((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  const handleAddNewClass = () => {
    const defaultName = `NuevaClase${previewUml.clases.length + 1}`;
    onAddClass({
      nombre: defaultName,
      visibilidad: 'PUBLIC',
      posicionX: 100 + previewUml.clases.length * 40,
      posicionY: 100 + previewUml.clases.length * 40,
      atributos: [{ nombre: 'id', tipoDato: 'Long', visibilidad: 'PRIVATE' }],
      metodos: [],
    });
  };

  const handleAddNewRelation = () => {
    if (previewUml.clases.length === 0) return;
    const origen = previewUml.clases[0]?.nombre || 'ClaseA';
    const destino = previewUml.clases[1]?.nombre || origen;
    onAddRelation({
      claseOrigen: origen,
      claseDestino: destino,
      tipoRelacion: 'ASOCIACION',
      cardinalidadOrigen: '1',
      cardinalidadDestino: '*',
    });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Barra de metadatos y confianza */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '12px',
          padding: '12px 16px',
          backgroundColor: '#0f172a',
          borderRadius: '8px',
          border: '1px solid #334155',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '4px 10px',
              borderRadius: '20px',
              backgroundColor: 'rgba(16, 185, 129, 0.15)',
              border: '1px solid #10b981',
              color: '#34d399',
              fontSize: '12px',
              fontWeight: 600,
            }}
          >
            <CheckCircle2 size={14} />
            Confianza: {Math.round((previewUml.nivelConfianza || 0.95) * 100)}%
          </span>

          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '4px 10px',
              borderRadius: '20px',
              backgroundColor: 'rgba(59, 130, 246, 0.15)',
              border: '1px solid #3b82f6',
              color: '#60a5fa',
              fontSize: '12px',
            }}
          >
            <Cpu size={14} />
            Motor: {previewUml.motorUtilizado || 'Visión Computacional & OCR'}
          </span>
        </div>

        {filePreviewUrl && (
          <button
            type="button"
            className="uml-toolbar-btn"
            onClick={() => setShowImageComparison((prev) => !prev)}
            style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <Eye size={14} /> {showImageComparison ? 'Ocultar Imagen Original' : 'Ver Imagen Original'}
          </button>
        )}
      </div>

      {/* Vista de comparación lado a lado con la imagen subida */}
      {showImageComparison && filePreviewUrl && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: '#020617',
            border: '1px solid #334155',
            borderRadius: '8px',
            padding: '12px',
            maxHeight: '220px',
            overflow: 'hidden',
          }}
        >
          <img
            src={filePreviewUrl}
            alt="Imagen cargada del diagrama UML"
            style={{ maxHeight: '200px', maxWidth: '100%', objectFit: 'contain', borderRadius: '4px' }}
          />
        </div>
      )}

      {/* Advertencias de detección si existen */}
      {previewUml.advertencias && previewUml.advertencias.length > 0 && (
        <div
          style={{
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            border: '1px solid #f59e0b',
            borderRadius: '8px',
            padding: '12px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#fbbf24', fontWeight: 600, fontSize: '13px', marginBottom: '6px' }}>
            <AlertTriangle size={15} />
            Advertencias de Reconocimiento
          </div>
          <ul style={{ margin: 0, paddingLeft: '20px', color: '#fcd34d', fontSize: '12px' }}>
            {previewUml.advertencias.map((adv, idx) => (
              <li key={idx}>{adv}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Sección 1: Clases Detectadas */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <h4 style={{ margin: 0, color: '#f8fafc', fontSize: '15px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Box size={16} style={{ color: '#38bdf8' }} />
            Clases Detectadas ({previewUml.clases.length})
          </h4>
          <button
            type="button"
            className="uml-toolbar-btn"
            onClick={handleAddNewClass}
            style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}
          >
            <Plus size={14} /> Añadir Clase
          </button>
        </div>

        {previewUml.clases.length === 0 ? (
          <p style={{ color: '#94a3b8', fontSize: '13px', fontStyle: 'italic' }}>
            No se detectaron clases en la imagen. Puedes agregar una manualmente.
          </p>
        ) : (
          previewUml.clases.map((clase, cIdx) => {
            const isExpanded = expandedClasses[cIdx] ?? true;

            return (
              <div
                key={cIdx}
                style={{
                  backgroundColor: '#1e293b',
                  border: '1px solid #334155',
                  borderRadius: '8px',
                  overflow: 'hidden',
                }}
              >
                {/* Cabecera de la Clase */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '10px 14px',
                    backgroundColor: '#0f172a',
                    borderBottom: isExpanded ? '1px solid #334155' : 'none',
                    gap: '12px',
                  }}
                >
                  <div
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', flex: 1 }}
                    onClick={() => toggleClassExpand(cIdx)}
                  >
                    {isExpanded ? <ChevronDown size={16} color="#94a3b8" /> : <ChevronRight size={16} color="#94a3b8" />}
                    <span style={{ color: '#38bdf8', fontWeight: 600, fontSize: '14px' }}>Clase:</span>
                    <input
                      type="text"
                      className="uml-field-input"
                      value={clase.nombre}
                      onClick={(e) => e.stopPropagation()}
                      onChange={(e) => onUpdateClass(cIdx, { nombre: e.target.value })}
                      placeholder="Nombre de la clase"
                      style={{
                        padding: '4px 8px',
                        fontSize: '13px',
                        fontWeight: 600,
                        color: '#f8fafc',
                        backgroundColor: '#1e293b',
                        border: '1px solid #475569',
                        borderRadius: '4px',
                        maxWidth: '220px',
                      }}
                    />
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <select
                      className="uml-field-select"
                      value={clase.visibilidad}
                      onChange={(e) => onUpdateClass(cIdx, { visibilidad: e.target.value as VisibilidadUML })}
                      style={{ padding: '4px 8px', fontSize: '12px' }}
                    >
                      <option value="PUBLIC">public (+)</option>
                      <option value="PRIVATE">private (-)</option>
                      <option value="PROTECTED">protected (#)</option>
                      <option value="PACKAGE">package (~)</option>
                    </select>

                    <button
                      type="button"
                      className="uml-del-btn"
                      onClick={() => onDeleteClass(cIdx)}
                      title="Eliminar clase"
                      style={{ color: '#ef4444', padding: '4px' }}
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>

                {/* Contenido expandible: Atributos y Métodos */}
                {isExpanded && (
                  <div style={{ padding: '14px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    {/* Atributos */}
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                        <span style={{ fontSize: '12px', fontWeight: 600, color: '#94a3b8' }}>
                          Atributos ({clase.atributos.length})
                        </span>
                        <button
                          type="button"
                          className="uml-toolbar-btn"
                          onClick={() =>
                            onAddAttribute(cIdx, {
                              nombre: 'nuevoAtributo',
                              tipoDato: 'String',
                              visibilidad: 'PRIVATE',
                            })
                          }
                          style={{ fontSize: '11px', padding: '2px 8px' }}
                        >
                          <Plus size={12} /> Atributo
                        </button>
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        {clase.atributos.map((attr, aIdx) => (
                          <div key={aIdx} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <select
                              className="uml-field-select"
                              value={attr.visibilidad}
                              onChange={(e) =>
                                onUpdateAttribute(cIdx, aIdx, { visibilidad: e.target.value as VisibilidadUML })
                              }
                              style={{ width: '60px', padding: '2px 4px', fontSize: '11px' }}
                            >
                              <option value="PRIVATE">-</option>
                              <option value="PUBLIC">+</option>
                              <option value="PROTECTED">#</option>
                              <option value="PACKAGE">~</option>
                            </select>

                            <input
                              type="text"
                              className="uml-field-input"
                              value={attr.nombre}
                              onChange={(e) => onUpdateAttribute(cIdx, aIdx, { nombre: e.target.value })}
                              placeholder="nombre"
                              style={{ flex: 1, padding: '3px 6px', fontSize: '12px' }}
                            />

                            <span style={{ color: '#94a3b8', fontSize: '12px' }}>:</span>

                            <input
                              type="text"
                              className="uml-field-input"
                              value={attr.tipoDato}
                              onChange={(e) => onUpdateAttribute(cIdx, aIdx, { tipoDato: e.target.value })}
                              placeholder="tipo (String, Long, int...)"
                              style={{ width: '100px', padding: '3px 6px', fontSize: '12px' }}
                            />

                            <button
                              type="button"
                              className="uml-del-btn"
                              onClick={() => onDeleteAttribute(cIdx, aIdx)}
                              style={{ color: '#f87171' }}
                            >
                              &times;
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Métodos */}
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                        <span style={{ fontSize: '12px', fontWeight: 600, color: '#94a3b8' }}>
                          Métodos ({clase.metodos.length})
                        </span>
                        <button
                          type="button"
                          className="uml-toolbar-btn"
                          onClick={() =>
                            onAddMethod(cIdx, {
                              nombre: 'nuevoMetodo',
                              tipoRetorno: 'void',
                              visibilidad: 'PUBLIC',
                              parametros: '',
                            })
                          }
                          style={{ fontSize: '11px', padding: '2px 8px' }}
                        >
                          <Plus size={12} /> Método
                        </button>
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        {clase.metodos.map((met, mIdx) => (
                          <div key={mIdx} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <select
                              className="uml-field-select"
                              value={met.visibilidad}
                              onChange={(e) =>
                                onUpdateMethod(cIdx, mIdx, { visibilidad: e.target.value as VisibilidadUML })
                              }
                              style={{ width: '60px', padding: '2px 4px', fontSize: '11px' }}
                            >
                              <option value="PUBLIC">+</option>
                              <option value="PRIVATE">-</option>
                              <option value="PROTECTED">#</option>
                              <option value="PACKAGE">~</option>
                            </select>

                            <input
                              type="text"
                              className="uml-field-input"
                              value={met.nombre}
                              onChange={(e) => onUpdateMethod(cIdx, mIdx, { nombre: e.target.value })}
                              placeholder="nombreMetodo"
                              style={{ flex: 1, padding: '3px 6px', fontSize: '12px' }}
                            />

                            <input
                              type="text"
                              className="uml-field-input"
                              value={met.parametros || ''}
                              onChange={(e) => onUpdateMethod(cIdx, mIdx, { parametros: e.target.value })}
                              placeholder="(params)"
                              style={{ width: '90px', padding: '3px 6px', fontSize: '12px' }}
                            />

                            <span style={{ color: '#94a3b8', fontSize: '12px' }}>:</span>

                            <input
                              type="text"
                              className="uml-field-input"
                              value={met.tipoRetorno}
                              onChange={(e) => onUpdateMethod(cIdx, mIdx, { tipoRetorno: e.target.value })}
                              placeholder="void"
                              style={{ width: '80px', padding: '3px 6px', fontSize: '12px' }}
                            />

                            <button
                              type="button"
                              className="uml-del-btn"
                              onClick={() => onDeleteMethod(cIdx, mIdx)}
                              style={{ color: '#f87171' }}
                            >
                              &times;
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      {/* Sección 2: Relaciones Detectadas */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <h4 style={{ margin: 0, color: '#f8fafc', fontSize: '15px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Link2 size={16} style={{ color: '#10b981' }} />
            Relaciones y Cardinalidades ({previewUml.relaciones.length})
          </h4>
          <button
            type="button"
            className="uml-toolbar-btn"
            onClick={handleAddNewRelation}
            disabled={previewUml.clases.length < 2}
            style={{ fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}
          >
            <Plus size={14} /> Añadir Relación
          </button>
        </div>

        {previewUml.relaciones.length === 0 ? (
          <p style={{ color: '#94a3b8', fontSize: '13px', fontStyle: 'italic' }}>
            No se detectaron relaciones entre clases. Puedes añadir relaciones si lo requieres.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {previewUml.relaciones.map((rel, rIdx) => (
              <div
                key={rIdx}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '8px',
                  backgroundColor: '#1e293b',
                  border: '1px solid #334155',
                  borderRadius: '6px',
                  padding: '8px 12px',
                }}
              >
                {/* Clase Origen */}
                <select
                  className="uml-field-select"
                  value={rel.claseOrigen}
                  onChange={(e) => onUpdateRelation(rIdx, { claseOrigen: e.target.value })}
                  style={{ minWidth: '110px', fontSize: '12px' }}
                >
                  {previewUml.clases.map((c, i) => (
                    <option key={i} value={c.nombre}>
                      {c.nombre}
                    </option>
                  ))}
                </select>

                {/* Cardinalidad Origen */}
                <input
                  type="text"
                  className="uml-field-input"
                  value={rel.cardinalidadOrigen}
                  onChange={(e) => onUpdateRelation(rIdx, { cardinalidadOrigen: e.target.value })}
                  placeholder="1"
                  style={{ width: '42px', padding: '3px 4px', textAlign: 'center', fontSize: '12px' }}
                  title="Cardinalidad Origen (1, 0..1, *, 1..*)"
                />

                {/* Tipo de Relación */}
                <select
                  className="uml-field-select"
                  value={rel.tipoRelacion}
                  onChange={(e) => onUpdateRelation(rIdx, { tipoRelacion: e.target.value as TipoRelacionUML })}
                  style={{ minWidth: '120px', fontSize: '12px', fontWeight: 600, color: '#38bdf8' }}
                >
                  <option value="ASOCIACION">━━ ASOCIACIÓN</option>
                  <option value="HERENCIA">──▷ HERENCIA</option>
                  <option value="AGREGACION">◇── AGREGACIÓN</option>
                  <option value="COMPOSICION">◆── COMPOSICIÓN</option>
                  <option value="DEPENDENCIA"> - - ▷ DEPENDENCIA</option>
                </select>

                {/* Cardinalidad Destino */}
                <input
                  type="text"
                  className="uml-field-input"
                  value={rel.cardinalidadDestino}
                  onChange={(e) => onUpdateRelation(rIdx, { cardinalidadDestino: e.target.value })}
                  placeholder="*"
                  style={{ width: '42px', padding: '3px 4px', textAlign: 'center', fontSize: '12px' }}
                  title="Cardinalidad Destino (1, 0..1, *, 1..*)"
                />

                {/* Clase Destino */}
                <select
                  className="uml-field-select"
                  value={rel.claseDestino}
                  onChange={(e) => onUpdateRelation(rIdx, { claseDestino: e.target.value })}
                  style={{ minWidth: '110px', fontSize: '12px' }}
                >
                  {previewUml.clases.map((c, i) => (
                    <option key={i} value={c.nombre}>
                      {c.nombre}
                    </option>
                  ))}
                </select>

                <button
                  type="button"
                  className="uml-del-btn"
                  onClick={() => onDeleteRelation(rIdx)}
                  style={{ color: '#ef4444', marginLeft: 'auto' }}
                  title="Eliminar relación"
                >
                  <Trash2 size={15} />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Opciones de Inserción en el Diagrama */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '10px 14px',
          backgroundColor: '#0f172a',
          borderRadius: '8px',
          border: '1px solid #334155',
        }}
      >
        <input
          type="checkbox"
          id="limpiarModelo"
          checked={limpiarModeloExistente}
          onChange={(e) => setLimpiarModeloExistente(e.target.checked)}
          style={{ cursor: 'pointer' }}
        />
        <label htmlFor="limpiarModelo" style={{ color: '#e2e8f0', fontSize: '13px', cursor: 'pointer' }}>
          Limpiar/reemplazar el diagrama actual (desmarcado: añade las nuevas clases manteniendo las existentes)
        </label>
      </div>

      {/* Botones de acción */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
        <button
          type="button"
          className="uml-toolbar-btn"
          onClick={onCancel}
          disabled={isLoading}
          style={{ padding: '8px 16px', fontSize: '14px' }}
        >
          <RefreshCw size={14} style={{ marginRight: '6px' }} />
          Subir Otra Imagen
        </button>

        <button
          type="button"
          className="uml-toolbar-btn primary"
          onClick={onApply}
          disabled={isLoading || previewUml.clases.length === 0}
          style={{
            padding: '8px 20px',
            fontSize: '14px',
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: '#10b981',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            cursor: isLoading ? 'not-allowed' : 'pointer',
          }}
        >
          <CheckCircle2 size={16} />
          {isLoading ? 'Aplicando...' : 'Aplicar al Diagrama UML'}
        </button>
      </div>
    </div>
  );
};
