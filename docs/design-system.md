# SHEVAULT — MASTER DESIGN SYSTEM SPECIFICATION

## 1. Brand Palette
*Rule: Plum = SheVault (#6D2E5B). Rose and peach are supporting accents, not replacements for plum.*

| Token | HEX | Purpose |
|---|---|---|
| `brand.primary` | **#6D2E5B** | Main brand, primary actions, active navigation |
| `brand.primaryDark` | **#4A1F3D** | Deep brand surfaces, pressed states, headers |
| `brand.primaryTint` | **#F1E5EF** | Selected cards, soft brand backgrounds |
| `brand.rose` | **#C85C7B** | Accent, illustrations, large visual elements |
| `brand.peach` | **#F4B6A6** | Warm decorative highlight |

---

## 2. Light Theme Neutrals
*Rule: Secondary/helper text is strictly `#5C5459` rather than `#70686D`.*

| Token | HEX | Purpose |
|---|---|---|
| `light.background` | **#FFF9F7** | Application background |
| `light.surface` | **#FFFFFF** | Cards, sheets, dialogs |
| `light.surfaceElevated` | **#FFFFFF** | Elevated surfaces |
| `light.textPrimary` | **#211D20** | Main text |
| `light.textSecondary` | **#5C5459** | Secondary/helper text |
| `light.textDisabled` | **#9D959A** | Disabled text |
| `light.border` | **#E7DEE3** | Borders/dividers |
| `light.divider` | **#EEE7EB** | Soft separators |

---

## 3. Dark Theme Neutrals

| Token | HEX | Purpose |
|---|---|---|
| `dark.background` | **#151116** | Main background |
| `dark.surface` | **#211A21** | Cards |
| `dark.surfaceElevated` | **#2B222A** | Dialogs/sheets/elevated cards |
| `dark.textPrimary` | **#F8F2F5** | Main text |
| `dark.textSecondary` | **#C8BDC5** | Secondary text |
| `dark.textDisabled` | **#756A73** | Disabled text |
| `dark.border` | **#3B3038** | Borders |
| `dark.divider` | **#332933** | Dividers |

---

## 4. Safety Semantic Palette: 🟢 Protected / Safe

| Token | HEX / RGBA | Invariant & Usage Rules |
|---|---|---|
| `safe.DEFAULT` | **#0F766E** | Safe confirmation (`You're Protected`, `Completed`, `Delivered`, `Verified`) |
| `safe.dark` | **#0A5B55** | Safe pressed / high-contrast border |
| `safe.surface` | **#E6F5F2** | Safe light card background |
| `safe.surfaceDark` | **rgba(15,118,110,0.18)** | Safe dark card background |

*Always pair with a shield/check icon and text.*

---

## 5. Safety Semantic Palette: 🚨 Emergency
*Rule: Never use emergency red as a decorative brand color.*

| Token | HEX / RGBA | Invariant & Usage Rules |
|---|---|---|
| `emergency.DEFAULT` | **#C92A32** | Critical SOS, active incident, panic triggers, critical failure |
| `emergency.dark` | **#A61B23** | Pressed state, emergency header |
| `emergency.surface` | **#FDEBEC** | Emergency light card background |
| `emergency.surfaceDark` | **rgba(201,42,50,0.18)** | Emergency dark card background |

---

## 6. Safety Semantic Palette: ⚠️ Warning
*Rule: Never use legacy amber #D9911E for warning text.*

| Token | HEX / RGBA | Invariant & Usage Rules |
|---|---|---|
| `warning.DEFAULT` | **#B54708** | Limited protection, low battery, location degraded, check-in overdue |
| `warning.surface` | **#FFF4E5** | Warning light card background |
| `warning.surfaceDark` | **rgba(181,71,8,0.18)** | Warning dark card background |

---

## 7. Safety Semantic Palette: 🔵 Information

| Token | HEX / RGBA | Invariant & Usage Rules |
|---|---|---|
| `info.DEFAULT` | **#3978B8** | Information, tips, setup guidance, system explanations |
| `info.dark` | **#2E6093** | Info pressed state |
| `info.surface` | **#EAF2FB** | Info light container |
| `info.surfaceDark` | **rgba(57,120,184,0.18)** | Info dark container |

---

## 8. State System: Critical Invariant
> **Never communicate safety state with color alone.**
> Every critical state requires: **Color + Icon + Text**.

| State | Color | Icon | Label |
|---|---|---|---|
| Protected | `#0F766E` | ShieldCheck | "You're Protected" |
| Active emergency | `#C92A32` | AlertOctagon | "Emergency Session Active" |
| Warning/limited | `#B54708` | TriangleAlert | "Protection Partially Limited" |
| Information | `#3978B8` | Info | "Information" |
| Disabled | Neutral gray (`#9D959A`) | CircleSlash | "Disabled" |

---

## 9. Button Palette

### Primary Button
- Background: `#6D2E5B`
- Text: `#FFFFFF`
- Pressed: `#4A1F3D`

### Emergency Button
- Background: `#C92A32`
- Text: `#FFFFFF`
- Pressed: `#A61B23`

### Safe Button
- Background: `#0F766E`
- Text: `#FFFFFF`
- Pressed: `#0A5B55`

### Secondary Button (Light)
- Background: `#FFFFFF`
- Text: `#6D2E5B`
- Border: `#E7DEE3`
- Pressed BG: `#F1E5EF`

### Dark Secondary Button
- Background: `#2B222A`
- Text: `#C978A8`
- Border: `#3B3038`

---

## 10. Text Colors by Hierarchy
- Primary Heading: `#211D20`
- Body: `#211D20`
- Secondary / Helper: `#5C5459`
- Disabled: `#9D959A`
- Emergency Text: `#A61B23`
- Safe Text: `#0A5B55`
- Warning Text: `#B54708`
- Info Text: `#2E6093`
- Dark Primary: `#F8F2F5`
- Dark Secondary: `#C8BDC5`

---

## 11. Focus & Accessibility
- **Light**: Focus ring `#6D2E5B` (width 3px, offset 2px)
- **Dark**: Focus ring `#C978A8` (width 3px, offset 2px)

---

## 12. Selection Colors
- **Light**: Selected BG `#F1E5EF`, Selected Border `#6D2E5B`, Selected Icon `#6D2E5B`
- **Dark**: Selected BG `rgba(201,120,168,0.15)`, Selected Border `#C978A8`, Selected Icon `#C978A8`

---

## 13. Input Fields
- **Normal**: Background `#FFFFFF`, Border `#E7DEE3`, Text `#211D20`, Placeholder `#5C5459`
- **Focus**: Border `#6D2E5B`
- **Error**: Border `#C92A32`, Background `#FDEBEC`
- **Success**: Border `#0F766E`, Background `#E6F5F2`

---

## 14. Cards
- **Normal**: Surface `#FFFFFF`, Border `#E7DEE3`, Shadow `rgba(74,31,61,0.08)`
- **Brand Card**: Background `#F1E5EF`, Border `rgba(109,46,91,0.12)`
- **Safe Card**: Background `#E6F5F2`, Border `rgba(15,118,110,0.15)`
- **Warning Card**: Background `#FFF4E5`, Border `rgba(181,71,8,0.15)`
- **Emergency Card**: Background `#FDEBEC`, Border `rgba(201,42,50,0.15)`

---

## 15. SOS Visual Language
*Rule: Do not flood the entire screen red.*

- **Idle**: SOS `#C92A32`, Ring `rgba(201,42,50,0.12)`, Text `#211D20`
- **Holding**: SOS `#C92A32`, Progress Ring `#A61B23`, Background `#FDEBEC`
- **Active**: Primary Emergency `#C92A32`, Critical `#A61B23`, Surface `#FDEBEC`

---

## 16. Incident Timeline
- Incident Started: Plum `#6D2E5B`
- Location Captured: Blue `#3978B8`
- Contact Notified: Teal `#0F766E`
- Network Lost: Amber `#B54708`
- Escalation: Red `#C92A32`
- Session Ended: Teal `#0F766E`

---

## 17. Connectivity Palette
- Connected: `#0F766E` (`● Connected`)
- Degraded: `#B54708` (`△ Limited connection`)
- Offline: `#5C5459` (`○ Offline`)
- Restoring: `#3978B8` (`↻ Reconnecting`)

---

## 18. Battery Palette
*Display exact percentage (e.g., "34%") rather than subjective labels.*

- 80–100%: Safe `#0F766E`
- 40–79%: Neutral `#5C5459`
- 20–39%: Warning `#B54708`
- 10–19%: Warning / Critical Emphasis `#B54708`
- <10%: Emergency / Critical `#C92A32`

---

## 19. Location Status
- Excellent: `#0F766E` (`✓ Location updated 4 sec ago`)
- Approximate: `#3978B8` (`△ Location accuracy reduced`)
- Degraded: `#B54708`
- Unavailable: `#C92A32` (`! Location unavailable`)

---

## 20. Map Palette
- **SheVault Location Marker**: Outer Ring `#FDEBEC`, Marker `#C92A32`, Center `#FFFFFF`
- **Route Colors**: Normal `#6D2E5B`, Recommended `#0F766E`, Alternative `#3978B8`, Uncertain/Degraded `#B54708`
- **Accuracy Circle**: `rgba(201,42,50,0.12)` (never opaque)

---

## 21. Trusted Circle Palette
- Active Contact: Icon `#0F766E`, Text `#211D20`
- Notification Pending: Icon `#B54708`
- Notification Failed: Icon `#C92A32`
- Contact Disabled: Icon `#9D959A`

---

## 22. Check-In Palette
- Active: Plum `#6D2E5B`
- Completed: Teal `#0F766E`
- Overdue: Amber `#B54708`
- Escalated: Red `#C92A32`

---

## 23. History Palette
*Keep history mostly neutral; use small semantic status indicators.*

- Completed: `#0F766E`
- Cancelled: `#5C5459`
- Escalated: `#C92A32`
- Unknown: `#9D959A`

---

## 24. Discreet Mode (Decoy Calculator)
*Rule: Strictly zero SheVault colors, zero plum, zero emergency red, zero SheVault branding.*

- Background: `#101010`
- Surface (Operators): `#1C1C1C`
- Key (Digits): `#2A2A2A`
- Key Pressed: `#3A3A3A`
- Text: `#FFFFFF`
- Secondary: `#AAAAAA`
- Operator Text: `#D0D0D0`

---

## 25. Dialog Scrims
- Light Scrim: `rgba(33,29,32,0.32)`
- Dark Scrim: `rgba(0,0,0,0.48)`
- Emergency Dialog: Surface `#FFFFFF`, Header/Icon `#C92A32` (never red background)

---

## 26. Bottom Navigation
*Rule: SOS button is strictly excluded from bottom navigation.*

- **Light**: Inactive `#706970`, Active `#6D2E5B`, Active Background Pill `#F1E5EF`
- **Dark**: Inactive `#AFA3AC`, Active `#C978A8`, Active Background Pill `rgba(201,120,168,0.14)`

---

## 27. Shadows (Plum-Tinted)
- Small: `rgba(74,31,61,0.05)`
- Medium: `rgba(74,31,61,0.08)`
- Large: `rgba(74,31,61,0.12)`

---

## 28. Overlay & Glass Colors
- `scrim.light`: `rgba(33,29,32,0.32)`
- `scrim.dark`: `rgba(0,0,0,0.48)`
- `surface.glass`: `rgba(255,255,255,0.72)`
- `surface.darkGlass`: `rgba(43,34,42,0.80)`

---

## 29. Skeleton / Loading Palette
- **Light**: Base `#F1ECEF`, Highlight `#F8F4F6`
- **Dark**: Base `#2B222A`, Highlight `#342B33`

---

## 30. Charts & Analytics Palette
- Primary Series: `#6D2E5B`
- Secondary: `#3978B8`
- Safe: `#0F766E`
- Warning: `#B54708`
- Emergency: `#C92A32`

---

## 31. Illustrations
- Palette: Plum, Rose, Peach, Ivory, Teal.
- Style: Editorial + Minimal + Human.
- Avoid: Neon cyberpunk, excessive 3D gradients, pink cliché sparkles.

---

## 32. Approved Gradients
*Rule: Never use gradients on SOS.*

- **Brand Gradient**: `#6D2E5B` &rarr; `#C85C7B`
- **Soft Warm Gradient**: `#F1E5EF` &rarr; `#F4B6A6`

---

## 33. Typography Hierarchy
- Large Display & Body: `#211D20` (Dark: `#F8F2F5`)
- Secondary: `#5C5459` (Dark: `#C8BDC5`)
- Touch target accessibility minimum: **48dp**.

---

## 34. Special High-Priority Status Tokens
1. `PROTECTED`: Teal (`#0F766E`) + `ShieldCheck` + "You're Protected"
2. `LIMITED`: Amber (`#B54708`) + `TriangleAlert` + "Protection Partially Limited"
3. `ACTIVE`: Red (`#C92A32`) + `AlertOctagon` + "Emergency Session Active"
4. `DISCREET`: Neutral (`#706970`) + `EyeOff` + "Discreet Mode Active"
5. `OFFLINE`: Amber/Neutral + `WifiOff` + "Offline Mode"

---

## 35. Master Token Naming Convention
```text
brand.primary           #6D2E5B
brand.primaryDark       #4A1F3D
brand.primaryTint       #F1E5EF
brand.rose              #C85C7B
brand.peach             #F4B6A6

surface.background      #FFF9F7
surface.default         #FFFFFF
surface.elevated        #FFFFFF

text.primary            #211D20
text.secondary          #5C5459
text.disabled           #9D959A

border.default          #E7DEE3
border.subtle           #EEE7EB

safe.default            #0F766E
safe.surface            #E6F5F2

warning.default         #B54708
warning.surface         #FFF4E5

emergency.default       #C92A32
emergency.dark          #A61B23
emergency.surface       #FDEBEC

info.default            #3978B8
info.surface            #EAF2FB

focus.ring              #6D2E5B / #C978A8
overlay.scrim           rgba(33,29,32,0.32) / rgba(0,0,0,0.48)
```

---

## 36. Visual Emotional Progression
```text
                    SHEVAULT
                       ↓
                 DEEP PLUM
                       ↓
             WARM IVORY / WHITE
                       ↓
          TEAL = PROTECTED / SAFE
                       ↓
          AMBER = ATTENTION / LIMITED
                       ↓
        RED = REAL EMERGENCY ONLY
```

**Calm &rarr; Trust &rarr; Attention &rarr; Emergency**
