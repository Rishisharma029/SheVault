/**
 * SheVault Web Dashboard Design System Tokens.
 *
 * CRITICAL ARCHITECTURAL RULE:
 * Never write `#C92A32` directly in application styling.
 * Always import and use `SheVaultColors.emergency`!
 *
 * Locked Master Design Palette:
 * Plum = SheVault (#6D2E5B)
 */

export const BrandTokens = {
  primary: "#6D2E5B",      // Main brand, primary actions, active navigation
  primaryDark: "#4A1F3D",  // Deep brand surfaces, pressed states, headers
  primaryTint: "#F1E5EF",  // Selected cards, soft brand backgrounds
  rose: "#C85C7B",         // Accent, illustrations, large visual elements
  peach: "#F4B6A6",        // Warm decorative highlight
} as const;

export const LightNeutralTokens = {
  background: "#FFF9F7",      // Application background
  surface: "#FFFFFF",         // Cards, sheets, dialogs
  surfaceElevated: "#FFFFFF", // Elevated surfaces
  textPrimary: "#211D20",     // Main text
  textSecondary: "#5C5459",   // Secondary/helper text (strictly #5C5459 rather than #70686D)
  textDisabled: "#9D959A",    // Disabled text
  border: "#E7DEE3",          // Borders
  divider: "#EEE7EB",         // Soft separators
} as const;

export const DarkNeutralTokens = {
  background: "#151116",      // Main background
  surface: "#211A21",         // Cards
  surfaceElevated: "#2B222A", // Dialogs/sheets/elevated cards
  textPrimary: "#F8F2F5",     // Main text
  textSecondary: "#C8BDC5",   // Secondary text
  textDisabled: "#756A73",    // Disabled text
  border: "#3B3038",          // Borders
  divider: "#332933",         // Soft separators
} as const;

export const SafeTokens = {
  default: "#0F766E",
  dark: "#0A5B55",
  surface: "#E6F5F2",
  surfaceDark: "rgba(15, 118, 110, 0.18)",
} as const;

export const EmergencyTokens = {
  default: "#C92A32",
  dark: "#A61B23",
  surface: "#FDEBEC",
  surfaceDark: "rgba(201, 42, 50, 0.18)",
} as const;

export const WarningTokens = {
  default: "#B54708", // Never amber #D9911E
  dark: "#8C3405",
  surface: "#FFF4E5",
  surfaceDark: "rgba(181, 71, 8, 0.18)",
} as const;

export const InfoTokens = {
  default: "#3978B8",
  dark: "#2E6093",
  surface: "#EAF2FB",
  surfaceDark: "rgba(57, 120, 184, 0.18)",
} as const;

export const FocusRingTokens = {
  light: "#6D2E5B",
  dark: "#C978A8",
} as const;

export const DecoyCalculatorTokens = {
  background: "#101010",
  surface: "#1C1C1C",
  key: "#2A2A2A",
  keyPressed: "#3A3A3A",
  text: "#FFFFFF",
  textSecondary: "#AAAAAA",
  operator: "#D0D0D0",
} as const;

export const BottomNavTokens = {
  lightInactive: "#706970",
  lightActive: "#6D2E5B",
  lightActivePill: "#F1E5EF",
  darkInactive: "#AFA3AC",
  darkActive: "#C978A8",
  darkActivePill: "rgba(201, 120, 168, 0.14)",
} as const;

export const ButtonTokens = {
  primaryBackground: BrandTokens.primary,
  primaryText: "#FFFFFF",
  primaryPressed: BrandTokens.primaryDark,

  emergencyBackground: EmergencyTokens.default,
  emergencyText: "#FFFFFF",
  emergencyPressed: EmergencyTokens.dark,

  safeBackground: SafeTokens.default,
  safeText: "#FFFFFF",
  safePressed: SafeTokens.dark,

  secondaryBackground: "#FFFFFF",
  secondaryText: BrandTokens.primary,
  secondaryBorder: LightNeutralTokens.border,
  secondaryPressedBg: BrandTokens.primaryTint,

  darkSecondaryBackground: DarkNeutralTokens.surfaceElevated,
  darkSecondaryText: FocusRingTokens.dark,
  darkSecondaryBorder: DarkNeutralTokens.border,
} as const;

export const TextHierarchyTokens = {
  primaryHeading: LightNeutralTokens.textPrimary,
  body: LightNeutralTokens.textPrimary,
  secondary: LightNeutralTokens.textSecondary,
  disabled: LightNeutralTokens.textDisabled,
  emergency: EmergencyTokens.dark,
  safe: SafeTokens.dark,
  warning: WarningTokens.default,
  info: InfoTokens.dark,

  darkPrimary: DarkNeutralTokens.textPrimary,
  darkSecondary: DarkNeutralTokens.textSecondary,
  darkDisabled: DarkNeutralTokens.textDisabled,
} as const;

export const SelectionTokens = {
  lightBackground: BrandTokens.primaryTint,
  lightBorder: BrandTokens.primary,
  lightIcon: BrandTokens.primary,

  darkBackground: "rgba(201, 120, 168, 0.15)",
  darkBorder: FocusRingTokens.dark,
  darkIcon: FocusRingTokens.dark,
} as const;

export const InputFieldTokens = {
  normalBackground: LightNeutralTokens.surface,
  normalBorder: LightNeutralTokens.border,
  normalText: LightNeutralTokens.textPrimary,
  normalPlaceholder: LightNeutralTokens.textSecondary,

  focusBorder: BrandTokens.primary,

  errorBorder: EmergencyTokens.default,
  errorBackground: EmergencyTokens.surface,

  successBorder: SafeTokens.default,
  successBackground: SafeTokens.surface,
} as const;

export const CardTokens = {
  normalSurface: LightNeutralTokens.surface,
  normalBorder: LightNeutralTokens.border,
  normalShadow: "rgba(74, 31, 61, 0.08)",

  brandBackground: BrandTokens.primaryTint,
  brandBorder: "rgba(109, 46, 91, 0.12)",

  safeBackground: SafeTokens.surface,
  safeBorder: "rgba(15, 118, 110, 0.15)",

  warningBackground: WarningTokens.surface,
  warningBorder: "rgba(181, 71, 8, 0.15)",

  emergencyBackground: EmergencyTokens.surface,
  emergencyBorder: "rgba(201, 42, 50, 0.15)",
} as const;

export const SosVisualTokens = {
  idleColor: EmergencyTokens.default,
  idleRing: "rgba(201, 42, 50, 0.12)",
  idleText: LightNeutralTokens.textPrimary,

  holdingColor: EmergencyTokens.default,
  holdingRing: EmergencyTokens.dark,
  holdingBg: EmergencyTokens.surface,

  activePrimary: EmergencyTokens.default,
  activeCritical: EmergencyTokens.dark,
  activeSurface: EmergencyTokens.surface,
} as const;

export const IncidentTimelineTokens = {
  started: BrandTokens.primary,
  location: InfoTokens.default,
  contact: SafeTokens.default,
  networkLost: WarningTokens.default,
  escalation: EmergencyTokens.default,
  ended: SafeTokens.default,
} as const;

export const ConnectivityTokens = {
  connected: SafeTokens.default,
  degraded: WarningTokens.default,
  offline: LightNeutralTokens.textSecondary,
  restoring: InfoTokens.default,
} as const;

export const BatteryTokens = {
  safe: SafeTokens.default,
  neutral: LightNeutralTokens.textSecondary,
  warning: WarningTokens.default,
  warningCritical: WarningTokens.default,
  emergency: EmergencyTokens.default,
} as const;

export const LocationStatusTokens = {
  excellent: SafeTokens.default,
  approximate: InfoTokens.default,
  degraded: WarningTokens.default,
  unavailable: EmergencyTokens.default,
} as const;

export const MapTokens = {
  markerOuterRing: EmergencyTokens.surface,
  markerPin: EmergencyTokens.default,
  markerCenter: "#FFFFFF",

  routeNormal: BrandTokens.primary,
  routeRecommended: SafeTokens.default,
  routeAlternative: InfoTokens.default,
  routeUncertain: WarningTokens.default,

  accuracyCircle: "rgba(201, 42, 50, 0.12)",
} as const;

export const TrustedCircleTokens = {
  activeContact: SafeTokens.default,
  activeText: LightNeutralTokens.textPrimary,
  pending: WarningTokens.default,
  failed: EmergencyTokens.default,
  disabled: LightNeutralTokens.textDisabled,
} as const;

export const CheckInTokens = {
  active: BrandTokens.primary,
  completed: SafeTokens.default,
  overdue: WarningTokens.default,
  escalated: EmergencyTokens.default,
} as const;

export const HistoryTokens = {
  completed: SafeTokens.default,
  cancelled: LightNeutralTokens.textSecondary,
  escalated: EmergencyTokens.default,
  unknown: LightNeutralTokens.textDisabled,
} as const;

export const OverlayTokens = {
  scrimLight: "rgba(33, 29, 32, 0.32)",
  scrimDark: "rgba(0, 0, 0, 0.48)",
  surfaceGlass: "rgba(255, 255, 255, 0.72)",
  surfaceDarkGlass: "rgba(43, 34, 42, 0.80)",
} as const;

export const SkeletonTokens = {
  lightBase: "#F1ECEF",
  lightHighlight: "#F8F4F6",
  darkBase: "#2B222A",
  darkHighlight: "#342B33",
} as const;

export const ChartTokens = {
  primary: BrandTokens.primary,
  secondary: InfoTokens.default,
  safe: SafeTokens.default,
  warning: WarningTokens.default,
  emergency: EmergencyTokens.default,
} as const;

export const GradientTokens = {
  brandStart: BrandTokens.primary,
  brandEnd: BrandTokens.rose,
  warmStart: BrandTokens.primaryTint,
  warmEnd: BrandTokens.peach,
} as const;

export const SheVaultColors = {
  emergency: EmergencyTokens.default,
  emergencyDark: EmergencyTokens.dark,
  primary: BrandTokens.primary,
  primaryDark: BrandTokens.primaryDark,
  primaryTint: BrandTokens.primaryTint,
  rose: BrandTokens.rose,
  peach: BrandTokens.peach,
  secondary: DarkNeutralTokens.textSecondary,
  safe: SafeTokens.default,
  safeDark: SafeTokens.dark,
  warning: WarningTokens.default,
  info: InfoTokens.default,
  background: DarkNeutralTokens.background,
  surface: DarkNeutralTokens.surface,
  surfaceElevated: DarkNeutralTokens.surfaceElevated,
  border: DarkNeutralTokens.border,
  divider: DarkNeutralTokens.divider,
  textPrimary: DarkNeutralTokens.textPrimary,
  textSecondary: DarkNeutralTokens.textSecondary,
  textMuted: DarkNeutralTokens.textDisabled,
  white: "#FFFFFF",
} as const;

export const PaletteTokens = {
  Emergency900: EmergencyTokens.dark,
  Emergency800: "#690005",
  Emergency700: "#93000A",
  Emergency600: SheVaultColors.emergency,
  Emergency500: "#DE3730",
  Emergency100: EmergencyTokens.surface,
  Emergency50: "#FFEDE8",
} as const;
