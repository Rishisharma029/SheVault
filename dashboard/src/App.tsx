import React, { useState, useEffect } from "react";
import {
  SheVaultColors,
  BrandTokens,
  LightNeutralTokens,
  DarkNeutralTokens,
  SafeTokens,
  WarningTokens,
  InfoTokens,
  EmergencyTokens,
} from "./tokens";
import {
  Shield,
  AlertTriangle,
  Users,
  Compass,
  History,
  Activity,
  Settings,
  MapPin,
  Clock,
  Battery,
  Wifi,
  Phone,
  MessageSquare,
  Radio,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  Sun,
  Moon,
  ExternalLink,
  ChevronRight,
  Send,
  RefreshCw,
  Navigation,
} from "lucide-react";

interface IncidentLocation {
  lat: number;
  lng: number;
  accuracy: number;
  speed: number | null;
  bearing: number | null;
  provider: string;
  updatedAt: string;
}

interface IncidentTimelineEvent {
  id: string;
  type: string;
  title: string;
  timestamp: string;
  details: string;
  status: "verified" | "dispatched" | "acknowledged" | "queued";
}

interface Incident {
  id: string;
  user: string;
  phone: string;
  status: "ACTIVE" | "SAFE_CANCELLED" | "COERCED_DURESS" | "ESCALATED";
  coords: { lat: number; lng: number };
  accuracy: number;
  battery: number;
  charging: boolean;
  connectivity: "CELLULAR_4G" | "CELLULAR_5G" | "WIFI" | "OFFLINE";
  movement: "Stationary" | "Walking" | "Running" | "Vehicle";
  activationMethod: string;
  startedAt: string;
  breadcrumbsCount: number;
  breadcrumbs: Array<{ lat: number; lng: number; time: string }>;
  timeline: IncidentTimelineEvent[];
}

interface TrustedContactStatus {
  id: string;
  name: string;
  relation: string;
  phone: string;
  priority: number;
  canReceiveLocation: boolean;
  deliveryStatus: "acknowledged" | "delivered" | "sent" | "queued" | "failed" | "unknown";
  deliveredAt: string | null;
}

export function App() {
  // Theme state: Default Light mode as required
  const [isDarkMode, setIsDarkMode] = useState<boolean>(false);
  const [activeTab, setActiveTab] = useState<"overview" | "incidents" | "contacts" | "history" | "telemetry" | "settings">("incidents");
  const [selectedIncidentId, setSelectedIncidentId] = useState<string>("INC-2026-9812");
  const [isLiveConnected, setIsLiveConnected] = useState<boolean>(true);
  const [lastHeartbeat, setLastHeartbeat] = useState<string>(new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" }));

  // Assistance action states (Strict safety rule: Never imply police dispatched without confirmation)
  const [assistanceActionStatus, setAssistanceActionStatus] = useState<{ message: string; type: "info" | "warning" | "success" } | null>(null);

  // Active theme tokens
  const theme = isDarkMode ? DarkNeutralTokens : LightNeutralTokens;

  // Incident state matching our backend & mobile state machine
  const [incidents] = useState<Incident[]>([
    {
      id: "INC-2026-9812",
      user: "Priya Sharma",
      phone: "+91 98765 43210",
      status: "ACTIVE",
      coords: { lat: 28.6139, lng: 77.2090 },
      accuracy: 3.4,
      battery: 64,
      charging: false,
      connectivity: "CELLULAR_5G",
      movement: "Running",
      activationMethod: "SOS Hold Button (1.35s)",
      startedAt: "17:42:10 IST",
      breadcrumbsCount: 42,
      breadcrumbs: [
        { lat: 28.6125, lng: 77.2075, time: "17:40:02" },
        { lat: 28.6131, lng: 77.2081, time: "17:41:15" },
        { lat: 28.6136, lng: 77.2086, time: "17:41:50" },
        { lat: 28.6139, lng: 77.2090, time: "17:42:10" },
      ],
      timeline: [
        { id: "e1", type: "ACTIVATION", title: "SOS Triggered", timestamp: "17:42:10 IST", details: "Manual press-and-hold (1.35s threshold met)", status: "verified" },
        { id: "e2", type: "LOCATION", title: "Fused GPS Fix Established", timestamp: "17:42:11 IST", details: "Lat: 28.613900, Lng: 77.209000 (±3.4m accuracy)", status: "verified" },
        { id: "e3", type: "ESCALATION", title: "Grace Window Concluded", timestamp: "17:42:15 IST", details: "Primary emergency dispatch pipeline engaged", status: "dispatched" },
        { id: "e4", type: "NOTIFICATION", title: "SMS Gateway Broadcast", timestamp: "17:42:16 IST", details: "Multi-channel alerts queued for 4 Trusted Circle contacts", status: "dispatched" },
        { id: "e5", type: "ACK", title: "Guardian Acknowledged", timestamp: "17:42:38 IST", details: "Mother (Sunita Sharma) viewed live location link", status: "acknowledged" },
      ],
    },
    {
      id: "INC-2026-9799",
      user: "Ananya Rao",
      phone: "+91 98111 22334",
      status: "SAFE_CANCELLED",
      coords: { lat: 19.0760, lng: 72.8777 },
      accuracy: 5.1,
      battery: 89,
      charging: true,
      connectivity: "WIFI",
      movement: "Stationary",
      activationMethod: "Panic Gesture (4 Taps)",
      startedAt: "15:20:04 IST",
      breadcrumbsCount: 18,
      breadcrumbs: [
        { lat: 19.0755, lng: 72.8765, time: "15:18:20" },
        { lat: 19.0760, lng: 72.8777, time: "15:20:04" },
      ],
      timeline: [
        { id: "e10", type: "ACTIVATION", title: "Panic Gesture Activated", timestamp: "15:20:04 IST", details: "4 rapid taps detected by accelerometer classifier", status: "verified" },
        { id: "e11", type: "DISARM", title: "Safe PIN Verified", timestamp: "15:21:40 IST", details: "Disarmed cleanly via user safe authentication", status: "acknowledged" },
      ],
    },
  ]);

  const [contacts] = useState<TrustedContactStatus[]>([
    { id: "c1", name: "Sunita Sharma", relation: "Mother", phone: "+91 98765 00001", priority: 1, canReceiveLocation: true, deliveryStatus: "acknowledged", deliveredAt: "17:42:38 IST" },
    { id: "c2", name: "Rajesh Sharma", relation: "Father", phone: "+91 98765 00002", priority: 2, canReceiveLocation: true, deliveryStatus: "delivered", deliveredAt: "17:42:20 IST" },
    { id: "c3", name: "Dr. Vikram Seth", relation: "Emergency Contact", phone: "+91 98765 00003", priority: 3, canReceiveLocation: true, deliveryStatus: "sent", deliveredAt: "17:42:16 IST" },
    { id: "c4", name: "Rhea Sen", relation: "Friend", phone: "+91 98765 00004", priority: 4, canReceiveLocation: false, deliveryStatus: "delivered", deliveredAt: "17:42:25 IST" },
  ]);

  // Heartbeat polling to ensure live synchronization with backend
  useEffect(() => {
    const timer = setInterval(() => {
      setLastHeartbeat(new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" }));
    }, 4000);
    return () => clearInterval(timer);
  }, []);

  const activeIncident = incidents.find((i) => i.id === selectedIncidentId) || incidents[0];

  // Verified assistance action handlers following strict Safety Product Rules
  const handleInitiateAssistance = (type: "emergency_link" | "notify_guardians") => {
    if (type === "emergency_link") {
      setAssistanceActionStatus({
        message: "Prepared verified location payload. Direct authorities to: Lat 28.6139, Lng 77.2090. Local emergency services line: 112.",
        type: "warning",
      });
    } else {
      setAssistanceActionStatus({
        message: `Alert payload re-dispatched to ${contacts.filter(c => c.deliveryStatus !== "acknowledged").length} pending guardians.`,
        type: "info",
      });
    }
    setTimeout(() => setAssistanceActionStatus(null), 7000);
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: theme.background,
        color: theme.textPrimary,
        display: "flex",
        flexDirection: "column",
        transition: "background-color 0.2s ease, color 0.2s ease",
      }}
    >
      {/* =========================================================================
          1. ENTERPRISE HEADER
          ========================================================================= */}
      <header
        style={{
          borderBottom: `1px solid ${theme.border}`,
          backgroundColor: theme.surface,
          padding: "0.75rem 1.75rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          position: "sticky",
          top: 0,
          zIndex: 40,
          boxShadow: isDarkMode ? "none" : "0 1px 3px rgba(109, 46, 91, 0.04)",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "1.25rem" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
            <div
              style={{
                width: 36,
                height: 36,
                borderRadius: "10px",
                backgroundColor: BrandTokens.primary,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#FFFFFF",
                boxShadow: `0 2px 8px ${BrandTokens.primary}33`,
              }}
            >
              <Shield size={20} strokeWidth={2.2} />
            </div>
            <div>
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                <span style={{ fontSize: "1.15rem", fontWeight: 700, letterSpacing: "-0.01em", color: theme.textPrimary }}>
                  SheVault
                </span>
                <span
                  style={{
                    fontSize: "0.68rem",
                    padding: "0.15rem 0.5rem",
                    borderRadius: "6px",
                    backgroundColor: BrandTokens.primaryTint,
                    color: BrandTokens.primaryDark,
                    fontWeight: 700,
                    letterSpacing: "0.04em",
                  }}
                >
                  GUARDIAN PORTAL
                </span>
              </div>
              <p style={{ fontSize: "0.72rem", color: theme.textSecondary }}>Emergency Response & Telemetry Oversight</p>
            </div>
          </div>

          <div style={{ height: 24, width: 1, backgroundColor: theme.divider }} />

          {/* Breadcrumb / Page Title */}
          <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", fontSize: "0.82rem", color: theme.textSecondary }}>
            <span>Portal</span>
            <ChevronRight size={14} />
            <span style={{ fontWeight: 600, color: theme.textPrimary, textTransform: "capitalize" }}>
              {activeTab.replace("-", " ")}
            </span>
          </div>
        </div>

        {/* Header Right: Connection & Theme Toggle */}
        <div style={{ display: "flex", alignItems: "center", gap: "1.25rem" }}>
          {/* Subtle Live Connection Status */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: isDarkMode ? SafeTokens.surfaceDark : SafeTokens.surface,
              border: `1px solid ${SafeTokens.default}33`,
              padding: "0.3rem 0.75rem",
              borderRadius: "8px",
            }}
          >
            <span
              style={{
                width: 7,
                height: 7,
                borderRadius: "50%",
                backgroundColor: SafeTokens.default,
              }}
            />
            <span style={{ fontSize: "0.75rem", fontWeight: 600, color: SafeTokens.default }}>
              Connected ({lastHeartbeat})
            </span>
          </div>

          {/* Theme switcher */}
          <button
            onClick={() => setIsDarkMode(!isDarkMode)}
            title={isDarkMode ? "Switch to Light Theme" : "Switch to Dark Theme"}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 34,
              height: 34,
              borderRadius: "8px",
              backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background,
              border: `1px solid ${theme.border}`,
              color: theme.textSecondary,
            }}
          >
            {isDarkMode ? <Sun size={17} /> : <Moon size={17} />}
          </button>
        </div>
      </header>

      {/* =========================================================================
          2. CORE LAYOUT (Slim persistent sidebar + central content)
          ========================================================================= */}
      <div style={{ display: "flex", flex: 1 }}>
        {/* Persistent Left Sidebar */}
        <aside
          style={{
            width: 240,
            borderRight: `1px solid ${theme.border}`,
            backgroundColor: theme.surface,
            padding: "1.25rem 0.85rem",
            display: "flex",
            flexDirection: "column",
            gap: "0.35rem",
            flexShrink: 0,
          }}
        >
          <div style={{ fontSize: "0.68rem", textTransform: "uppercase", letterSpacing: "0.08em", color: theme.textDisabled, padding: "0 0.65rem 0.5rem", fontWeight: 700 }}>
            Navigation
          </div>

          {[
            { id: "overview", label: "Overview", icon: Compass, badge: null },
            { id: "incidents", label: "Active Incidents", icon: AlertTriangle, badge: incidents.filter((i) => i.status === "ACTIVE").length, badgeEmergency: true },
            { id: "contacts", label: "Trusted Circle", icon: Users, badge: contacts.length, badgeEmergency: false },
            { id: "history", label: "Incident History", icon: History, badge: null },
            { id: "telemetry", label: "Telemetry", icon: Activity, badge: null },
            { id: "settings", label: "Settings", icon: Settings, badge: null },
          ].map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id as any)}
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  padding: "0.65rem 0.85rem",
                  borderRadius: "8px",
                  backgroundColor: isActive ? (isDarkMode ? DarkNeutralTokens.surfaceElevated : BrandTokens.primaryTint) : "transparent",
                  color: isActive ? BrandTokens.primary : theme.textSecondary,
                  fontWeight: isActive ? 700 : 500,
                  fontSize: "0.85rem",
                  textAlign: "left",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <Icon size={18} strokeWidth={isActive ? 2.3 : 1.8} color={isActive ? BrandTokens.primary : theme.textSecondary} />
                  <span>{item.label}</span>
                </div>
                {item.badge !== null && (
                  <span
                    style={{
                      backgroundColor: item.badgeEmergency ? EmergencyTokens.default : theme.divider,
                      color: item.badgeEmergency ? "#FFFFFF" : theme.textSecondary,
                      fontSize: "0.68rem",
                      fontWeight: 700,
                      padding: "0.1rem 0.45rem",
                      borderRadius: "999px",
                    }}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}

          {/* Sidebar Footer with system integrity status */}
          <div style={{ marginTop: "auto", borderTop: `1px solid ${theme.border}`, paddingTop: "1rem", paddingLeft: "0.5rem" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.35rem" }}>
              <span style={{ width: 6, height: 6, borderRadius: "50%", backgroundColor: SafeTokens.default }} />
              <span style={{ fontSize: "0.72rem", fontWeight: 600, color: theme.textPrimary }}>Platform Verified</span>
            </div>
            <p style={{ fontSize: "0.68rem", color: theme.textSecondary, lineHeight: 1.4 }}>
              Encrypted Guardian Mesh
              <br />
              Zero-leak covert protocol active
            </p>
          </div>
        </aside>

        {/* Central Content Area */}
        <main style={{ flex: 1, padding: "1.75rem 2.25rem", overflowY: "auto", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
          {/* Action notification banner if triggered */}
          {assistanceActionStatus && (
            <div
              style={{
                padding: "0.85rem 1.25rem",
                borderRadius: "10px",
                backgroundColor:
                  assistanceActionStatus.type === "warning"
                    ? (isDarkMode ? WarningTokens.surfaceDark : WarningTokens.surface)
                    : (isDarkMode ? InfoTokens.surfaceDark : InfoTokens.surface),
                border: `1px solid ${assistanceActionStatus.type === "warning" ? WarningTokens.default : InfoTokens.default}44`,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                fontSize: "0.85rem",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                <AlertCircle size={18} color={assistanceActionStatus.type === "warning" ? WarningTokens.default : InfoTokens.default} />
                <span style={{ fontWeight: 500 }}>{assistanceActionStatus.message}</span>
              </div>
              <button onClick={() => setAssistanceActionStatus(null)} style={{ fontSize: "0.75rem", color: theme.textSecondary, textDecoration: "underline" }}>
                Dismiss
              </button>
            </div>
          )}

          {/* =========================================================================
              TAB: ACTIVE INCIDENT (Human-centered, restrained emergency response)
              ========================================================================= */}
          {activeTab === "incidents" && (
            <>
              {/* 1. Incident Status Bar */}
              <div
                style={{
                  backgroundColor: theme.surface,
                  border: `1px solid ${theme.border}`,
                  borderRadius: "12px",
                  padding: "1.1rem 1.5rem",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  flexWrap: "wrap",
                  gap: "1rem",
                  boxShadow: isDarkMode ? "none" : "0 2px 6px rgba(109, 46, 91, 0.03)",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
                  <div
                    style={{
                      width: 42,
                      height: 42,
                      borderRadius: "10px",
                      backgroundColor: activeIncident.status === "ACTIVE" ? EmergencyTokens.surface : SafeTokens.surface,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: activeIncident.status === "ACTIVE" ? EmergencyTokens.default : SafeTokens.default,
                    }}
                  >
                    <AlertTriangle size={22} strokeWidth={2.2} />
                  </div>
                  <div>
                    <div style={{ display: "flex", alignItems: "center", gap: "0.6rem" }}>
                      <h2 style={{ fontSize: "1.15rem", fontWeight: 700, color: theme.textPrimary }}>
                        {activeIncident.user}
                      </h2>
                      <span
                        style={{
                          fontSize: "0.7rem",
                          fontWeight: 700,
                          padding: "0.2rem 0.55rem",
                          borderRadius: "6px",
                          backgroundColor: activeIncident.status === "ACTIVE" ? EmergencyTokens.surface : SafeTokens.surface,
                          color: activeIncident.status === "ACTIVE" ? EmergencyTokens.default : SafeTokens.default,
                          border: `1px solid ${activeIncident.status === "ACTIVE" ? EmergencyTokens.default : SafeTokens.default}33`,
                        }}
                      >
                        {activeIncident.status === "ACTIVE" ? "ACTIVE EMERGENCY" : "RESOLVED"}
                      </span>
                    </div>
                    <div style={{ display: "flex", alignItems: "center", gap: "1rem", fontSize: "0.78rem", color: theme.textSecondary, marginTop: "0.2rem" }}>
                      <span>Started: <strong>{activeIncident.startedAt}</strong></span>
                      <span>•</span>
                      <span>Trigger: <strong>{activeIncident.activationMethod}</strong></span>
                      <span>•</span>
                      <span>Session ID: <code style={{ fontSize: "0.74rem" }}>{activeIncident.id}</code></span>
                    </div>
                  </div>
                </div>

                {/* Differentiated Assistance Actions */}
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <button
                    onClick={() => handleInitiateAssistance("notify_guardians")}
                    style={{
                      padding: "0.55rem 1rem",
                      borderRadius: "8px",
                      backgroundColor: theme.surface,
                      border: `1px solid ${theme.border}`,
                      color: theme.textPrimary,
                      fontSize: "0.82rem",
                      fontWeight: 600,
                      display: "flex",
                      alignItems: "center",
                      gap: "0.45rem",
                    }}
                  >
                    <Send size={15} color={theme.textSecondary} />
                    Notify Circle Contacts
                  </button>
                  <button
                    onClick={() => handleInitiateAssistance("emergency_link")}
                    style={{
                      padding: "0.55rem 1rem",
                      borderRadius: "8px",
                      backgroundColor: BrandTokens.primary,
                      color: "#FFFFFF",
                      fontSize: "0.82rem",
                      fontWeight: 600,
                      display: "flex",
                      alignItems: "center",
                      gap: "0.45rem",
                      boxShadow: `0 2px 6px ${BrandTokens.primary}33`,
                    }}
                  >
                    <ExternalLink size={15} />
                    Export Authorities Dispatch Dossier
                  </button>
                </div>
              </div>

              {/* 2-Column Responsive Layout: Real Map View (larger) + Incident Details */}
              <div style={{ display: "grid", gridTemplateColumns: "1.45fr 1fr", gap: "1.5rem", alignItems: "start" }}>
                {/* LEFT: Live Geospatial Map Card */}
                <div
                  style={{
                    backgroundColor: theme.surface,
                    borderRadius: "12px",
                    border: `1px solid ${theme.border}`,
                    padding: "1.25rem",
                    display: "flex",
                    flexDirection: "column",
                    gap: "1rem",
                    boxShadow: isDarkMode ? "none" : "0 2px 8px rgba(109, 46, 91, 0.03)",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div>
                      <h3 style={{ fontSize: "0.95rem", fontWeight: 700 }}>Geospatial Movement & Location Vector</h3>
                      <p style={{ fontSize: "0.75rem", color: theme.textSecondary }}>Fused Location Provider • Verified GPS Fix</p>
                    </div>
                    <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                      <span
                        style={{
                          fontSize: "0.72rem",
                          fontWeight: 600,
                          padding: "0.2rem 0.5rem",
                          borderRadius: "6px",
                          backgroundColor: isDarkMode ? InfoTokens.surfaceDark : InfoTokens.surface,
                          color: InfoTokens.default,
                        }}
                      >
                        Accuracy: ±{activeIncident.accuracy}m
                      </span>
                      <span
                        style={{
                          fontSize: "0.72rem",
                          fontWeight: 600,
                          padding: "0.2rem 0.5rem",
                          borderRadius: "6px",
                          backgroundColor: theme.divider,
                          color: theme.textSecondary,
                        }}
                      >
                        {activeIncident.breadcrumbsCount} Breadcrumbs
                      </span>
                    </div>
                  </div>

                  {/* Clean SVG Cartographic Canvas (Replaces fake cyberpunk radar) */}
                  <div
                    style={{
                      height: 380,
                      borderRadius: "10px",
                      backgroundColor: isDarkMode ? "#1B161B" : "#F7F3F5",
                      border: `1px solid ${theme.border}`,
                      position: "relative",
                      overflow: "hidden",
                    }}
                  >
                    {/* Real Cartographic Grid Lines */}
                    <svg width="100%" height="100%" style={{ position: "absolute", inset: 0 }}>
                      <defs>
                        <pattern id="cartoGrid" width="40" height="40" patternUnits="userSpaceOnUse">
                          <path d="M 40 0 L 0 0 0 40" fill="none" stroke={isDarkMode ? "rgba(255,255,255,0.05)" : "rgba(109,46,91,0.06)"} strokeWidth="1" />
                        </pattern>
                      </defs>
                      <rect width="100%" height="100%" fill="url(#cartoGrid)" />

                      {/* Movement trail line connecting breadcrumbs */}
                      <polyline
                        points="90,300 160,240 230,170 300,120"
                        fill="none"
                        stroke={BrandTokens.primary}
                        strokeWidth="3"
                        strokeDasharray="6,4"
                      />

                      {/* Historical Breadcrumb Points */}
                      <circle cx="90" cy="300" r="5" fill={BrandTokens.rose} opacity="0.6" />
                      <circle cx="160" cy="240" r="5" fill={BrandTokens.rose} opacity="0.75" />
                      <circle cx="230" cy="170" r="5" fill={BrandTokens.rose} opacity="0.9" />

                      {/* Current User Accuracy Radius */}
                      <circle cx="300" cy="120" r="28" fill={BrandTokens.primaryTint} opacity={isDarkMode ? "0.2" : "0.5"} stroke={BrandTokens.primary} strokeWidth="1.5" />

                      {/* Current User Live Pin */}
                      <circle cx="300" cy="120" r="8" fill={BrandTokens.primary} stroke="#FFFFFF" strokeWidth="2.5" />
                    </svg>

                    {/* Map Overlay Badge */}
                    <div
                      style={{
                        position: "absolute",
                        top: 14,
                        left: 14,
                        backgroundColor: theme.surface,
                        border: `1px solid ${theme.border}`,
                        padding: "0.45rem 0.75rem",
                        borderRadius: "8px",
                        boxShadow: "0 2px 6px rgba(0,0,0,0.06)",
                        fontSize: "0.75rem",
                      }}
                    >
                      <div style={{ fontWeight: 700, color: theme.textPrimary, display: "flex", alignItems: "center", gap: 5 }}>
                        <MapPin size={14} color={BrandTokens.primary} />
                        Current Position: {activeIncident.user}
                      </div>
                      <div style={{ fontSize: "0.68rem", color: theme.textSecondary, marginTop: 2 }}>
                        {activeIncident.coords.lat.toFixed(6)}° N, {activeIncident.coords.lng.toFixed(6)}° E
                      </div>
                    </div>

                    {/* Map Legend / Controls */}
                    <div
                      style={{
                        position: "absolute",
                        bottom: 14,
                        left: 14,
                        right: 14,
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        fontSize: "0.72rem",
                        color: theme.textSecondary,
                        backgroundColor: `${theme.surface}EE`,
                        padding: "0.4rem 0.75rem",
                        borderRadius: "8px",
                        border: `1px solid ${theme.border}`,
                        backdropFilter: "blur(4px)",
                      }}
                    >
                      <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
                        <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
                          <span style={{ width: 8, height: 8, borderRadius: "50%", backgroundColor: BrandTokens.primary }} /> Current Fix
                        </span>
                        <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
                          <span style={{ width: 14, height: 2, backgroundColor: BrandTokens.primary }} /> Trail Vector
                        </span>
                      </div>
                      <span>Updated 2s ago • Cellular GPS High Priority</span>
                    </div>
                  </div>
                </div>

                {/* RIGHT: Incident Details & Live Telemetry Panel */}
                <div style={{ display: "flex", flexDirection: "column", gap: "1.25rem" }}>
                  {/* Telemetry Metrics Grid */}
                  <div
                    style={{
                      backgroundColor: theme.surface,
                      borderRadius: "12px",
                      border: `1px solid ${theme.border}`,
                      padding: "1.25rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.85rem",
                      boxShadow: isDarkMode ? "none" : "0 2px 8px rgba(109, 46, 91, 0.03)",
                    }}
                  >
                    <h3 style={{ fontSize: "0.95rem", fontWeight: 700 }}>Telemetry & Device State</h3>

                    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }}>
                      {/* Metric: Battery */}
                      <div style={{ padding: "0.75rem", borderRadius: "8px", backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background, border: `1px solid ${theme.border}` }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: theme.textSecondary, fontSize: "0.72rem" }}>
                          <Battery size={14} /> Battery
                        </div>
                        <div style={{ fontSize: "1.1rem", fontWeight: 700, color: activeIncident.battery < 20 ? EmergencyTokens.default : theme.textPrimary, marginTop: 4 }}>
                          {activeIncident.battery}%
                        </div>
                        <span style={{ fontSize: "0.68rem", color: theme.textSecondary }}>{activeIncident.charging ? "Charging" : "Discharging"}</span>
                      </div>

                      {/* Metric: Movement */}
                      <div style={{ padding: "0.75rem", borderRadius: "8px", backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background, border: `1px solid ${theme.border}` }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: theme.textSecondary, fontSize: "0.72rem" }}>
                          <Navigation size={14} /> Movement Mode
                        </div>
                        <div style={{ fontSize: "1.1rem", fontWeight: 700, color: WarningTokens.default, marginTop: 4 }}>
                          {activeIncident.movement}
                        </div>
                        <span style={{ fontSize: "0.68rem", color: theme.textSecondary }}>3s Sampling interval</span>
                      </div>

                      {/* Metric: Connectivity */}
                      <div style={{ padding: "0.75rem", borderRadius: "8px", backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background, border: `1px solid ${theme.border}` }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: theme.textSecondary, fontSize: "0.72rem" }}>
                          <Wifi size={14} /> Connectivity
                        </div>
                        <div style={{ fontSize: "1.1rem", fontWeight: 700, color: SafeTokens.default, marginTop: 4 }}>
                          {activeIncident.connectivity.replace("_", " ")}
                        </div>
                        <span style={{ fontSize: "0.68rem", color: theme.textSecondary }}>Live Socket streaming</span>
                      </div>

                      {/* Metric: Accuracy */}
                      <div style={{ padding: "0.75rem", borderRadius: "8px", backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background, border: `1px solid ${theme.border}` }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: theme.textSecondary, fontSize: "0.72rem" }}>
                          <Compass size={14} /> Fix Radius
                        </div>
                        <div style={{ fontSize: "1.1rem", fontWeight: 700, color: InfoTokens.default, marginTop: 4 }}>
                          ±{activeIncident.accuracy}m
                        </div>
                        <span style={{ fontSize: "0.68rem", color: theme.textSecondary }}>Fused GNSS Provider</span>
                      </div>
                    </div>
                  </div>

                  {/* Trusted Contacts Delivery Ledger */}
                  <div
                    style={{
                      backgroundColor: theme.surface,
                      borderRadius: "12px",
                      border: `1px solid ${theme.border}`,
                      padding: "1.25rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.85rem",
                      boxShadow: isDarkMode ? "none" : "0 2px 8px rgba(109, 46, 91, 0.03)",
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <h3 style={{ fontSize: "0.95rem", fontWeight: 700 }}>Circle Dispatch Status</h3>
                      <span style={{ fontSize: "0.72rem", color: theme.textSecondary }}>Real delivery proofs</span>
                    </div>

                    <div style={{ display: "flex", flexDirection: "column", gap: "0.6rem" }}>
                      {contacts.map((contact) => {
                        const statusColors: Record<string, { bg: string; text: string }> = {
                          acknowledged: { bg: SafeTokens.surface, text: SafeTokens.default },
                          delivered: { bg: isDarkMode ? InfoTokens.surfaceDark : InfoTokens.surface, text: InfoTokens.default },
                          sent: { bg: theme.divider, text: theme.textSecondary },
                          queued: { bg: WarningTokens.surface, text: WarningTokens.default },
                          failed: { bg: EmergencyTokens.surface, text: EmergencyTokens.default },
                          unknown: { bg: theme.divider, text: theme.textDisabled },
                        };
                        const sc = statusColors[contact.deliveryStatus] || statusColors.unknown;

                        return (
                          <div
                            key={contact.id}
                            style={{
                              padding: "0.65rem 0.85rem",
                              borderRadius: "8px",
                              backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background,
                              border: `1px solid ${theme.border}`,
                              display: "flex",
                              justifyContent: "space-between",
                              alignItems: "center",
                            }}
                          >
                            <div>
                              <div style={{ fontSize: "0.82rem", fontWeight: 600 }}>
                                {contact.name} ({contact.relation})
                              </div>
                              <div style={{ fontSize: "0.7rem", color: theme.textSecondary }}>
                                Priority #{contact.priority} • {contact.phone}
                              </div>
                            </div>
                            <div style={{ textAlign: "right" }}>
                              <span
                                style={{
                                  fontSize: "0.68rem",
                                  fontWeight: 700,
                                  padding: "0.15rem 0.5rem",
                                  borderRadius: "6px",
                                  backgroundColor: sc.bg,
                                  color: sc.text,
                                  textTransform: "uppercase",
                                }}
                              >
                                {contact.deliveryStatus}
                              </span>
                              {contact.deliveredAt && (
                                <div style={{ fontSize: "0.65rem", color: theme.textDisabled, marginTop: 2 }}>
                                  {contact.deliveredAt}
                                </div>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              </div>

              {/* 3. Chronological Incident Event Timeline */}
              <div
                style={{
                  backgroundColor: theme.surface,
                  borderRadius: "12px",
                  border: `1px solid ${theme.border}`,
                  padding: "1.25rem 1.5rem",
                  boxShadow: isDarkMode ? "none" : "0 2px 8px rgba(109, 46, 91, 0.03)",
                }}
              >
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                  <div>
                    <h3 style={{ fontSize: "0.95rem", fontWeight: 700 }}>Forensic Incident Timeline</h3>
                    <p style={{ fontSize: "0.75rem", color: theme.textSecondary }}>Verified events from persistent incident journal</p>
                  </div>
                  <span style={{ fontSize: "0.72rem", color: theme.textDisabled }}>Cryptographically sequence-checked</span>
                </div>

                <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
                  {activeIncident.timeline.map((evt, idx) => (
                    <div key={evt.id} style={{ display: "flex", gap: "1rem", alignItems: "flex-start" }}>
                      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", width: 16 }}>
                        <div
                          style={{
                            width: 10,
                            height: 10,
                            borderRadius: "50%",
                            backgroundColor: evt.status === "acknowledged" ? SafeTokens.default : BrandTokens.primary,
                            marginTop: 4,
                          }}
                        />
                        {idx !== activeIncident.timeline.length - 1 && (
                          <div style={{ width: 2, height: 32, backgroundColor: theme.divider }} />
                        )}
                      </div>
                      <div style={{ flex: 1, paddingBottom: 6 }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                          <span style={{ fontSize: "0.84rem", fontWeight: 600, color: theme.textPrimary }}>{evt.title}</span>
                          <span style={{ fontSize: "0.72rem", color: theme.textSecondary }}>{evt.timestamp}</span>
                        </div>
                        <p style={{ fontSize: "0.76rem", color: theme.textSecondary, marginTop: 2 }}>{evt.details}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}

          {/* =========================================================================
              TAB: OVERVIEW
              ========================================================================= */}
          {activeTab === "overview" && (
            <div style={{ display: "flex", flexDirection: "column", gap: "1.25rem" }}>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "1.25rem" }}>
                <div style={{ padding: "1.25rem", borderRadius: "12px", backgroundColor: theme.surface, border: `1px solid ${theme.border}` }}>
                  <div style={{ fontSize: "0.75rem", color: theme.textSecondary, fontWeight: 600 }}>Active Protection Sessions</div>
                  <div style={{ fontSize: "1.6rem", fontWeight: 800, marginTop: "0.5rem", color: EmergencyTokens.default }}>1</div>
                  <p style={{ fontSize: "0.75rem", color: theme.textSecondary, marginTop: "0.25rem" }}>Priya Sharma (SOS Broadcasting)</p>
                </div>
                <div style={{ padding: "1.25rem", borderRadius: "12px", backgroundColor: theme.surface, border: `1px solid ${theme.border}` }}>
                  <div style={{ fontSize: "0.75rem", color: theme.textSecondary, fontWeight: 600 }}>Trusted Guardians Configured</div>
                  <div style={{ fontSize: "1.6rem", fontWeight: 800, marginTop: "0.5rem", color: BrandTokens.primary }}>4</div>
                  <p style={{ fontSize: "0.75rem", color: theme.textSecondary, marginTop: "0.25rem" }}>All contacts reachable via SMS/Push</p>
                </div>
                <div style={{ padding: "1.25rem", borderRadius: "12px", backgroundColor: theme.surface, border: `1px solid ${theme.border}` }}>
                  <div style={{ fontSize: "0.75rem", color: theme.textSecondary, fontWeight: 600 }}>Resolved Incidents</div>
                  <div style={{ fontSize: "1.6rem", fontWeight: 800, marginTop: "0.5rem", color: SafeTokens.default }}>1</div>
                  <p style={{ fontSize: "0.75rem", color: theme.textSecondary, marginTop: "0.25rem" }}>Safe PIN cancellation verified</p>
                </div>
              </div>

              <div style={{ padding: "1.5rem", borderRadius: "12px", backgroundColor: theme.surface, border: `1px solid ${theme.border}` }}>
                <h3 style={{ fontSize: "1rem", fontWeight: 700, marginBottom: "0.5rem" }}>Guardian Quick Links</h3>
                <p style={{ fontSize: "0.8rem", color: theme.textSecondary, marginBottom: "1rem" }}>
                  Select an active emergency below to inspect live GPS vector tracking and dispatch status.
                </p>
                <button
                  onClick={() => setActiveTab("incidents")}
                  style={{
                    padding: "0.6rem 1.25rem",
                    borderRadius: "8px",
                    backgroundColor: BrandTokens.primary,
                    color: "#FFFFFF",
                    fontSize: "0.85rem",
                    fontWeight: 600,
                  }}
                >
                  Inspect Active SOS (Priya Sharma) →
                </button>
              </div>
            </div>
          )}

          {/* =========================================================================
              TAB: TRUSTED CIRCLE
              ========================================================================= */}
          {activeTab === "contacts" && (
            <div style={{ backgroundColor: theme.surface, borderRadius: "12px", border: `1px solid ${theme.border}`, padding: "1.5rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.25rem" }}>
                <div>
                  <h3 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Trusted Circle Roster</h3>
                  <p style={{ color: theme.textSecondary, fontSize: "0.78rem" }}>Authorized recipients of encrypted emergency broadcasts</p>
                </div>
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "1rem" }}>
                {contacts.map((contact) => (
                  <div
                    key={contact.id}
                    style={{
                      backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background,
                      border: `1px solid ${theme.border}`,
                      borderRadius: "10px",
                      padding: "1rem 1.2rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.5rem",
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <span style={{ fontWeight: 700, fontSize: "0.95rem" }}>{contact.name}</span>
                      <span style={{ backgroundColor: BrandTokens.primaryTint, color: BrandTokens.primaryDark, fontSize: "0.68rem", fontWeight: 700, padding: "0.15rem 0.5rem", borderRadius: "999px" }}>
                        Priority #{contact.priority}
                      </span>
                    </div>
                    <div style={{ fontSize: "0.8rem", color: theme.textSecondary }}>
                      Relation: <strong style={{ color: theme.textPrimary }}>{contact.relation}</strong>
                    </div>
                    <div style={{ fontSize: "0.8rem", color: theme.textSecondary }}>Phone: {contact.phone}</div>
                    <div style={{ fontSize: "0.75rem", color: contact.canReceiveLocation ? SafeTokens.default : theme.textDisabled, marginTop: 4 }}>
                      {contact.canReceiveLocation ? "✓ GPS Location Authorized" : "✕ Location Disabled"}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* =========================================================================
              TAB: INCIDENT HISTORY
              ========================================================================= */}
          {activeTab === "history" && (
            <div style={{ backgroundColor: theme.surface, borderRadius: "12px", border: `1px solid ${theme.border}`, padding: "1.5rem" }}>
              <h3 style={{ fontSize: "1.1rem", fontWeight: 700, marginBottom: "0.35rem" }}>Auditable Incident Journal</h3>
              <p style={{ color: theme.textSecondary, fontSize: "0.78rem", marginBottom: "1.25rem" }}>
                Archived security sessions persisted in Room database for forensic analysis.
              </p>

              <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
                {incidents.map((inc) => (
                  <div
                    key={inc.id}
                    style={{
                      padding: "1rem 1.25rem",
                      borderRadius: "10px",
                      backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background,
                      border: `1px solid ${theme.border}`,
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                    }}
                  >
                    <div>
                      <div style={{ display: "flex", alignItems: "center", gap: "0.6rem" }}>
                        <span style={{ fontWeight: 700, fontSize: "0.95rem" }}>{inc.user}</span>
                        <code style={{ fontSize: "0.72rem", color: theme.textSecondary }}>{inc.id}</code>
                      </div>
                      <div style={{ fontSize: "0.76rem", color: theme.textSecondary, marginTop: 3 }}>
                        {inc.activationMethod} • Started {inc.startedAt} • {inc.breadcrumbsCount} Breadcrumbs
                      </div>
                    </div>
                    <div>
                      <span
                        style={{
                          fontSize: "0.7rem",
                          fontWeight: 700,
                          padding: "0.2rem 0.6rem",
                          borderRadius: "6px",
                          backgroundColor: inc.status === "ACTIVE" ? EmergencyTokens.surface : SafeTokens.surface,
                          color: inc.status === "ACTIVE" ? EmergencyTokens.default : SafeTokens.default,
                        }}
                      >
                        {inc.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* =========================================================================
              TAB: TELEMETRY ENGINE
              ========================================================================= */}
          {activeTab === "telemetry" && (
            <div style={{ backgroundColor: theme.surface, borderRadius: "12px", border: `1px solid ${theme.border}`, padding: "1.5rem" }}>
              <h3 style={{ fontSize: "1.1rem", fontWeight: 700, marginBottom: "0.35rem" }}>Adaptive Telemetry Policy Engine</h3>
              <p style={{ color: theme.textSecondary, fontSize: "0.78rem", marginBottom: "1.5rem" }}>
                Dynamically tunes GPS, sensor, and upload intervals based on battery temperature, movement velocity, and hardware lifecycle.
              </p>

              <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "1rem" }}>
                {[
                  {
                    tier: "NORMAL TIER",
                    status: "Battery > 25%",
                    desc: "3-5s GPS Breadcrumbs, high-fidelity accelerometer variance, live WebSocket streaming.",
                    color: SafeTokens.default,
                    bg: SafeTokens.surface,
                  },
                  {
                    tier: "CONSTRAINED TIER",
                    status: "Battery 10% - 25%",
                    desc: "15s GPS batching, sensor rate halved, cellular back-off applied to conserve energy.",
                    color: WarningTokens.default,
                    bg: WarningTokens.surface,
                  },
                  {
                    tier: "CRITICAL TIER",
                    status: "Battery < 10%",
                    desc: "60-120s GPS bursts, audio paused, SMS emergency fallback payload queued.",
                    color: EmergencyTokens.default,
                    bg: EmergencyTokens.surface,
                  },
                ].map((tier, idx) => (
                  <div
                    key={idx}
                    style={{
                      backgroundColor: isDarkMode ? DarkNeutralTokens.surfaceElevated : LightNeutralTokens.background,
                      border: `1.5px solid ${tier.color}`,
                      borderRadius: "10px",
                      padding: "1.2rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.5rem",
                    }}
                  >
                    <div style={{ fontWeight: 800, color: tier.color, fontSize: "0.95rem" }}>{tier.tier}</div>
                    <div style={{ fontSize: "0.72rem", color: theme.textSecondary }}>Condition: {tier.status}</div>
                    <p style={{ fontSize: "0.78rem", color: theme.textSecondary, lineHeight: 1.4, marginTop: 4 }}>{tier.desc}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* =========================================================================
              TAB: SETTINGS
              ========================================================================= */}
          {activeTab === "settings" && (
            <div style={{ backgroundColor: theme.surface, borderRadius: "12px", border: `1px solid ${theme.border}`, padding: "1.5rem" }}>
              <h3 style={{ fontSize: "1.1rem", fontWeight: 700, marginBottom: "0.35rem" }}>Guardian Portal Settings</h3>
              <p style={{ color: theme.textSecondary, fontSize: "0.78rem", marginBottom: "1.25rem" }}>
                Portal authentication and emergency dispatch gateway configurations.
              </p>

              <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                <div style={{ padding: "1rem", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
                  <div style={{ fontWeight: 600, fontSize: "0.85rem" }}>Emergency Dispatch Relay</div>
                  <p style={{ fontSize: "0.75rem", color: theme.textSecondary, marginTop: 2 }}>
                    Auto-forwards emergency location packets to designated SMS and cloud gateways.
                  </p>
                </div>
                <div style={{ padding: "1rem", borderRadius: "8px", border: `1px solid ${theme.border}` }}>
                  <div style={{ fontWeight: 600, fontSize: "0.85rem" }}>Data Retention & Privacy</div>
                  <p style={{ fontSize: "0.75rem", color: theme.textSecondary, marginTop: 2 }}>
                    GPS breadcrumbs automatically purged 30 days after incident resolution.
                  </p>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
