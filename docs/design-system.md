# SHEVAULT — MASTER DESIGN SYSTEM SPECIFICATION

## 1. Architectural Philosophy
The SheVault Design System is safety-critical, accessible, and completely customizable. Every safety state adheres to rigorous accessibility rules to prevent misinterpretation during high-stress situations.

### The Golden Rule
> **Never write `Color(0xFFC92A32)` anywhere in application code.**
> Always reference `SheVaultColors.emergency` (or `SheVaultTheme.colors.emergency`).
> This guarantees that the entire emergency palette is universally swappable and securely masked during discreet decoy operations.

### The Safety Invariant Rule
> **Never communicate safety state with color alone.**
> Every safety indicator MUST combine: `Color + Icon + Text`.
> High-stress visibility demands unmistakable multi-modal confirmation.

---

## 2. Master Palette Tokens

### 1. Brand Palette
*Rule: Plum = SheVault. Rose and peach are supporting accents, not replacements for plum.*

| Token | Hex | Role |
|---|---|---|
| `brand.primary` | **#6D2E5B** | Main brand, primary actions, active navigation |
| `brand.primaryDark` | **#4A1F3D** | Deep brand surfaces, pressed states, headers |
| `brand.primaryTint` | **#F1E5EF** | Selected cards, soft brand backgrounds |
| `brand.rose` | **#C85C7B** | Accent, illustrations, large visual elements |
| `brand.peach` | **#F4B6A6** | Warm decorative highlight |

### 2. Light Theme Neutrals
*Rule: Secondary/helper text is strictly `#5C5459` rather than `#70686D`.*

| Token | Hex | Purpose |
|---|---|---|
| `light.background` | **#FFF9F7** | Application canvas background |
| `light.surface` | **#FFFFFF** | Cards, sheets, dialogs |
| `light.surfaceElevated` | **#FFFFFF** | Elevated card surfaces |
| `light.textPrimary` | **#211D20** | High-contrast main text |
| `light.textSecondary` | **#5C5459** | Secondary/helper text |
| `light.textDisabled` | **#9D959A** | Inactive/disabled text |
| `light.border` | **#E7DEE3** | Structural borders |
| `light.divider` | **#EEE7EB** | Soft card and list separators |

### 3. Dark Theme Neutrals

| Token | Hex | Purpose |
|---|---|---|
| `dark.background` | **#151116** | Main background canvas |
| `dark.surface` | **#211A21** | Cards and containers |
| `dark.surfaceElevated` | **#2B222A** | Dialogs, sheets, elevated layers |
| `dark.textPrimary` | **#F8F2F5** | High-contrast text |
| `dark.textSecondary` | **#C8BDC5** | Secondary text |
| `dark.textDisabled` | **#756A73** | Inactive/disabled text |
| `dark.border` | **#3B3038** | Structural borders |
| `dark.divider` | **#332933** | Soft dividers |

### 4. Safety Semantic Palette

| Semantic Domain | Token | Hex / RGBA | Invariant & Usage Rules |
|---|---|---|---|
| **Safe** | `safe.default` | **#0F766E** | Confirmation, protected status |
| | `safe.dark` | **#0A5B55** | Safe pressed / high-contrast border |
| | `safe.surface` | **#E6F5F2** | Safe light card background |
| | `safe.surfaceDark` | `rgba(15, 118, 110, 0.18)` | Safe dark card background |
| **Emergency** | `emergency.default` | **#C92A32** | SOS triggers, panic activation, active sessions. **Never decorative.** |
| | `emergency.dark` | **#A61B23** | Pressed state, emergency header |
| | `emergency.surface` | **#FDEBEC** | Emergency light card background |
| | `emergency.surfaceDark` | `rgba(201, 42, 50, 0.18)` | Emergency dark card background |
| **Warning** | `warning.default` | **#B54708** | Permission revoked, GPS degraded. **Never amber #D9911E.** |
| | `warning.dark` | **#8C3405** | Warning pressed state |
| | `warning.surface` | **#FFF4E5** | Warning light card background |
| | `warning.surfaceDark` | `rgba(181, 71, 8, 0.18)` | Warning dark card background |
| **Info** | `info.default` | **#3978B8** | Informational tips, telemetry fix indicators |
| | `info.dark` | **#2E6093** | Info pressed state |
| | `info.surface` | **#EAF2FB** | Info light container |
| | `info.surfaceDark` | `rgba(57, 120, 184, 0.18)` | Info dark container |

### 5. Focus Rings
- **Light Theme**: `#6D2E5B` (3px stroke, 2px offset)
- **Dark Theme**: `#C978A8` (3px stroke, 2px offset)

### 6. Discreet Calculator Decoy Palette
*Rule: Decoy must have zero SheVault branding, zero plum, zero emergency red.*

| Token | Hex | Function |
|---|---|---|
| `decoy.background` | **#101010** | Calculator body |
| `decoy.surface` | **#1C1C1C** | Operator button keys (`÷`, `×`, `−`, `+`, `=`) |
| `decoy.key` | **#2A2A2A** | Digit keys (`0` - `9`, `.`) |
| `decoy.keyPressed` | **#3A3A3A** | Active/pressed key feedback |
| `decoy.text` | **#FFFFFF** | High-contrast numbers |
| `decoy.textSecondary` | **#AAAAAA** | History and subtitle readout |
| `decoy.operator` | **#D0D0D0** | Operator symbol text |

### 7. Shadows, Scrims & Skeletons
- **Scrims**:
  - Light mode: `rgba(33, 29, 32, 0.32)`
  - Dark mode: `rgba(0, 0, 0, 0.48)`
- **Shadows**:
  - Soft: `rgba(74, 31, 61, 0.05)`
  - Card: `rgba(74, 31, 61, 0.08)`
  - Modal: `rgba(74, 31, 61, 0.12)`
- **Skeleton Shimmer**:
  - Light base: `#F3ECEF`, highlight: `#FAF6F8`
  - Dark base: `#2B222A`, highlight: `#3B3038`
- **Approved Gradients**:
  - Brand Gradient: `#6D2E5B` &rarr; `#C85C7B`
  - Warm Subtle Gradient: `#F1E5EF` &rarr; `#F4B6A6`
  - *Rule: Never use gradients on the SOS button or active emergency banner.*

### 8. Bottom Navigation
*Rule: SOS button is strictly excluded from bottom navigation.*

- **Light Mode**:
  - Inactive: `#706970`
  - Active: `#6D2E5B`
  - Active Background Pill: `#F1E5EF`
- **Dark Mode**:
  - Inactive: `#AFA3AC`
  - Active: `#C978A8`
  - Active Background Pill: `rgba(201, 120, 168, 0.14)`

---

## 3. Five Primary Safety States (`Color + Icon + Text`)

Every primary safety state is encoded with explicit visual triad invariants:

```
┌────────────────────────────────────────────────────────────────────────┐
│ State        │ Color                │ Icon              │ Text         │
├──────────────┼──────────────────────┼───────────────────┼──────────────┤
│ PROTECTED    │ Teal (#0F766E)       │ ShieldCheck       │ "You're      │
│              │                      │                   │ Protected"   │
│ LIMITED      │ Amber (#B54708)      │ TriangleAlert     │ "Protection  │
│              │                      │                   │ Limited"     │
│ ACTIVE       │ Red (#C92A32)        │ AlertOctagon      │ "Emergency   │
│              │                      │                   │ Active"      │
│ DISCREET     │ Neutral (#706970)    │ EyeOff            │ "Discreet    │
│              │                      │                   │ Active"      │
│ OFFLINE      │ Amber/Neutral        │ WifiOff           │ "Offline     │
│              │                      │                   │ Mode"        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Typography Ladder
- `sosCounter`: 64sp Black Monospace counter for emergency countdown visibility.
- `alertBanner`: 13sp Bold for persistent security status alerts.
- Standard Material 3 typography ladder (`displayLarge` down to `labelSmall`).

## 5. Spacing Grid (4dp Baseline)
`none` (0dp), `xxs` (2dp), `xs` (4dp), `sm` (8dp), `md` (12dp), `lg` (16dp), `xl` (24dp), `xxl` (32dp), `xxxl` (48dp), `huge` (64dp).

## 6. Icon Touch Targets
- Minimum accessible touch target: **48dp**.
- Scales: `micro` (12dp), `small` (16dp), `medium` (24dp), `large` (32dp), `extraLarge` (48dp), `sosHero` (72dp).

---

## 7. Developer Usage

### Kotlin Jetpack Compose
```kotlin
// In UI components:
val emergencyColor = SheVaultTheme.colors.emergency
val primaryColor = SheVaultTheme.colors.primary
val safeCard = SheVaultTheme.status.descriptorFor(SheVaultSafetyStatus.PROTECTED)

// Primary Safety Card:
SafetyStatusCard(status = SheVaultSafetyStatus.PROTECTED)
```

### React Web Dashboard
```typescript
import { SheVaultColors, BrandTokens, DarkNeutralTokens } from "./tokens";

// Guaranteed zero raw hex violation
const beaconColor = SheVaultColors.emergency;
const surfaceBg = DarkNeutralTokens.surface;
```
