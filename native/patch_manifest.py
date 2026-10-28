import re, sys
p = sys.argv[1]
s = open(p).read()
s = s.replace(
    '<category android:name="android.intent.category.LAUNCHER" />',
    '<category android:name="android.intent.category.LAUNCHER" />\n'
    '                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />\n'
    '                <category android:name="android.intent.category.HOME" />\n'
    '                <category android:name="android.intent.category.DEFAULT" />',
)
s = s.replace('<application', '<application android:banner="@mipmap/ic_launcher"', 1)
extra = (
    '    <uses-feature android:name="android.software.leanback" android:required="false" />\n'
    '    <uses-feature android:name="android.hardware.touchscreen" android:required="false" />\n'
    '    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />\n'
    '    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />\n'
)
s = re.sub(r'(<manifest[^>]*>)', r'\1\n' + extra, s, count=1)
service = (
    '        <service android:name=".RemapService" android:exported="false"\n'
    '            android:label="TV Home - Botao do controle"\n'
    '            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">\n'
    '            <intent-filter><action android:name="android.accessibilityservice.AccessibilityService" /></intent-filter>\n'
    '            <meta-data android:name="android.accessibilityservice" android:resource="@xml/remap_service" />\n'
    '        </service>\n'
    '        <service android:name=".RemoteService" android:exported="false" />\n'
)
s = s.replace('</application>', service + '    </application>', 1)
open(p, "w").write(s)
print(s)
