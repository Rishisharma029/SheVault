import os
import re
import pytest

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

def test_modular_directory_structure_exists():
    required_dirs = [
        "android/app",
        "android/core/design",
        "android/core/database",
        "android/core/network",
        "android/core/security",
        "android/core/permissions",
        "android/core/common",
        "android/feature/home",
        "android/feature/sos",
        "android/feature/incident",
        "android/feature/trustedcircle",
        "android/feature/disretmode",
        "android/feature/saferoute",
        "android/feature/history",
        "android/feature/settings",
        "android/feature/onboarding",
        "android/service",
        "android/service/IncidentService",
        "android/service/LocationService",
        "android/service/SensorService",
        "android/service/DeliveryService",
        "backend/app/api",
        "backend/app/models",
        "backend/app/schemas",
        "backend/app/services",
        "backend/app/repositories",
        "backend/app/workers",
        "backend/app/core",
        "backend/tests",
        "dashboard",
        "docs"
    ]

    for d in required_dirs:
        path = os.path.join(ROOT_DIR, d)
        assert os.path.exists(path), f"Required modular directory missing: {d}"

def test_design_system_tokens_exist():
    design_dir = os.path.join(ROOT_DIR, "android", "core", "design", "src", "main", "kotlin", "com", "shevault", "core", "design")
    token_files = [
        os.path.join(design_dir, "tokens", "ColorTokens.kt"),
        os.path.join(design_dir, "tokens", "TypographyTokens.kt"),
        os.path.join(design_dir, "tokens", "SpacingTokens.kt"),
        os.path.join(design_dir, "tokens", "RadiusTokens.kt"),
        os.path.join(design_dir, "tokens", "ElevationTokens.kt"),
        os.path.join(design_dir, "tokens", "MotionTokens.kt"),
        os.path.join(design_dir, "tokens", "IconRules.kt"),
        os.path.join(design_dir, "tokens", "StatusTokens.kt"),
        os.path.join(design_dir, "theme", "SheVaultTheme.kt")
    ]
    for tf in token_files:
        assert os.path.exists(tf), f"Required design system file missing: {tf}"

def test_critical_rule_no_hardcoded_emergency_color_in_application():
    """
    CRITICAL RULE TEST:
    'Never write: Color(0xFFC92A32) throughout the application. Instead: SheVaultColors.emergency'
    Scan all kotlin source files in android/.
    Only ColorTokens.kt is allowed to define Color(0xFFC92A32).
    Every other file MUST use SheVaultColors.emergency.
    """
    android_dir = os.path.join(ROOT_DIR, "android")
    pattern = re.compile(r"0x[cC]92[aA]32")
    violations = []

    for root, _, files in os.walk(android_dir):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                relpath = os.path.relpath(filepath, ROOT_DIR)
                # Skip the exact definition file where SheVaultColors.emergency is defined
                if file == "ColorTokens.kt" or file == "ThemeTokensTest.kt":
                    continue
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                    if pattern.search(content):
                        violations.append(relpath)

    assert len(violations) == 0, f"Found hardcoded emergency color 0xFFC92A32 violations in: {violations}. Must use SheVaultColors.emergency instead!"

def test_dashboard_uses_tokens_not_hardcoded_color():
    """
    In the React dashboard, '#C92A32' should only appear in tokens.ts.
    All components (App.tsx, etc.) must import and use SheVaultColors.emergency.
    """
    dashboard_src = os.path.join(ROOT_DIR, "dashboard", "src")
    pattern = re.compile(r"#C92A32", re.IGNORECASE)
    violations = []

    for root, _, files in os.walk(dashboard_src):
        for file in files:
            if file.endswith((".ts", ".tsx")):
                if file == "tokens.ts":
                    continue
                filepath = os.path.join(root, file)
                relpath = os.path.relpath(filepath, ROOT_DIR)
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.read()
                    if pattern.search(content):
                        violations.append(relpath)

    assert len(violations) == 0, f"Dashboard files contains hardcoded #C92A32: {violations}. Must use SheVaultColors.emergency from tokens.ts!"

def test_repositories_module_populated():
    """
    Verify backend/app/repositories contains UserRepository, TrustedContactRepository, etc.
    """
    repo_dir = os.path.join(ROOT_DIR, "backend", "app", "repositories")
    assert os.path.exists(repo_dir)
    files = [f for f in os.listdir(repo_dir) if f.endswith(".py")]
    expected = ["users.py", "contacts.py", "incidents.py", "locations.py", "deliveries.py", "checkins.py"]
    for exp in expected:
        assert exp in files, f"Missing repository file: {exp}"


def test_main_activity_is_not_the_entire_application():
    """
    Rule: 'Don't put the entire application in MainActivity.kt.'
    MainActivity should be a thin entrypoint (< 50 lines) that delegates to SheVaultNavHost.
    """
    main_activity_path = os.path.join(ROOT_DIR, "android", "app", "src", "main", "kotlin", "com", "shevault", "MainActivity.kt")
    assert os.path.exists(main_activity_path)
    with open(main_activity_path, "r", encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip() and not line.strip().startswith("//") and not line.strip().startswith("/*") and not line.strip().startswith("*")]
    # Thin activity check
    assert len(lines) < 35, f"MainActivity.kt is too bloated ({len(lines)} non-comment lines)! It must not contain the entire application."

def test_foreground_service_types_declared_in_manifest():
    """
    Modern Android foreground service type requirements:
    location, microphone, dataSync
    """
    incident_manifest = os.path.join(ROOT_DIR, "android", "service", "IncidentService", "src", "main", "AndroidManifest.xml")
    loc_manifest = os.path.join(ROOT_DIR, "android", "service", "LocationService", "src", "main", "AndroidManifest.xml")
    deliv_manifest = os.path.join(ROOT_DIR, "android", "service", "DeliveryService", "src", "main", "AndroidManifest.xml")

    assert os.path.exists(incident_manifest)
    assert os.path.exists(loc_manifest)
    assert os.path.exists(deliv_manifest)

    with open(incident_manifest, "r", encoding="utf-8") as f:
        inc_text = f.read()
    with open(loc_manifest, "r", encoding="utf-8") as f:
        loc_text = f.read()
    with open(deliv_manifest, "r", encoding="utf-8") as f:
        deliv_text = f.read()

    assert 'android:foregroundServiceType="location|microphone|dataSync"' in inc_text
    assert 'android:foregroundServiceType="location"' in loc_text
    assert 'android:foregroundServiceType="dataSync"' in deliv_text

