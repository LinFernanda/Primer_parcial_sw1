import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ClassNode } from '../components/ClassNode';
import { ClaseUML } from '../models/uml.types';
import { ReactFlowProvider } from 'reactflow';

describe('ClassNode Component', () => {
  const mockClase: ClaseUML = {
    id: 1,
    nombre: 'Cliente',
    visibilidad: 'PUBLIC',
    posicionX: 100,
    posicionY: 100,
    atributos: [
      { id: 10, nombre: 'email', tipoDato: 'String', visibilidad: 'PRIVATE' },
      { id: 11, nombre: 'edad', tipoDato: 'Integer', visibilidad: 'PUBLIC' },
    ],
    metodos: [
      { id: 20, nombre: 'comprar', tipoRetorno: 'void', visibilidad: 'PUBLIC', parametros: 'item:String' },
    ],
  };

  it('debe renderizar el nombre de la clase y estereotipo', () => {
    render(
      <ReactFlowProvider>
        <ClassNode
          id="1"
          data={{ clase: mockClase }}
          selected={false}
          type="classNode"
          zIndex={1}
          isConnectable={true}
          xPos={100}
          yPos={100}
          dragging={false}
        />
      </ReactFlowProvider>
    );

    expect(screen.getByText('Cliente')).toBeInTheDocument();
    expect(screen.getByText('«class»')).toBeInTheDocument();
  });

  it('debe renderizar los atributos con sus visibilidades y tipos de dato', () => {
    render(
      <ReactFlowProvider>
        <ClassNode
          id="1"
          data={{ clase: mockClase }}
          selected={false}
          type="classNode"
          zIndex={1}
          isConnectable={true}
          xPos={100}
          yPos={100}
          dragging={false}
        />
      </ReactFlowProvider>
    );

    expect(screen.getByText('email')).toBeInTheDocument();
    expect(screen.getByText('String')).toBeInTheDocument();
    expect(screen.getByText('edad')).toBeInTheDocument();
    expect(screen.getByText('Integer')).toBeInTheDocument();
  });

  it('debe renderizar los métodos de la clase', () => {
    render(
      <ReactFlowProvider>
        <ClassNode
          id="1"
          data={{ clase: mockClase }}
          selected={false}
          type="classNode"
          zIndex={1}
          isConnectable={true}
          xPos={100}
          yPos={100}
          dragging={false}
        />
      </ReactFlowProvider>
    );

    expect(screen.getByText('comprar')).toBeInTheDocument();
    expect(screen.getByText('void')).toBeInTheDocument();
  });
});
