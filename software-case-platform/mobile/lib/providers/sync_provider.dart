import 'dart:async';
import 'package:flutter/material.dart';
import '../data/local/dao/sync_error_dao.dart';
import '../data/local/dao/sync_queue_dao.dart';
import '../data/local/entities/sync_error_entity.dart';
import '../data/local/entities/sync_operation_entity.dart';
import '../services/connectivity/connectivity_service.dart';
import '../services/sync/sync_service.dart';

class SyncProvider extends ChangeNotifier {
  final SyncService syncService;
  final SyncQueueDao syncQueueDao;
  final SyncErrorDao syncErrorDao;
  final ConnectivityService connectivityService;

  List<SyncOperationEntity> _pendingOperations = [];
  List<SyncErrorEntity> _activeErrors = [];
  SyncReport? _lastReport;
  StreamSubscription? _syncSub;
  StreamSubscription? _connSub;

  List<SyncOperationEntity> get pendingOperations => List.unmodifiable(_pendingOperations);
  List<SyncErrorEntity> get activeErrors => List.unmodifiable(_activeErrors);
  SyncReport? get lastReport => _lastReport;
  bool get isSyncing => syncService.isSyncing;
  bool get isOnline => connectivityService.isOnline;
  bool get isOffline => connectivityService.isOffline;
  ConnectivityStatus get connectivityStatus => connectivityService.currentStatus;
  int get pendingCount => _pendingOperations.length;
  int get errorCount => _activeErrors.length;

  SyncProvider({
    required this.syncService,
    required this.syncQueueDao,
    required this.syncErrorDao,
    required this.connectivityService,
  }) {
    refreshStatus();

    _syncSub = syncService.onSyncCompleted.listen((report) {
      _lastReport = report;
      refreshStatus();
    });

    _connSub = connectivityService.onStatusChange.listen((_) {
      notifyListeners();
    });
  }

  Future<void> refreshStatus() async {
    _pendingOperations = await syncQueueDao.getAllPending();
    _activeErrors = await syncErrorDao.getActiveErrors();
    notifyListeners();
  }

  Future<SyncReport> triggerSync() async {
    final report = await syncService.synchronize();
    _lastReport = report;
    await refreshStatus();
    return report;
  }

  Future<void> resolveError(String id) async {
    await syncErrorDao.resolveError(id);
    await refreshStatus();
  }

  void toggleOfflineMode(bool forceOffline) {
    connectivityService.setForcedStatus(
      forceOffline ? ConnectivityStatus.offline : ConnectivityStatus.online,
    );
    notifyListeners();
  }

  @override
  void dispose() {
    _syncSub?.cancel();
    _connSub?.cancel();
    super.dispose();
  }
}
