import 'dart:async';
import 'dart:io';

enum ConnectivityStatus { online, offline, checking }

class ConnectivityService {
  ConnectivityStatus _currentStatus = ConnectivityStatus.online;
  ConnectivityStatus? _forcedStatus;
  final _controller = StreamController<ConnectivityStatus>.broadcast();
  Timer? _pollingTimer;
  String? _pingHost;
  int _pingPort = 8080;

  ConnectivityStatus get currentStatus => _forcedStatus ?? _currentStatus;
  bool get isOnline => currentStatus == ConnectivityStatus.online;
  bool get isOffline => currentStatus == ConnectivityStatus.offline;
  Stream<ConnectivityStatus> get onStatusChange => _controller.stream;

  ConnectivityService({
    String? pingHost,
    int pingPort = 8080,
    bool startPolling = true,
  })  : _pingHost = pingHost,
        _pingPort = pingPort {
    if (startPolling) {
      startHeartbeat(interval: const Duration(seconds: 15));
    }
  }

  void setForcedStatus(ConnectivityStatus? status) {
    _forcedStatus = status;
    _controller.add(currentStatus);
  }

  void updateHost(String host, {int port = 8080}) {
    _pingHost = host;
    _pingPort = port;
  }

  Future<ConnectivityStatus> checkConnection() async {
    if (_forcedStatus != null) return _forcedStatus!;

    try {
      final host = _pingHost ?? '127.0.0.1';
      final socket = await Socket.connect(
        host,
        _pingPort,
        timeout: const Duration(milliseconds: 1500),
      );
      socket.destroy();
      _setStatus(ConnectivityStatus.online);
    } catch (_) {
      // Fallback: Si no hay socket al host específico, verificar conectividad DNS básica
      try {
        final result = await InternetAddress.lookup('google.com')
            .timeout(const Duration(milliseconds: 1500));
        if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
          _setStatus(ConnectivityStatus.online);
        } else {
          _setStatus(ConnectivityStatus.offline);
        }
      } catch (_) {
        _setStatus(ConnectivityStatus.offline);
      }
    }
    return currentStatus;
  }

  void _setStatus(ConnectivityStatus newStatus) {
    if (_currentStatus != newStatus) {
      _currentStatus = newStatus;
      if (_forcedStatus == null) {
        _controller.add(_currentStatus);
      }
    }
  }

  void startHeartbeat({Duration interval = const Duration(seconds: 15)}) {
    _pollingTimer?.cancel();
    _pollingTimer = Timer.periodic(interval, (_) => checkConnection());
  }

  void stopHeartbeat() {
    _pollingTimer?.cancel();
    _pollingTimer = null;
  }

  void dispose() {
    stopHeartbeat();
    _controller.close();
  }
}
