# SheVault Design System Specification

## 1. Architectural Philosophy
The SheVault Design System is built to be modular, accessible, and completely replaceable.

### The Golden Rule
> **Never write `Color(0xFFC92A32)` anywhere in application code.**
> Always reference `SheVaultColors.emergency` (or `SheVaultTheme.colors.emergency`).
> This guarantees that the entire brand emergency palette can be re-themed or adapted for discrete decoy modes without refactoring individual UI components.

---

## 2. Token Registry

### ColorTokens
- `SheVaultColors.emergency`: `Color(0xFFC92A32)` - High visibility emergency crimson.
- `PaletteTokens.Primary`: Deep empowering royal amethyst (`0xFF7C3AED` - `0xFFEDE9FE`).
- `PaletteTokens.Secondary`: Calm and reassuring teal (`0xFF0D9488` - `0xFFCCFBF1`).
- `PaletteTokens.Safe`: Emerald confirmation (`0xFF10B981`).
- `PaletteTokens.Warning`: Caution amber (`0xFFF59E0B`).
- `PaletteTokens.Slate`: Neutral background and surface hierarchy.
- `Decoy Mode`: Neutral utility tones (calculator gray) to hide emergency indicators from hostile onlookers.

### TypographyTokens
- `sosCounter`: 64sp Black Monospace counter for emergency countdown visibility in high-stress situations.
- `alertBanner`: 13sp Bold for persistent security status alerts.
- Standard Material 3 typography ladder (`displayLarge` down to `labelSmall`).

### SpacingTokens
- 4dp Baseline Grid: `none` (0dp), `xxs` (2dp), `xs` (4dp), `sm` (8dp), `md` (12dp), `lg` (16dp), `xl` (24dp), `xxl` (32dp), `xxxl` (48dp), `huge` (64dp).

### RadiusTokens
- `xs` (4dp), `sm` (8dp), `md` (12dp), `lg` (16dp), `xl` (24dp), `pill` (50dp), `circle` (9999dp).

### ElevationTokens
- `level0` (0dp) to `level5` (12dp), and `emergencySos` (16dp) for critical floating action elements.

### MotionTokens
- `durationFastMs` (150ms), `durationMediumMs` (300ms), `durationSlowMs` (500ms).
- `durationSosPulseMs` (1000ms): Continuous rhythmic heartbeat pulse for active SOS beacon.

### IconRules
- Size scale: `micro` (12dp), `small` (16dp), `medium` (24dp), `large` (32dp), `extraLarge` (48dp), `sosHero` (72dp).
- Minimum accessible touch target: 48dp.

### StatusTokens
- Semantic color mapping for `SafetyState`:
  - `SAFE`
  - `ACTIVE_MONITORING`
  - `CAUTION_ALERT`
  - `EMERGENCY_SOS` (bound to `SheVaultColors.emergency`)
  - `DISCRETE_DECOY`
  - `OFFLINE_STANDALONE`

---

## 3. Theme Composition Local Provider
Access all tokens seamlessly anywhere in the UI hierarchy:
```kotlin
val emergencyColor = SheVaultTheme.colors.emergency
val spacing = SheVaultTheme.spacing.md
val sosStyle = SheVaultTheme.typography.sosCounter
```
