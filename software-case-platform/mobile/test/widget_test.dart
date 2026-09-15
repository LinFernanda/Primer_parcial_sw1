import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/main.dart';

void main() {
  testWidgets('App basic smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const CasePlatformMobileApp());
    await tester.pump();
    expect(find.text('CASE Platform Mobile'), findsOneWidget);
  });
}
