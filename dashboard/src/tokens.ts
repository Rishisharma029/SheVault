/**
 * SheVault Web Dashboard Design System Tokens.
 *
 * CRITICAL ARCHITECTURAL RULE:
 * Never write `#C92A32` directly in application styling.
 * Always import and use `SheVaultColors.emergency`!
 */

export const SheVaultColors = {
  /**
   * Finalized high-visibility emergency crimson for SOS triggers and critical alerts.
   */
  emergency: "#C92A32",
  primary: "#7C3AED",
  primaryDark: "#2E0854",
  secondary: "#0D9488",
  safe: "#10B981",
  warning: "#F59E0B",
  background: "#0F172A",
  surface: "#1E293B",
  border: "#334155",
  textPrimary: "#F8FAFC",
  textSecondary: "#94A3B8",
  textMuted: "#64748B",
  white: "#FFFFFF",
};

export const PaletteTokens = {
  Emergency900: "#410002",
  Emergency800: "#690005",
  Emergency700: "#93000A",
  Emergency600: SheVaultColors.emergency, // #C92A32
  Emergency500: "#DE3730",
  Emergency100: "#FFDAD6",
  Emergency50: "#FFEDE8",
};
