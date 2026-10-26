# Seminarunterlagen: Container und Kubernetes/OpenShift

Viertägiges Seminar: Einführung in Container-Technologie und Kubernetes/OpenShift.

## Inhaltsverzeichnis

Das Seminar besteht aus einem **Rookie-Teil** (Tag 1–2, Kapitel 1–11) und einem
**Advanced-Teil** (Tag 3–4, Kapitel 12–21).

| Kapitel | Teil | Thema |
|---|---|---|
| [1 Container](./01-container/) | Rookie | Container-Grundlagen, Podman/Docker, Containerfile, Registries |
| [2 Kubernetes Basics](./02-kubernetes-basics/) | Rookie | Architektur, Control-Loop, kubectl/oc, OpenShift-Login |
| [3 OpenShift vs. "Vanilla Kubernetes"](./03-openshift-vs-kubernetes/) | Rookie | OpenShift vs. Vanilla Kubernetes |
| [4 Container/Pods auf Kubernetes](./04-pods-on-kubernetes/) | Rookie | Namespaces, Pods, Deployments, Troubleshooting |
| [5 Automation – Kubernetes Manifeste & GitOps](./05-automation/) | Rookie | Kubernetes-Manifeste, GitOps mit oc apply |
| [6 Networking – Services, Routes & Port-Forward](./06-networking/) | Rookie | Services, Routes, Port-Forward |
| [7 Konfiguration](./07-konfiguration/) | Rookie | Umgebungsvariablen, Secrets, ConfigMaps |
| [8 Requests und Limits](./08-requests-limits/) | Rookie | Requests, Limits, LimitRange |
| [9 Kustomize Grundlagen](./09-kustomize/) | Rookie | Kustomize-Grundlagen, Base/Overlays, `oc apply -k` |
| [10 Health-Checks, Quotas und LimitRanges](./10-health-quotas/) | Rookie | Health-Checks (Probes), Quotas und LimitRanges vertieft |
| [11 Kubernetes RBAC](./11-rbac/) | Rookie | Kubernetes RBAC: Role, RoleBinding, ServiceAccount |
| [12 Server-Side vs. Client-Side Apply](./12-apply-strategies/) | Advanced | Server-Side vs. Client-Side Apply, `--validate`, `--dry-run`, kuberc |
| [13 ConfigMaps und Secrets vertieft](./13-configmaps-secrets/) | Advanced | ConfigMaps/Secrets vertieft, `configMapGenerator`, Sealed Secrets |
| [14 OpenShift Security Context Constraints (SCCs)](./14-scc/) | Advanced | OpenShift Security Context Constraints, feste User-IDs |
| [15 Persistent Storage](./15-storage/) | Advanced | Persistent Storage: PVCs, Provisioning, Resizing, Snapshots |
| [16 Grundlagen der NetworkPolicies](./16-networkpolicies/) | Advanced | Grundlagen der NetworkPolicies |
| [17 Jobs und CronJobs](./17-jobs/) | Advanced | Jobs und CronJobs: Wiederholungen, Parallelität, Zeitpläne |
| [18 Scheduling – Affinity, Anti-Affinity und Topology Spread](./18-scheduling/) | Advanced | Node Affinity, Pod (Anti-)Affinity, Topology Spread Constraints, Taints |
| [19 Verfügbarkeit – Rolling Updates, PodDisruptionBudget, Autoscaling](./19-availability/) | Advanced | Rolling Updates ohne Ausfall, PodDisruptionBudget, Autoscaling (HPA) |
| [20 Debugging und Observability](./20-debugging/) | Advanced | Fehlerbilder diagnostizieren, `oc debug`, Ephemeral Containers, Logs, Events, Metriken |
| [21 Quarkus: Kurze Wiederholung](./21-quarkus/) | Advanced | Quarkus auf OpenShift: SmallRye Health, Konfiguration, Graceful Shutdown |
| [Anhang](./99-anhang/) | – | VS-Code-Setup, [weiterführende Dokumentation](./99-anhang/DOCUMENTATION.md) |

## Demo-Anwendung

Das Verzeichnis [01-container/demo-app](./01-container/demo-app/) enthält eine
[Quarkus](https://quarkus.io/)-Anwendung (Java 21), die als roter Faden durch alle Kapitel
verwendet wird. Die fertigen Images `quay.io/ghilling/quarkus-demo:1.0`, `:1.1` und `:1.2` (ab Kapitel 21)
baut die CI für `linux/amd64` und `linux/arm64`.

| Endpoint | Methode | Wirkung |
|---|---|---|
| `/`, `/<datei>` | GET | Liefert Dateien aus `/var/demo/html` aus (konfigurierbar über `DEMO_HTML_DIR`) |
| `/info` | GET | Version, Hostname (= Pod-Name) und konfigurierbare Nachricht (`DEMO_MESSAGE`) als JSON |
| `/health` | GET | `200 {"status":"UP"}` oder `503 {"status":"DOWN"}` |
| `/q/health/live`, `/ready`, `/started` | GET | SmallRye Health (Kapitel 21) |
| `/admin/health/toggle` | POST | Kippt den Health-Zustand (UP ↔ DOWN) |
| `/admin/restart` | POST | Beendet den Prozess (simulierter Absturz) |

### Bauen und lokal starten

```bash
cd 01-container/demo-app
mvn package

# HTML-Verzeichnis vorbereiten und App starten
mkdir -p /tmp/html
echo '<html><body><h1>Hallo!</h1></body></html>' > /tmp/html/index.html
DEMO_HTML_DIR=/tmp/html java -jar target/quarkus-app/quarkus-run.jar
# → http://localhost:8080
```

Als Container: siehe [Kapitel 1](./01-container/README.md#image-build--containerfile).

## Voraussetzungen

- OpenShift 4.21+ Cluster (kein Admin-Zugang erforderlich)
- `oc` CLI installiert
- Java 21 + Maven (nur, um die Demo-App selbst zu bauen)
- `podman` oder `docker` (für Kapitel 1, siehe [Podman oder Docker?](./01-container/README.md#podman-oder-docker))

## Anmelden

```bash
oc login https://api.<cluster>.<domain>:6443
auf dem eigenen Cluster:
    oc new-project <project-name>
oder auf der Red Hat Sandbox den zugewiesenen Namespace verwenden.
```

## Konventionen

- Alle Manifeste werden in das **aktuell aktive Project** angewendet – sie enthalten keinen
  `namespace`. Für alle Übungen genügt die Rolle `admin` im eigenen Project.
- ❗ markiert Beispiele, die **nicht** ohne Weiteres ausführbar sind (z. B. weil
  Cluster-Admin-Rechte oder spezielle Cluster-Features nötig sind). Die zugehörigen Manifeste
  liegen im Unterverzeichnis `cluster-specific/` des jeweiligen Kapitels.

```bash
oc apply -f 05-automation/deployment.yaml
oc apply -f 06-networking/service.yaml
oc apply -f 06-networking/route.yaml
```

## Weiterführende Dokumentation

Siehe [99-anhang/DOCUMENTATION.md](./99-anhang/DOCUMENTATION.md) für eine
kuratierte Liste externer Referenzen zur Kubernetes- und OpenShift-4.22-Dokumentation.