export interface UserProfile {
  id: string;
  name: string;
  phone: string;
  email: string;
  hasSafePin: boolean;
  hasDuressPin: boolean;
}

export interface TrustedContact {
  id: string;
  name: string;
  phone: string;
  relationship: string;
  priority: "PRIMARY" | "SECONDARY" | "FALLBACK";
  canReceiveLocation: boolean;
  canReceiveIncidentUpdates: boolean;
}

export interface IncidentSession {
  id: string;
  sessionId: string;
  userId: string;
  status: "STARTING" | "ACTIVE_GRACE" | "ACTIVE" | "SAFE_CANCELLED" | "COERCED_DURESS" | "ESCALATED";
  activationMethod: "HOLD" | "PANIC_GESTURE" | "CHECK_IN_ESCALATION" | "VOICE";
  startedAt: string;
  battery: number;
  latitude: number;
  longitude: number;
  accuracy: number;
  movementMode: "STATIONARY" | "WALKING" | "RUNNING" | "VEHICLE";
  isDuress: boolean;
}

export interface SafetyCheckIn {
  id: string;
  title: string;
  destination?: string;
  expectedAt: string;
  status: "ACTIVE" | "COMPLETED" | "OVERDUE" | "CANCELLED";
}

const API_BASE = "http://127.0.0.1:8000/api/v1";

class ApiService {
  private token: string | null = localStorage.getItem("shevault_jwt");

  setToken(token: string | null) {
    this.token = token;
    if (token) localStorage.setItem("shevault_jwt", token);
    else localStorage.removeItem("shevault_jwt");
  }

  getToken() {
    return this.token;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<{ success: boolean; data?: T; message?: string }> {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      ...(options.headers as Record<string, string>),
    };
    if (this.token) {
      headers["Authorization"] = `Bearer ${this.token}`;
    }

    try {
      const res = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers,
      });
      const data = await res.json();
      if (!res.ok) {
        return { success: false, message: data?.error?.message || `HTTP ${res.status}` };
      }
      return data;
    } catch (e: any) {
      return { success: false, message: e.message || "Network Error" };
    }
  }

  async login(email: string, password: string) {
    const res = await this.request<{ access_token: string; refresh_token: string }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    if (res.success && res.data?.access_token) {
      this.setToken(res.data.access_token);
    }
    return res;
  }

  async register(name: string, phone: string, email: string, password: string) {
    const res = await this.request<{ access_token: string; refresh_token: string }>("/auth/register", {
      method: "POST",
      body: JSON.stringify({ name, phone, email, password }),
    });
    if (res.success && res.data?.access_token) {
      this.setToken(res.data.access_token);
    }
    return res;
  }

  async getMe() {
    return this.request<UserProfile>("/auth/me");
  }

  async setupPins(safePin: string, duressPin: string) {
    return this.request<boolean>("/auth/pins", {
      method: "POST",
      body: JSON.stringify({ safe_pin: safePin, duress_pin: duressPin }),
    });
  }

  async listIncidents() {
    return this.request<IncidentSession[]>("/incidents");
  }

  async createIncident(session: Partial<IncidentSession>) {
    return this.request<{ incident_id: string; status: string }>("/incidents", {
      method: "POST",
      headers: { "Idempotency-Key": session.sessionId || `SES-${Date.now()}` },
      body: JSON.stringify({
        client_session_id: session.sessionId || `SES-${Date.now()}`,
        activation_method: session.activationMethod || "HOLD",
        started_at: new Date().toISOString(),
        battery: session.battery || 88,
        location: {
          latitude: session.latitude || 28.6139,
          longitude: session.longitude || 77.2090,
          accuracy: session.accuracy || 3.5,
        },
      }),
    });
  }

  async cancelIncident(incidentId: string, pin: string) {
    return this.request<{ status: string }>(`/incidents/${incidentId}/cancel`, {
      method: "POST",
      body: JSON.stringify({ pin }),
    });
  }

  async duressIncident(incidentId: string, pin: string) {
    return this.request<{ status: string }>(`/incidents/${incidentId}/duress`, {
      method: "POST",
      body: JSON.stringify({ pin }),
    });
  }

  async listContacts() {
    return this.request<TrustedContact[]>("/contacts");
  }

  async createContact(contact: Partial<TrustedContact>) {
    return this.request<TrustedContact>("/contacts", {
      method: "POST",
      body: JSON.stringify({
        name: contact.name,
        relationship: contact.relationship,
        phone: contact.phone,
        priority: contact.priority || "PRIMARY",
        permissions: {
          incident_alerts: contact.canReceiveIncidentUpdates ?? true,
          location: contact.canReceiveLocation ?? true,
          battery: true,
          evidence: false,
        },
      }),
    });
  }

  async deleteContact(contactId: string) {
    return this.request<boolean>(`/contacts/${contactId}`, {
      method: "DELETE",
    });
  }

  async listCheckIns() {
    return this.request<SafetyCheckIn[]>("/checkins");
  }

  async createCheckIn(title: string, destination: string, expectedMinutes: number) {
    const expectedAt = new Date(Date.now() + expectedMinutes * 60000).toISOString();
    return this.request<SafetyCheckIn>("/checkins", {
      method: "POST",
      body: JSON.stringify({
        title,
        destination,
        expected_at: expectedAt,
      }),
    });
  }

  async completeCheckIn(id: string) {
    return this.request<SafetyCheckIn>(`/checkins/${id}/complete`, {
      method: "POST",
    });
  }
}

export const api = new ApiService();
