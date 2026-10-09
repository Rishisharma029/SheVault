import { useState, useEffect, useRef } from "react";
import {
  SheVaultColors,
  BrandTokens,
  LightNeutralTokens,
  SafeTokens,
  EmergencyTokens,
} from "./tokens";
import { api } from "./api";
import type { UserProfile, TrustedContact, IncidentSession, SafetyCheckIn } from "./api";
import {
  Shield,
  ShieldAlert,
  Users,
  Compass,
  Clock,
  History,
  Lock,
  EyeOff,
  Settings,
  AlertTriangle,
  CheckCircle,
  Plus,
  Trash2,
  Radio,
} from "lucide-react";

export function App() {
  // Navigation tabs
  const [currentTab, setCurrentTab] = useState<
    | "onboarding"
    | "home"
    | "sos"
    | "incident"
    | "trusted-circle"
    | "safe-route"
    | "check-in"
    | "history"
    | "discreet"
    | "settings"
  >("home");

  // Authentication & Profile state
  const [user, setUser] = useState<UserProfile | null>(null);

  // App domain states
  const [contacts, setContacts] = useState<TrustedContact[]>([]);
  const [incidents, setIncidents] = useState<IncidentSession[]>([]);
  const [checkIns, setCheckIns] = useState<SafetyCheckIn[]>([]);
  const [activeIncident, setActiveIncident] = useState<IncidentSession | null>(null);

  // SOS hold timer state
  const [isHolding, setIsHolding] = useState(false);
  const [holdProgress, setHoldProgress] = useState(0);
  const holdIntervalRef = useRef<any>(null);

  // Discreet Calculator mode
  const [calcInput, setCalcInput] = useState("0");

  // PIN entry dialog state
  const [showPinModal, setShowPinModal] = useState(false);
  const [pinInput, setPinInput] = useState("");

  // Load session & live data
  useEffect(() => {
    bootstrapSession();
  }, []);

  const bootstrapSession = async () => {
    const meRes = await api.getMe();
    if (meRes.success && meRes.data) {
      setUser(meRes.data);
      loadAppData();
    } else {
      // Auto-register/login demo user on localhost
      const loginRes = await api.login("rishi@example.com", "SafeVaultPass@2026");
      if (loginRes.success) {
        const u = await api.getMe();
        if (u.data) setUser(u.data);
        loadAppData();
      } else {
        // Create demo account
        const regRes = await api.register("Rishi Sharma", "+919876543210", "rishi@example.com", "SafeVaultPass@2026");
        if (regRes.success) {
          await api.setupPins("1234", "9999");
          const u = await api.getMe();
          if (u.data) setUser(u.data);
          loadAppData();
        }
      }
    }
  };

  const loadAppData = async () => {
    const [c, inc, chk] = await Promise.all([
      api.listContacts(),
      api.listIncidents(),
      api.listCheckIns(),
    ]);
    if (c.data) setContacts(c.data);
    if (inc.data) setIncidents(inc.data);
    if (chk.data) setCheckIns(chk.data);
  };

  // SOS Trigger Mechanics
  const startHold = () => {
    setIsHolding(true);
    setHoldProgress(0);
    const startTime = Date.now();
    holdIntervalRef.current = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(100, (elapsed / 1350) * 100);
      setHoldProgress(progress);
      if (progress >= 100) {
        clearInterval(holdIntervalRef.current);
        triggerSosSession("HOLD");
      }
    }, 30);
  };

  const endHold = () => {
    setIsHolding(false);
    setHoldProgress(0);
    if (holdIntervalRef.current) clearInterval(holdIntervalRef.current);
  };

  const triggerSosSession = async (method: "HOLD" | "PANIC_GESTURE") => {
    setIsHolding(false);
    setHoldProgress(0);
    const newSession: IncidentSession = {
      id: `INC-${Date.now()}`,
      sessionId: `SES-${Date.now()}`,
      userId: user?.id || "demo-user",
      status: "ACTIVE",
      activationMethod: method,
      startedAt: new Date().toLocaleTimeString(),
      battery: 88,
      latitude: 28.6139,
      longitude: 77.2090,
      accuracy: 3.2,
      movementMode: "WALKING",
      isDuress: false,
    };
    setActiveIncident(newSession);
    setCurrentTab("incident");

    // Cloud call
    await api.createIncident(newSession);
    loadAppData();
  };

  // Disarm / Duress Handlers
  const handlePinSubmit = async () => {
    if (!activeIncident) return;
    if (pinInput === "9999") {
      // Duress PIN -> Silent covert distress
      await api.duressIncident(activeIncident.id, pinInput);
      setShowPinModal(false);
      setPinInput("");
      setActiveIncident(null);
      setCurrentTab("home");
    } else {
      // Safe PIN -> Clean resolve
      await api.cancelIncident(activeIncident.id, pinInput);
      setShowPinModal(false);
      setPinInput("");
      setActiveIncident(null);
      setCurrentTab("home");
    }
    loadAppData();
  };

  // Discreet calculator logic
  const handleCalcBtn = (val: string) => {
    if (val === "C") setCalcInput("0");
    else if (val === "=") {
      if (calcInput === "9999") {
        // Covert trigger from discreet calculator!
        triggerSosSession("HOLD");
      } else {
        try {
          setCalcInput(String(Function(`'use strict'; return (${calcInput})`)()));
        } catch {
          setCalcInput("0");
        }
      }
    } else {
      setCalcInput((prev) => (prev === "0" ? val : prev + val));
    }
  };

  // Render Discreet Mode Screen
  if (currentTab === "discreet") {
    return (
      <div style={{ minHeight: "100vh", backgroundColor: "#1C1917", color: "#FFF", padding: 24, display: "flex", flexDirection: "column", maxWidth: 460, margin: "0 auto" }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 20 }}>
          <span style={{ fontSize: 13, color: "#78716C" }}>Calculator</span>
          <button onClick={() => setCurrentTab("home")} style={{ background: "none", color: "#A8A29E", fontSize: 12 }}>
            Exit
          </button>
        </div>
        <div style={{ flex: 1, display: "flex", alignItems: "flex-end", justifyContent: "flex-end", fontSize: 44, padding: "20px 0" }}>
          {calcInput}
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 12 }}>
          {["C", "+/-", "%", "/", "7", "8", "9", "*", "4", "5", "6", "-", "1", "2", "3", "+", "0", ".", "="].map((btn) => (
            <button
              key={btn}
              onClick={() => handleCalcBtn(btn)}
              style={{
                height: 64,
                borderRadius: 32,
                backgroundColor: ["/", "*", "-", "+", "="].includes(btn) ? BrandTokens.rose : "#292524",
                color: "#FFF",
                fontWeight: 600,
                fontSize: 20,
              }}
            >
              {btn}
            </button>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: "100vh", backgroundColor: LightNeutralTokens.background, color: LightNeutralTokens.textPrimary, display: "flex", flexDirection: "column", maxWidth: 500, margin: "0 auto", borderLeft: `1px solid ${LightNeutralTokens.border}`, borderRight: `1px solid ${LightNeutralTokens.border}` }}>
      {/* Top Header */}
      <header style={{ padding: "16px 20px", display: "flex", alignItems: "center", justifyContent: "space-between", borderBottom: `1px solid ${LightNeutralTokens.border}`, backgroundColor: LightNeutralTokens.surface }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ width: 34, height: 34, borderRadius: 10, backgroundColor: BrandTokens.primary, display: "flex", alignItems: "center", justifyContent: "center", color: "#FFF" }}>
            <Shield size={20} />
          </div>
          <div>
            <h1 style={{ fontSize: 18, fontWeight: 700, color: BrandTokens.primary }}>SheVault</h1>
            <p style={{ fontSize: 11, color: LightNeutralTokens.textSecondary }}>Autonomous Personal Safety</p>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <button onClick={() => setCurrentTab("discreet")} style={{ background: "none", color: LightNeutralTokens.textSecondary, display: "flex", alignItems: "center", gap: 4, fontSize: 12 }}>
            <EyeOff size={16} /> Discreet
          </button>
          <button onClick={() => setCurrentTab("settings")} style={{ background: "none", color: LightNeutralTokens.textSecondary }}>
            <Settings size={18} />
          </button>
        </div>
      </header>

      {/* Main Tab Area */}
      <main style={{ flex: 1, padding: 20, overflowY: "auto" }}>
        {/* ================= TAB: HOME ================= */}
        {currentTab === "home" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 24, alignItems: "center" }}>
            {/* Protection Badge */}
            <div style={{ width: "100%", padding: "14px 18px", borderRadius: 16, backgroundColor: SafeTokens.surface, border: `1px solid ${SafeTokens.default}33`, display: "flex", alignItems: "center", gap: 12 }}>
              <CheckCircle size={22} color={SafeTokens.default} />
              <div>
                <p style={{ fontWeight: 600, fontSize: 14, color: SafeTokens.default }}>You're Protected</p>
                <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>All sensors active • {contacts.length} contacts reachable</p>
              </div>
            </div>

            {/* SOS Primary Button */}
            <div style={{ margin: "24px 0", textAlign: "center" }}>
              <div
                onMouseDown={startHold}
                onMouseUp={endHold}
                onTouchStart={startHold}
                onTouchEnd={endHold}
                style={{
                  width: 200,
                  height: 200,
                  borderRadius: "50%",
                  backgroundColor: isHolding ? EmergencyTokens.default : SheVaultColors.emergency,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                  color: "#FFF",
                  boxShadow: `0 12px 32px ${EmergencyTokens.default}55`,
                  position: "relative",
                  userSelect: "none",
                  transform: isHolding ? "scale(0.96)" : "scale(1)",
                  transition: "transform 0.1s ease",
                }}
              >
                {/* SVG Progress Ring */}
                <svg style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", transform: "rotate(-90deg)" }}>
                  <circle cx="100" cy="100" r="92" stroke="rgba(255,255,255,0.2)" strokeWidth="6" fill="transparent" />
                  <circle
                    cx="100"
                    cy="100"
                    r="92"
                    stroke="#FFF"
                    strokeWidth="6"
                    fill="transparent"
                    strokeDasharray={2 * Math.PI * 92}
                    strokeDashoffset={2 * Math.PI * 92 - (holdProgress / 100) * (2 * Math.PI * 92)}
                    strokeLinecap="round"
                  />
                </svg>

                <AlertTriangle size={42} style={{ marginBottom: 6 }} />
                <span style={{ fontSize: 26, fontWeight: 900, letterSpacing: 2 }}>SOS</span>
                <span style={{ fontSize: 11, opacity: 0.9, marginTop: 4 }}>
                  {isHolding ? "HOLD TIGHT..." : "HOLD 1.35s"}
                </span>
              </div>
            </div>

            {/* Quick Action Cards */}
            <div style={{ width: "100%", display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
              <button
                onClick={() => setCurrentTab("trusted-circle")}
                style={{
                  padding: "16px 10px",
                  borderRadius: 14,
                  backgroundColor: LightNeutralTokens.surface,
                  border: `1px solid ${LightNeutralTokens.border}`,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                <Users size={20} color={BrandTokens.primary} />
                <span style={{ fontSize: 12, fontWeight: 600 }}>Circle ({contacts.length})</span>
              </button>

              <button
                onClick={() => setCurrentTab("safe-route")}
                style={{
                  padding: "16px 10px",
                  borderRadius: 14,
                  backgroundColor: LightNeutralTokens.surface,
                  border: `1px solid ${LightNeutralTokens.border}`,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                <Compass size={20} color={BrandTokens.rose} />
                <span style={{ fontSize: 12, fontWeight: 600 }}>Safe Route</span>
              </button>

              <button
                onClick={() => setCurrentTab("check-in")}
                style={{
                  padding: "16px 10px",
                  borderRadius: 14,
                  backgroundColor: LightNeutralTokens.surface,
                  border: `1px solid ${LightNeutralTokens.border}`,
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                <Clock size={20} color={BrandTokens.peach} />
                <span style={{ fontSize: 12, fontWeight: 600 }}>Check-In</span>
              </button>
            </div>

            {/* Secondary Activation: Panic Gesture Simulation */}
            <div style={{ width: "100%", padding: 14, borderRadius: 14, backgroundColor: BrandTokens.primaryTint, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <Radio size={18} color={BrandTokens.primary} />
                <span style={{ fontSize: 13, fontWeight: 500, color: BrandTokens.primaryDark }}>4-Tap Panic Gesture</span>
              </div>
              <button
                onClick={() => triggerSosSession("PANIC_GESTURE")}
                style={{
                  padding: "6px 14px",
                  borderRadius: 8,
                  backgroundColor: BrandTokens.primary,
                  color: "#FFF",
                  fontSize: 12,
                  fontWeight: 600,
                }}
              >
                Simulate 4 Taps
              </button>
            </div>
          </div>
        )}

        {/* ================= TAB: ACTIVE INCIDENT ================= */}
        {currentTab === "incident" && activeIncident && (
          <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            <div style={{ padding: 18, borderRadius: 16, backgroundColor: EmergencyTokens.surface, border: `1px solid ${SheVaultColors.emergency}44` }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
                <span style={{ fontSize: 12, fontWeight: 700, color: SheVaultColors.emergency, display: "flex", alignItems: "center", gap: 6 }}>
                  <ShieldAlert size={16} /> EMERGENCY BROADCAST ACTIVE
                </span>
                <span style={{ fontSize: 11, color: LightNeutralTokens.textSecondary }}>{activeIncident.startedAt}</span>
              </div>
              <p style={{ fontSize: 14, fontWeight: 600, marginBottom: 8 }}>
                Telemetry & location dispatching to 3 emergency contacts and cloud dispatch.
              </p>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, fontSize: 12 }}>
                <div>📍 GPS: 28.6139° N, 77.2090° E</div>
                <div>🔋 Battery: 88% (Discharging)</div>
                <div>📡 Accuracy: ± 3.2m</div>
                <div>🚶 Mode: Walking</div>
              </div>
            </div>

            {/* Disarm / Cancel Options */}
            <div style={{ marginTop: 20, display: "flex", flexDirection: "column", gap: 12 }}>
              <button
                onClick={() => setShowPinModal(true)}
                style={{
                  width: "100%",
                  padding: 16,
                  borderRadius: 14,
                  backgroundColor: SafeTokens.default,
                  color: "#FFF",
                  fontWeight: 700,
                  fontSize: 15,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: 8,
                }}
              >
                <CheckCircle size={20} /> I'm Safe (Enter PIN)
              </button>
            </div>
          </div>
        )}

        {/* ================= TAB: TRUSTED CIRCLE ================= */}
        {currentTab === "trusted-circle" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <h2 style={{ fontSize: 16, fontWeight: 700 }}>Emergency Contacts</h2>
              <button
                onClick={async () => {
                  await api.createContact({ name: "Sister", relationship: "Sibling", phone: "+919876500005", priority: "PRIMARY" });
                  loadAppData();
                }}
                style={{ padding: "6px 12px", borderRadius: 8, backgroundColor: BrandTokens.primary, color: "#FFF", fontSize: 12, display: "flex", alignItems: "center", gap: 4 }}
              >
                <Plus size={14} /> Add Contact
              </button>
            </div>

            {contacts.map((c) => (
              <div key={c.id} style={{ padding: 14, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}`, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <p style={{ fontWeight: 600, fontSize: 14 }}>{c.name} ({c.relationship})</p>
                  <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>{c.phone} • {c.priority}</p>
                </div>
                <button onClick={async () => { await api.deleteContact(c.id); loadAppData(); }} style={{ background: "none", color: EmergencyTokens.default }}>
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* ================= TAB: CHECK-IN ================= */}
        {currentTab === "check-in" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700 }}>Safety Check-Ins</h2>
            <div style={{ padding: 16, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}` }}>
              <p style={{ fontSize: 13, fontWeight: 600, marginBottom: 8 }}>Schedule Safety Timer</p>
              <button
                onClick={async () => {
                  await api.createCheckIn("Walking back from library", "Hostel 4", 30);
                  loadAppData();
                }}
                style={{ width: "100%", padding: 12, borderRadius: 10, backgroundColor: BrandTokens.primary, color: "#FFF", fontWeight: 600, fontSize: 13 }}
              >
                Start 30-Min Walk Check-In
              </button>
            </div>

            {checkIns.map((ch) => (
              <div key={ch.id} style={{ padding: 14, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}`, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <p style={{ fontWeight: 600, fontSize: 14 }}>{ch.title}</p>
                  <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>Expected: {new Date(ch.expectedAt).toLocaleTimeString()}</p>
                </div>
                <button
                  onClick={async () => { await api.completeCheckIn(ch.id); loadAppData(); }}
                  style={{ padding: "6px 12px", borderRadius: 8, backgroundColor: SafeTokens.surface, color: SafeTokens.default, fontWeight: 600, fontSize: 12 }}
                >
                  I've Arrived
                </button>
              </div>
            ))}
          </div>
        )}

        {/* ================= TAB: SAFE ROUTE ================= */}
        {currentTab === "safe-route" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700 }}>Safe Route Navigation</h2>
            <div style={{ padding: 16, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}` }}>
              <p style={{ fontSize: 13, color: LightNeutralTokens.textSecondary, marginBottom: 12 }}>
                Active safety corridors with verified streetlights and police outpost waypoints.
              </p>
              <div style={{ height: 160, borderRadius: 10, backgroundColor: "#E5E7EB", display: "flex", alignItems: "center", justifyContent: "center", color: "#6B7280", fontSize: 13 }}>
                🗺️ Verified Illuminated Corridors (Online)
              </div>
            </div>
          </div>
        )}

        {/* ================= TAB: HISTORY ================= */}
        {currentTab === "history" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700 }}>Incident Journal</h2>
            {incidents.length === 0 ? (
              <p style={{ fontSize: 13, color: LightNeutralTokens.textSecondary }}>No past incidents logged.</p>
            ) : (
              incidents.map((i) => (
                <div key={i.id} style={{ padding: 14, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}` }}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
                    <span style={{ fontWeight: 600, fontSize: 14 }}>{i.activationMethod}</span>
                    <span style={{ fontSize: 12, color: i.status === "ACTIVE" ? SheVaultColors.emergency : SafeTokens.default, fontWeight: 600 }}>{i.status}</span>
                  </div>
                  <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>{i.startedAt} • Battery: {i.battery}%</p>
                </div>
              ))
            )}
          </div>
        )}

        {/* ================= TAB: SETTINGS ================= */}
        {currentTab === "settings" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700 }}>Security & Hardware PINs</h2>
            <div style={{ padding: 16, borderRadius: 14, backgroundColor: LightNeutralTokens.surface, border: `1px solid ${LightNeutralTokens.border}`, display: "flex", flexDirection: "column", gap: 10 }}>
              <div>
                <p style={{ fontSize: 13, fontWeight: 600 }}>Safe Disarm PIN</p>
                <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>Configured (Default: 1234)</p>
              </div>
              <hr style={{ border: "none", borderTop: `1px solid ${LightNeutralTokens.divider}` }} />
              <div>
                <p style={{ fontSize: 13, fontWeight: 600 }}>Covert Duress PIN</p>
                <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary }}>Configured (Default: 9999) — Signals silent distress</p>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Disarm PIN Dialog */}
      {showPinModal && (
        <div style={{ position: "fixed", inset: 0, backgroundColor: "rgba(0,0,0,0.6)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100, padding: 20 }}>
          <div style={{ width: "100%", maxWidth: 360, backgroundColor: LightNeutralTokens.surface, borderRadius: 20, padding: 24, textAlign: "center" }}>
            <Lock size={32} color={BrandTokens.primary} style={{ marginBottom: 12 }} />
            <h3 style={{ fontSize: 18, fontWeight: 700, marginBottom: 8 }}>Authentication</h3>
            <p style={{ fontSize: 12, color: LightNeutralTokens.textSecondary, marginBottom: 16 }}>
              Enter your 4-digit PIN to disarm protection.
            </p>
            <input
              type="password"
              maxLength={4}
              value={pinInput}
              onChange={(e) => setPinInput(e.target.value)}
              placeholder="••••"
              style={{ width: 140, fontSize: 28, textAlign: "center", letterSpacing: 8, padding: "8px 12px", borderRadius: 10, border: `1px solid ${LightNeutralTokens.border}`, marginBottom: 20 }}
            />
            <div style={{ display: "flex", gap: 10 }}>
              <button onClick={() => setShowPinModal(false)} style={{ flex: 1, padding: 12, borderRadius: 10, backgroundColor: LightNeutralTokens.divider }}>
                Cancel
              </button>
              <button onClick={handlePinSubmit} style={{ flex: 1, padding: 12, borderRadius: 10, backgroundColor: BrandTokens.primary, color: "#FFF", fontWeight: 700 }}>
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bottom Navigation */}
      <nav style={{ display: "flex", justifyContent: "space-around", padding: "12px 6px", borderTop: `1px solid ${LightNeutralTokens.border}`, backgroundColor: LightNeutralTokens.surface }}>
        {[
          { tab: "home", label: "Home", icon: Shield },
          { tab: "trusted-circle", label: "Circle", icon: Users },
          { tab: "history", label: "Journal", icon: History },
          { tab: "settings", label: "Settings", icon: Settings },
        ].map(({ tab, label, icon: Icon }) => (
          <button
            key={tab}
            onClick={() => setCurrentTab(tab as any)}
            style={{
              background: "none",
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 4,
              color: currentTab === tab ? BrandTokens.primary : LightNeutralTokens.textSecondary,
              fontWeight: currentTab === tab ? 700 : 500,
              fontSize: 11,
            }}
          >
            <Icon size={20} />
            {label}
          </button>
        ))}
      </nav>
    </div>
  );
}
export default App;
