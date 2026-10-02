import React, { useState, useEffect } from "react";
import { SheVaultColors } from "./tokens";

interface Incident {
  id: string;
  user: string;
  phone: string;
  status: "ACTIVE" | "SAFE_CANCELLED" | "COERCED_DURESS" | "ESCALATED";
  coords: { lat: number; lng: number };
  accuracy: number;
  battery: number;
  charging: boolean;
  movement: "Stationary" | "Walking" | "Running" | "Vehicle";
  activationMethod: string;
  startedAt: string;
  breadcrumbsCount: number;
  audioRecording: boolean;
}

interface TrustedContact {
  name: string;
  relation: string;
  phone: string;
  priority: number;
  canReceiveLocation: boolean;
}

export function App() {
  const [activeTab, setActiveTab] = useState<"incidents" | "contacts" | "telemetry" | "raw">("incidents");
  const [selectedIncidentId, setSelectedIncidentId] = useState<string>("INC-2026-9812");
  const [systemOnline, setSystemOnline] = useState<boolean>(true);
  const [lastHeartbeat, setLastHeartbeat] = useState<string>(new Date().toLocaleTimeString());

  // Incident state matching our Android Session Machine
  const [incidents, setIncidents] = useState<Incident[]>([
    {
      id: "INC-2026-9812",
      user: "Priya Sharma",
      phone: "+91 98765 43210",
      status: "ACTIVE",
      coords: { lat: 28.6139, lng: 77.2090 },
      accuracy: 3.4,
      battery: 64,
      charging: false,
      movement: "Running",
      activationMethod: "HOLD_SOS_BUTTON (1.35s)",
      startedAt: "17:42:10 IST",
      breadcrumbsCount: 42,
      audioRecording: true,
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
      movement: "Stationary",
      activationMethod: "PANIC_GESTURE (4 taps)",
      startedAt: "15:20:04 IST",
      breadcrumbsCount: 18,
      audioRecording: false,
    },
  ]);

  const [contacts] = useState<TrustedContact[]>([
    { name: "Sunita Sharma", relation: "Mother", phone: "+91 98765 00001", priority: 1, canReceiveLocation: true },
    { name: "Rajesh Sharma", relation: "Father", phone: "+91 98765 00002", priority: 2, canReceiveLocation: true },
    { name: "Dr. Vikram Seth", relation: "Emergency Contact", phone: "+91 98765 00003", priority: 3, canReceiveLocation: true },
    { name: "Rhea Sen", relation: "Friend", phone: "+91 98765 00004", priority: 4, canReceiveLocation: false },
  ]);

  useEffect(() => {
    const timer = setInterval(() => {
      setLastHeartbeat(new Date().toLocaleTimeString());
    }, 3000);
    return () => clearInterval(timer);
  }, []);

  const activeIncident = incidents.find((i) => i.id === selectedIncidentId) || incidents[0];

  return (
    <div style={{ minHeight: "100vh", backgroundColor: "#090D16", color: SheVaultColors.textPrimary, display: "flex", flexDirection: "column" }}>
      {/* Top Navbar */}
      <header
        style={{
          borderBottom: `1px solid ${SheVaultColors.border}`,
          backgroundColor: "#0F172A",
          padding: "0.875rem 2rem",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
          <div
            style={{
              width: 38,
              height: 38,
              borderRadius: "10px",
              backgroundColor: SheVaultColors.emergency,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontWeight: 800,
              fontSize: "1.2rem",
              color: SheVaultColors.white,
              boxShadow: "0 0 20px rgba(201, 42, 50, 0.45)",
            }}
          >
            🛡️
          </div>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: "0.6rem" }}>
              <span style={{ fontSize: "1.25rem", fontWeight: 800, letterSpacing: "-0.02em" }}>SheVault</span>
              <span
                style={{
                  fontSize: "0.68rem",
                  padding: "0.15rem 0.5rem",
                  borderRadius: "999px",
                  backgroundColor: "#2E0854",
                  color: "#C4B5FD",
                  fontWeight: 600,
                  border: "1px solid #6D28D9",
                }}
              >
                GUARDIAN PORTAL
              </span>
            </div>
            <span style={{ fontSize: "0.75rem", color: SheVaultColors.textSecondary }}>Native Safety Cloud Dispatch & Telemetry Hub</span>
          </div>
        </div>

        {/* Global System Telemetry Status */}
        <div style={{ display: "flex", alignItems: "center", gap: "1.5rem" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontSize: "0.8rem", color: SheVaultColors.textSecondary }}>
            <span>Backend Sync:</span>
            <code style={{ color: "#38BDF8", backgroundColor: "#1E293B", padding: "0.2rem 0.4rem", borderRadius: "4px" }}>:8000/api/v1</code>
          </div>

          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
              backgroundColor: "rgba(16, 185, 129, 0.12)",
              border: "1px solid rgba(16, 185, 129, 0.3)",
              padding: "0.35rem 0.75rem",
              borderRadius: "999px",
            }}
          >
            <span style={{ width: 8, height: 8, borderRadius: "50%", backgroundColor: SheVaultColors.safe }} />
            <span style={{ fontSize: "0.75rem", fontWeight: 600, color: "#34D399" }}>Live Mesh Connected ({lastHeartbeat})</span>
          </div>
        </div>
      </header>

      {/* Main Command Dashboard Layout */}
      <div style={{ display: "flex", flex: 1 }}>
        {/* Left Sidebar Navigation */}
        <aside
          style={{
            width: 260,
            borderRight: `1px solid ${SheVaultColors.border}`,
            backgroundColor: "#0B1120",
            padding: "1.5rem 1rem",
            display: "flex",
            flexDirection: "column",
            gap: "0.5rem",
          }}
        >
          <div style={{ fontSize: "0.7rem", textTransform: "uppercase", letterSpacing: "0.08em", color: SheVaultColors.textMuted, padding: "0 0.5rem 0.5rem" }}>
            Guardian Controls
          </div>

          {[
            { id: "incidents", label: "Active Incidents", badge: incidents.filter((i) => i.status === "ACTIVE").length, icon: "🚨" },
            { id: "contacts", label: "Trusted Circle", badge: contacts.length, icon: "👥" },
            { id: "telemetry", label: "Adaptive Telemetry", badge: null, icon: "🛰️" },
            { id: "raw", label: "Incident Journal (Room)", badge: null, icon: "📜" },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: "0.75rem 0.875rem",
                borderRadius: "8px",
                border: "none",
                cursor: "pointer",
                backgroundColor: activeTab === tab.id ? "#1E293B" : "transparent",
                color: activeTab === tab.id ? SheVaultColors.white : SheVaultColors.textSecondary,
                fontWeight: activeTab === tab.id ? 600 : 500,
                fontSize: "0.875rem",
                textAlign: "left",
                transition: "all 0.15s ease",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                <span>{tab.icon}</span>
                <span>{tab.label}</span>
              </div>
              {tab.badge !== null && (
                <span
                  style={{
                    backgroundColor: tab.id === "incidents" ? SheVaultColors.emergency : "#334155",
                    color: SheVaultColors.white,
                    fontSize: "0.7rem",
                    fontWeight: 700,
                    padding: "0.15rem 0.45rem",
                    borderRadius: "999px",
                  }}
                >
                  {tab.badge}
                </span>
              )}
            </button>
          ))}

          <div style={{ marginTop: "auto", borderTop: `1px solid ${SheVaultColors.border}`, paddingTop: "1rem" }}>
            <div style={{ fontSize: "0.75rem", color: SheVaultColors.textMuted, padding: "0 0.5rem" }}>
              <div>Android Client: v1.0.0</div>
              <div>SDK Target: API 34 (Android 14)</div>
              <div>FGS: Location | Mic | DataSync</div>
            </div>
          </div>
        </aside>

        {/* Central Stage */}
        <main style={{ flex: 1, padding: "2rem", overflowY: "auto", display: "flex", flexDirection: "column", gap: "1.75rem" }}>
          {activeTab === "incidents" && (
            <>
              {/* Active Emergency Banner */}
              {activeIncident.status === "ACTIVE" && (
                <div
                  className="emergency-beacon"
                  style={{
                    backgroundColor: "rgba(201, 42, 50, 0.15)",
                    border: `1.5px solid ${SheVaultColors.emergency}`,
                    borderRadius: "14px",
                    padding: "1.25rem 1.75rem",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: "1.25rem" }}>
                    <div
                      style={{
                        width: 48,
                        height: 48,
                        borderRadius: "50%",
                        backgroundColor: SheVaultColors.emergency,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontSize: "1.5rem",
                      }}
                    >
                      ⚠️
                    </div>
                    <div>
                      <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                        <span style={{ fontSize: "1.2rem", fontWeight: 800, color: "#FFA6AB" }}>
                          CRITICAL EMERGENCY ACTIVE: {activeIncident.user}
                        </span>
                        <span
                          style={{
                            backgroundColor: SheVaultColors.emergency,
                            color: SheVaultColors.white,
                            fontSize: "0.7rem",
                            fontWeight: 800,
                            padding: "0.2rem 0.6rem",
                            borderRadius: "999px",
                            letterSpacing: "0.05em",
                          }}
                        >
                          BROADCASTING SOS
                        </span>
                      </div>
                      <div style={{ fontSize: "0.85rem", color: "#FCA5A5", marginTop: "0.25rem" }}>
                        Activated via <strong>{activeIncident.activationMethod}</strong> • High-Rate GPS Breadcrumbs Streaming
                      </div>
                    </div>
                  </div>

                  <div style={{ display: "flex", gap: "0.75rem" }}>
                    <button
                      onClick={() => alert(`Calling Police Control Room (112) with victim live coordinates: ${activeIncident.coords.lat}, ${activeIncident.coords.lng}`)}
                      style={{
                        backgroundColor: SheVaultColors.emergency,
                        color: SheVaultColors.white,
                        border: "none",
                        padding: "0.7rem 1.25rem",
                        borderRadius: "8px",
                        fontWeight: 700,
                        fontSize: "0.9rem",
                        cursor: "pointer",
                        boxShadow: "0 4px 12px rgba(201, 42, 50, 0.4)",
                      }}
                    >
                      🚨 Dispatch Police (112)
                    </button>
                    <button
                      onClick={() => alert(`SMS Broadcast sent to ${contacts.length} Trusted Circle guardians.`)}
                      style={{
                        backgroundColor: "#1E293B",
                        color: SheVaultColors.white,
                        border: `1px solid ${SheVaultColors.border}`,
                        padding: "0.7rem 1.25rem",
                        borderRadius: "8px",
                        fontWeight: 600,
                        fontSize: "0.9rem",
                        cursor: "pointer",
                      }}
                    >
                      📞 Notify Guardians
                    </button>
                  </div>
                </div>
              )}

              {/* Grid: Incident Live Telemetry & Geospatial Map Card */}
              <div style={{ display: "grid", gridTemplateColumns: "1.2fr 0.8fr", gap: "1.5rem" }}>
                {/* Live Geospatial Tracker */}
                <div
                  style={{
                    backgroundColor: SheVaultColors.surface,
                    borderRadius: "14px",
                    border: `1px solid ${SheVaultColors.border}`,
                    padding: "1.5rem",
                    display: "flex",
                    flexDirection: "column",
                    gap: "1rem",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div>
                      <h3 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Geospatial Live Vector</h3>
                      <p style={{ fontSize: "0.8rem", color: SheVaultColors.textSecondary }}>Fused Location Provider GPS Fix</p>
                    </div>
                    <span
                      style={{
                        fontSize: "0.75rem",
                        padding: "0.25rem 0.65rem",
                        borderRadius: "6px",
                        backgroundColor: "#0F172A",
                        border: "1px solid #334155",
                        color: "#38BDF8",
                        fontWeight: 600,
                      }}
                    >
                      Accuracy: ±{activeIncident.accuracy}m
                    </span>
                  </div>

                  {/* Visual Map Canvas / Radar Deck */}
                  <div
                    style={{
                      height: 320,
                      backgroundColor: "#0B1120",
                      borderRadius: "10px",
                      border: "1px solid #1E293B",
                      position: "relative",
                      overflow: "hidden",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      backgroundImage: `
                        radial-gradient(circle at center, rgba(30, 41, 59, 0.4) 0, transparent 70%),
                        linear-gradient(#1E293B 1px, transparent 1px),
                        linear-gradient(90deg, #1E293B 1px, transparent 1px)
                      `,
                      backgroundSize: "100% 100%, 32px 32px, 32px 32px",
                    }}
                  >
                    {/* Radar Range Rings */}
                    <div style={{ position: "absolute", width: 220, height: 220, borderRadius: "50%", border: "1px dashed rgba(56, 189, 248, 0.2)" }} />
                    <div style={{ position: "absolute", width: 140, height: 140, borderRadius: "50%", border: "1px solid rgba(56, 189, 248, 0.15)" }} />

                    {/* Beacon Pin */}
                    <div style={{ position: "relative", display: "flex", flexDirection: "column", alignItems: "center" }}>
                      <div
                        style={{
                          width: 22,
                          height: 22,
                          borderRadius: "50%",
                          backgroundColor: SheVaultColors.emergency,
                          boxShadow: "0 0 24px rgba(201, 42, 50, 0.9)",
                          border: "3px solid #FFF",
                        }}
                      />
                      <div
                        style={{
                          marginTop: "0.5rem",
                          backgroundColor: "#0F172A",
                          border: `1px solid ${SheVaultColors.border}`,
                          padding: "0.3rem 0.6rem",
                          borderRadius: "6px",
                          fontSize: "0.75rem",
                          fontWeight: 700,
                        }}
                      >
                        📍 {activeIncident.user} ({activeIncident.movement})
                      </div>
                    </div>

                    <div style={{ position: "absolute", bottom: 12, left: 12, fontSize: "0.75rem", color: SheVaultColors.textMuted }}>
                      Lat: {activeIncident.coords.lat.toFixed(6)} | Lng: {activeIncident.coords.lng.toFixed(6)}
                    </div>

                    <div style={{ position: "absolute", bottom: 12, right: 12, fontSize: "0.75rem", color: "#38BDF8" }}>
                      Breadcrumb #{activeIncident.breadcrumbsCount} Ingested
                    </div>
                  </div>
                </div>

                {/* Device Hardware & Telemetry Snapshot Card */}
                <div
                  style={{
                    backgroundColor: SheVaultColors.surface,
                    borderRadius: "14px",
                    border: `1px solid ${SheVaultColors.border}`,
                    padding: "1.5rem",
                    display: "flex",
                    flexDirection: "column",
                    gap: "1.25rem",
                  }}
                >
                  <h3 style={{ fontSize: "1.1rem", fontWeight: 700 }}>Telemetry Policy Snapshot</h3>

                  <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
                    {[
                      { label: "Hardware Motion Classifier", value: activeIncident.movement, tag: "Dynamic Sampling (3s)", color: "#F59E0B" },
                      {
                        label: "Device Battery",
                        value: `${activeIncident.battery}% (${activeIncident.charging ? "Charging" : "Discharging"})`,
                        tag: activeIncident.battery < 20 ? "CRITICAL TIER" : "NORMAL TIER",
                        color: activeIncident.battery < 20 ? SheVaultColors.emergency : SheVaultColors.safe,
                      },
                      { label: "Ambient Audio Evidence", value: activeIncident.audioRecording ? "Capturing (Microphone FGS)" : "Disabled", tag: "FGS Mic", color: "#38BDF8" },
                      { label: "Session Token", value: activeIncident.id, tag: "Room Persisted", color: SheVaultColors.textSecondary },
                      { label: "Coerced Duress Guard", value: "Active (Covert Protocol)", tag: "Zero Client Leak", color: SheVaultColors.safe },
                    ].map((row, idx) => (
                      <div
                        key={idx}
                        style={{
                          backgroundColor: "#0F172A",
                          borderRadius: "8px",
                          padding: "0.75rem 1rem",
                          border: "1px solid #1E293B",
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                        }}
                      >
                        <div>
                          <div style={{ fontSize: "0.75rem", color: SheVaultColors.textSecondary }}>{row.label}</div>
                          <div style={{ fontSize: "0.95rem", fontWeight: 700, color: row.color, marginTop: "0.15rem" }}>{row.value}</div>
                        </div>
                        <span style={{ fontSize: "0.68rem", backgroundColor: "#1E293B", padding: "0.2rem 0.5rem", borderRadius: "4px", color: SheVaultColors.textMuted }}>
                          {row.tag}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </>
          )}

          {activeTab === "contacts" && (
            <div style={{ backgroundColor: SheVaultColors.surface, borderRadius: "14px", border: `1px solid ${SheVaultColors.border}`, padding: "1.75rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
                <div>
                  <h2 style={{ fontSize: "1.3rem", fontWeight: 700 }}>Trusted Circle Contacts</h2>
                  <p style={{ color: SheVaultColors.textSecondary, fontSize: "0.85rem" }}>Configured in Onboarding & Persisted in Room database</p>
                </div>
                <button
                  onClick={() => alert("Add Contact dialog")}
                  style={{
                    backgroundColor: SheVaultColors.primary,
                    color: SheVaultColors.white,
                    border: "none",
                    padding: "0.6rem 1rem",
                    borderRadius: "8px",
                    fontWeight: 600,
                    cursor: "pointer",
                  }}
                >
                  + Add Guardian Contact
                </button>
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "1rem" }}>
                {contacts.map((contact, i) => (
                  <div
                    key={i}
                    style={{
                      backgroundColor: "#0F172A",
                      border: `1px solid ${SheVaultColors.border}`,
                      borderRadius: "10px",
                      padding: "1.2rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.6rem",
                    }}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <span style={{ fontWeight: 700, fontSize: "1.05rem" }}>{contact.name}</span>
                      <span style={{ backgroundColor: "#2E0854", color: "#C4B5FD", fontSize: "0.7rem", padding: "0.2rem 0.5rem", borderRadius: "999px" }}>
                        Priority #{contact.priority}
                      </span>
                    </div>
                    <div style={{ fontSize: "0.85rem", color: SheVaultColors.textSecondary }}>
                      Relation: <strong style={{ color: SheVaultColors.textPrimary }}>{contact.relation}</strong>
                    </div>
                    <div style={{ fontSize: "0.85rem", color: SheVaultColors.textSecondary }}>Phone: {contact.phone}</div>
                    <div style={{ marginTop: "0.4rem", display: "flex", gap: "0.5rem", fontSize: "0.75rem" }}>
                      <span style={{ color: contact.canReceiveLocation ? "#34D399" : "#94A3B8" }}>
                        {contact.canReceiveLocation ? "✓ Location Stream Enabled" : "✕ Location Disabled"}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === "telemetry" && (
            <div style={{ backgroundColor: SheVaultColors.surface, borderRadius: "14px", border: `1px solid ${SheVaultColors.border}`, padding: "1.75rem" }}>
              <h2 style={{ fontSize: "1.3rem", fontWeight: 700, marginBottom: "0.5rem" }}>Adaptive Telemetry Policy Engine</h2>
              <p style={{ color: SheVaultColors.textSecondary, fontSize: "0.85rem", marginBottom: "1.5rem" }}>
                Dynamically tunes GPS, sensor, and upload intervals based on battery temperature, movement velocity, and foreground service lifecycle.
              </p>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "1rem" }}>
                {[
                  {
                    tier: "NORMAL TIER",
                    status: "Active When Battery > 25%",
                    desc: "3-5s GPS Breadcrumbs, high-fidelity accelerometer variance, live WebSocket streaming.",
                    color: SheVaultColors.safe,
                  },
                  {
                    tier: "CONSTRAINED TIER",
                    status: "Active When Battery 10% - 25% or Temp >= 42°C",
                    desc: "15s GPS batching, sensor rate halved, cellular back-off applied.",
                    color: SheVaultColors.warning,
                  },
                  {
                    tier: "CRITICAL TIER",
                    status: "Active When Battery < 10% or Temp >= 48°C",
                    desc: "60-120s GPS bursts, audio paused, SMS emergency fallback payload queued.",
                    color: SheVaultColors.emergency,
                  },
                ].map((tier, idx) => (
                  <div
                    key={idx}
                    style={{
                      backgroundColor: "#0F172A",
                      border: `1.5px solid ${tier.color}`,
                      borderRadius: "10px",
                      padding: "1.25rem",
                      display: "flex",
                      flexDirection: "column",
                      gap: "0.75rem",
                    }}
                  >
                    <div style={{ fontWeight: 800, color: tier.color, fontSize: "1.1rem" }}>{tier.tier}</div>
                    <div style={{ fontSize: "0.78rem", color: "#94A3B8" }}>{tier.status}</div>
                    <p style={{ fontSize: "0.85rem", color: SheVaultColors.textSecondary, lineHeight: 1.4 }}>{tier.desc}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === "raw" && (
            <div style={{ backgroundColor: SheVaultColors.surface, borderRadius: "14px", border: `1px solid ${SheVaultColors.border}`, padding: "1.75rem" }}>
              <h2 style={{ fontSize: "1.3rem", fontWeight: 700, marginBottom: "0.5rem" }}>Local Room Incident Journal Timeline</h2>
              <p style={{ color: SheVaultColors.textSecondary, fontSize: "0.85rem", marginBottom: "1.5rem" }}>
                Forensic event timeline reconstructed from Room database table <code>incident_events</code>.
              </p>

              <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                {[
                  { event: "INCIDENT_CREATED", ts: "17:42:10.104", payload: '{"method": "HOLD_SOS_BUTTON", "battery": 68}' },
                  { event: "LOCATION_CAPTURED", ts: "17:42:10.820", payload: '{"lat": 28.6139, "lng": 77.2090, "accuracy": 3.4}' },
                  { event: "ESCALATION_ATTEMPT", ts: "17:42:15.000", payload: '{"graceExpired": true, "dispatching": "SERVER_REST"}' },
                  { event: "ACK_RECEIVED", ts: "17:42:15.420", payload: '{"dispatchId": "DISP-9812", "server": "200 OK"}' },
                  { event: "LOCATION_CAPTURED", ts: "17:42:18.000", payload: '{"movement": "RUNNING", "speed": 4.1}' },
                ].map((item, i) => (
                  <div
                    key={i}
                    style={{
                      backgroundColor: "#0F172A",
                      padding: "0.85rem 1.25rem",
                      borderRadius: "8px",
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      fontFamily: "monospace",
                      fontSize: "0.85rem",
                      border: "1px solid #1E293B",
                    }}
                  >
                    <div style={{ display: "flex", gap: "1.5rem", alignItems: "center" }}>
                      <span style={{ color: "#38BDF8" }}>{item.ts}</span>
                      <strong style={{ color: SheVaultColors.emergency }}>{item.event}</strong>
                    </div>
                    <code style={{ color: "#94A3B8" }}>{item.payload}</code>
                  </div>
                ))}
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
