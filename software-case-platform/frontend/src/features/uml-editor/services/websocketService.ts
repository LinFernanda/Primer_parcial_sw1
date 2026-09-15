import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UMLEvent, UsuarioConectado, BloqueoElemento } from '../models/uml.types';

export class UMLWebSocketService {
  private client: Client | null = null;
  private currentModeloId: number | null = null;
  private isConnected: boolean = false;

  public connect(
    modeloId: number,
    usuarioEmail: string,
    nombre: string,
    onEventReceived: (event: UMLEvent) => void,
    onPresenceReceived: (usuarios: UsuarioConectado[]) => void,
    onLocksReceived: (bloqueos: BloqueoElemento[]) => void,
    onStatusChange?: (connected: boolean) => void
  ) {
    if (this.client && this.isConnected && this.currentModeloId === modeloId) {
      return;
    }

    if (this.client) {
      this.disconnect();
    }

    this.currentModeloId = modeloId;
    const token = localStorage.getItem('token');

    // Construye URL absoluta o relativa según el entorno
    const wsUrl = window.location.origin.includes('5173')
      ? 'http://localhost:8080/ws'
      : `${window.location.origin}/ws`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : '',
        token: token || '',
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (msg: string) => {
        if (import.meta.env.DEV) {
          console.debug('[STOMP]', msg);
        }
      },
      onConnect: () => {
        this.isConnected = true;
        if (onStatusChange) onStatusChange(true);

        // Suscripción al canal colaborativo del modelo actual
        this.client?.subscribe(`/topic/modelo/${modeloId}`, (message: IMessage) => {
          try {
            const event: UMLEvent = JSON.parse(message.body);

            if (event.tipoOperacion === 'PRESENCE_SYNC' && event.datosCambio?.usuarios) {
              onPresenceReceived(event.datosCambio.usuarios as UsuarioConectado[]);
            } else if (event.tipoOperacion === 'LOCKS_SYNC' && event.datosCambio?.bloqueos) {
              onLocksReceived(event.datosCambio.bloqueos as BloqueoElemento[]);
            } else {
              onEventReceived(event);
            }
          } catch (err) {
            console.error('Error procesando evento WebSocket STOMP:', err);
          }
        });

        // Enviar evento de ingreso al modelo
        this.sendJoin(modeloId, usuarioEmail, nombre);
      },
      onDisconnect: () => {
        this.isConnected = false;
        if (onStatusChange) onStatusChange(false);
      },
      onStompError: (frame) => {
        console.error('Error STOMP:', frame.headers['message'], frame.body);
      },
    });

    this.client.activate();
  }

  public disconnect() {
    if (this.client && this.currentModeloId) {
      this.sendLeave(this.currentModeloId);
      this.client.deactivate();
      this.client = null;
      this.currentModeloId = null;
      this.isConnected = false;
    }
  }

  public sendJoin(modeloId: number, usuario: string, nombre?: string) {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: `/app/modelo/${modeloId}/join`,
      body: JSON.stringify({ usuario, nombre }),
    });
  }

  public sendLeave(modeloId: number) {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: `/app/modelo/${modeloId}/leave`,
      body: JSON.stringify({}),
    });
  }

  public sendEvent(modeloId: number, event: Partial<UMLEvent>) {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: `/app/modelo/${modeloId}/event`,
      body: JSON.stringify(event),
    });
  }

  public lockElement(modeloId: number, elementoId: string, elementoTipo: string = 'CLASE') {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: `/app/modelo/${modeloId}/lock`,
      body: JSON.stringify({ elementoId, elementoTipo }),
    });
  }

  public unlockElement(modeloId: number, elementoId: string) {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: `/app/modelo/${modeloId}/unlock`,
      body: JSON.stringify({ elementoId }),
    });
  }

  public getConnected(): boolean {
    return this.isConnected;
  }
}

export const websocketService = new UMLWebSocketService();
