import React from 'react';
import ReactFlow, {
  Background,
  BackgroundVariant,
  Controls,
  MiniMap,
  Node,
  Edge,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { useUMLStore } from '../store/umlStore';
import { ClassNode } from './ClassNode';

const nodeTypes = {
  classNode: ClassNode,
};

export const UMLCanvas: React.FC = () => {
  const {
    nodes,
    edges,
    onNodesChange,
    onEdgesChange,
    onConnect,
    selectClass,
    selectRelation,
    savePositionChanges,
    broadcastNodePosition,
  } = useUMLStore();

  const handleNodeClick = (_: React.MouseEvent, node: Node) => {
    selectClass(parseInt(node.id, 10));
  };

  const handleEdgeClick = (_: React.MouseEvent, edge: Edge) => {
    const relId = parseInt(edge.id.replace('rel-', ''), 10);
    selectRelation(relId);
  };

  const handlePaneClick = () => {
    selectClass(null);
    selectRelation(null);
  };

  const handleNodeDragStop = (_: React.MouseEvent, node: Node) => {
    // Auto-guarda cambios de posición y difunde a los demás colaboradores
    savePositionChanges();
    const claseId = parseInt(node.id, 10);
    broadcastNodePosition(claseId, node.position.x, node.position.y);
  };

  return (
    <div className="uml-canvas-container">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onNodeClick={handleNodeClick}
        onEdgeClick={handleEdgeClick}
        onPaneClick={handlePaneClick}
        onNodeDragStop={handleNodeDragStop}
        nodeTypes={nodeTypes}
        fitView
        snapToGrid
        snapGrid={[15, 15]}
        defaultEdgeOptions={{
          type: 'smoothstep',
        }}
      >
        <Background variant={BackgroundVariant.Dots} gap={20} size={1.5} color="#334155" />
        <Controls showInteractive={false} />
        <MiniMap
          nodeColor="#3b82f6"
          maskColor="rgba(15, 23, 42, 0.7)"
          style={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '6px' }}
        />
      </ReactFlow>
    </div>
  );
};
