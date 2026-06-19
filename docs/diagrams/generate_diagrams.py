"""Generates OrderFlow architecture PNGs with matplotlib (no external tooling)."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import os

OUT = os.path.dirname(__file__)
plt.rcParams["font.family"] = "DejaVu Sans"

# palette: (fill, edge, text)
C = {
    "client": ("#ede9fe", "#7c3aed", "#4c1d95"),
    "sec":    ("#fee2e2", "#dc2626", "#7f1d1d"),
    "web":    ("#dbeafe", "#2563eb", "#1e3a8a"),
    "svc":    ("#dcfce7", "#16a34a", "#14532d"),
    "repo":   ("#fef3c7", "#d97706", "#78350f"),
    "db":     ("#ede9fe", "#7c3aed", "#4c1d95"),
    "ext":    ("#f1f5f9", "#64748b", "#334155"),
    "happy":  ("#dcfce7", "#16a34a", "#14532d"),
    "bad":    ("#fee2e2", "#dc2626", "#7f1d1d"),
    "done":   ("#dbeafe", "#2563eb", "#1e3a8a"),
}

def box(ax, x, y, w, h, text, kind, fs=11, bold=True):
    fill, edge, tc = C[kind]
    p = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.08",
                       linewidth=1.8, edgecolor=edge, facecolor=fill, zorder=2)
    ax.add_patch(p)
    ax.text(x + w/2, y + h/2, text, ha="center", va="center", fontsize=fs,
            color=tc, fontweight="bold" if bold else "normal", zorder=3)

def band(ax, x, y, w, h, label, edge):
    p = FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.02,rounding_size=0.06",
                       linewidth=1.4, edgecolor=edge, facecolor="none",
                       linestyle=(0, (4, 3)), zorder=1)
    ax.add_patch(p)
    ax.text(x + 0.15, y + h - 0.28, label, ha="left", va="center",
            fontsize=10.5, color=edge, fontweight="bold", zorder=3)

def arrow(ax, p1, p2, color="#64748b", style="-|>", dashed=False):
    a = FancyArrowPatch(p1, p2, arrowstyle=style, mutation_scale=16,
                        linewidth=1.6, color=color, zorder=1,
                        linestyle="--" if dashed else "-",
                        shrinkA=2, shrinkB=2)
    ax.add_patch(a)

# ============================================================
# 1. Layered architecture
# ============================================================
fig, ax = plt.subplots(figsize=(11, 12))
ax.set_xlim(0, 12); ax.set_ylim(0, 14); ax.axis("off")
ax.text(6, 13.5, "OrderFlow — Layered Architecture", ha="center",
        fontsize=17, fontweight="bold", color="#0f172a")

box(ax, 4.5, 12.3, 3, 0.7, "Client / Frontend", "client", fs=12)

band(ax, 0.5, 11.0, 11, 0.95, "SECURITY FILTER CHAIN", "#dc2626")
box(ax, 4.0, 11.15, 4, 0.55, "JwtAuthenticationFilter", "sec", fs=10.5)

band(ax, 0.5, 9.0, 11, 1.6, "WEB LAYER — Controllers", "#2563eb")
for i, t in enumerate(["OrderController", "PaymentController", "ProductController", "DocumentController"]):
    box(ax, 0.8 + i*2.75, 9.25, 2.5, 0.7, t, "web", fs=9.5)

band(ax, 0.5, 6.6, 11, 2.0, "SERVICE LAYER — Business Logic", "#16a34a")
svcs = ["OrderService", "OrderStateMachine", "ProductService", "AuditService", "PaymentService"]
for i, t in enumerate(svcs):
    box(ax, 0.75 + i*2.2, 7.55, 2.0, 0.7, t, "svc", fs=8.8)
ax.text(6, 6.95, "@Transactional · server-authoritative pricing · audit on every change",
        ha="center", fontsize=9, color="#14532d", style="italic")

band(ax, 0.5, 4.6, 11, 1.6, "REPOSITORY LAYER — Spring Data JPA", "#d97706")
for i, t in enumerate(["OrderRepository", "ProductRepository", "AuditLogRepository"]):
    box(ax, 1.0 + i*3.5, 4.85, 3.0, 0.7, t, "repo", fs=9.5)

box(ax, 4.0, 2.9, 4, 0.8, "H2  /  PostgreSQL", "db", fs=12)
box(ax, 0.7, 2.95, 2.7, 0.7, "Stripe", "ext", fs=10)
box(ax, 8.6, 2.95, 2.7, 0.7, "Firebase  /  AWS S3", "ext", fs=10)

# arrows between layers
arrow(ax, (6, 12.3), (6, 11.95))
arrow(ax, (6, 11.0), (6, 10.6))
arrow(ax, (6, 9.0), (6, 8.6))
arrow(ax, (6, 6.6), (6, 6.2))
arrow(ax, (6, 4.6), (6, 3.7))
arrow(ax, (4.0, 7.9), (3.4, 3.3), color="#94a3b8", dashed=True)   # service -> stripe
arrow(ax, (8.6, 7.9), (9.9, 3.65), color="#94a3b8", dashed=True)  # service -> ext

ax.text(6, 0.6,
        "Request flow: Client → JWT filter → Controller (thin) → Service (logic + tx) → Repository → DB",
        ha="center", fontsize=10, color="#475569")
plt.savefig(os.path.join(OUT, "layered-architecture.png"), dpi=170, bbox_inches="tight",
            facecolor="white")
plt.close(fig)

# ============================================================
# 2. Order lifecycle state machine
# ============================================================
fig, ax = plt.subplots(figsize=(13, 6.5))
ax.set_xlim(0, 14); ax.set_ylim(0, 7); ax.axis("off")
ax.text(7, 6.6, "OrderFlow — Order Lifecycle (State Machine)", ha="center",
        fontsize=17, fontweight="bold", color="#0f172a")

main = ["CREATED", "PENDING_PAYMENT", "PAID", "PROCESSING", "COMPLETED"]
xs = [0.4, 3.0, 6.0, 8.4, 11.2]
w = [2.0, 2.6, 1.7, 2.3, 2.0]
ymain = 4.0
pos = {}
for i, s in enumerate(main):
    kind = "done" if s == "COMPLETED" else "happy"
    box(ax, xs[i], ymain, w[i], 0.9, s, kind, fs=10.5)
    pos[s] = (xs[i], xs[i] + w[i], ymain)

for i in range(len(main) - 1):
    arrow(ax, (pos[main[i]][1], ymain + 0.45), (pos[main[i+1]][0], ymain + 0.45), color="#16a34a")

# terminal bad states
box(ax, 4.0, 1.4, 2.0, 0.85, "CANCELLED", "bad", fs=10.5)
box(ax, 8.0, 1.4, 2.0, 0.85, "FAILED", "bad", fs=10.5)
arrow(ax, (1.4, ymain), (4.6, 2.25), color="#dc2626", dashed=True)
arrow(ax, (4.3, ymain), (5.0, 2.25), color="#dc2626", dashed=True)
arrow(ax, (4.3, ymain), (8.6, 2.25), color="#dc2626", dashed=True)
arrow(ax, (9.0, ymain), (9.0, 2.25), color="#dc2626", dashed=True)

ax.text(7, 0.5, "Only explicitly-allowed transitions are permitted — "
        "illegal jumps (e.g. CREATED → COMPLETED) are rejected by OrderStateMachine.",
        ha="center", fontsize=10.5, color="#475569", style="italic")
plt.savefig(os.path.join(OUT, "order-lifecycle.png"), dpi=170, bbox_inches="tight",
            facecolor="white")
plt.close(fig)

# ============================================================
# 3. Payment flow (sequence-style swimlanes)
# ============================================================
fig, ax = plt.subplots(figsize=(12, 7))
ax.set_xlim(0, 12); ax.set_ylim(0, 8); ax.axis("off")
ax.text(6, 7.6, "OrderFlow — Payment Flow (async + webhook verification)",
        ha="center", fontsize=16, fontweight="bold", color="#0f172a")

lanes = [("Client", 1.5, "client"), ("PaymentController", 4.5, "web"),
         ("OrderService", 7.5, "svc"), ("Stripe", 10.3, "ext")]
for name, x, kind in lanes:
    box(ax, x - 1.1, 6.5, 2.2, 0.6, name, kind, fs=10)
    ax.plot([x, x], [0.6, 6.5], color="#cbd5e1", linewidth=1.2, zorder=0)

def msg(y, x1, x2, label, color="#334155", dashed=False):
    arrow(ax, (x1, y), (x2, y), color=color, dashed=dashed)
    midx = (x1 + x2) / 2
    ax.text(midx, y + 0.12, label, ha="center", fontsize=8.8, color=color)

msg(6.0, 1.5, 4.5, "POST /orders/{id}/pay")
msg(5.5, 4.5, 10.3, "createPaymentIntent")
msg(5.0, 10.3, 4.5, "clientSecret", dashed=True)
msg(4.5, 4.5, 7.5, "changeStatus(PENDING_PAYMENT)", color="#16a34a")
msg(4.0, 4.5, 1.5, "PaymentResult", dashed=True)
ax.text(6, 3.4, "— customer pays on Stripe (time passes) —", ha="center",
        fontsize=9.5, color="#92400e", style="italic")
msg(2.9, 10.3, 4.5, "POST /webhooks/stripe (signed)")
ax.text(4.5, 2.45, "verify signature\n(shared secret)", ha="center", fontsize=8.2,
        color="#7f1d1d", bbox=dict(boxstyle="round,pad=0.3", fc="#fee2e2", ec="#dc2626"))
msg(1.7, 4.5, 7.5, "changeStatus(PAID / FAILED)", color="#16a34a")

plt.savefig(os.path.join(OUT, "payment-flow.png"), dpi=170, bbox_inches="tight",
            facecolor="white")
plt.close(fig)

print("Generated:", os.listdir(OUT))
