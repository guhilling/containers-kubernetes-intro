# Kapitel 4: Container/Pods auf Kubernetes
<!-- level: rookie -->

## Namespace / OpenShift Project

Ein **Namespace** ist eine logische Isolationsschicht in Kubernetes.
OpenShift nennt diese **Projects** (= Namespace + zusätzliche Metadaten).

Genauer: Ein **Project** ist ein Hilfsobjekt, um leichter mit **Namespaces** umgehen zu können.

```bash
# Namespace/Project anzeigen
oc get projects             # geht immer
kubectl get namespaces      # ❗ nur mit cluster-admin-Berechtigung

# Aktuelles Project anzeigen
oc project

# (fast) Alle Ressourcen im aktuellen Namespace
kubectl get all             # ❗ Developer Sandbox: Fehler bei einzelnen Ressourcentypen (fehlende Rechte)
```

Alle Befehle in diesem Seminar werden im **Repository-Root** ausgeführt und wirken im
aktuell aktiven Project.

---

## Warum Pods?

Ein **Pod** ist die kleinste deploybare Einheit in Kubernetes:
- Enthält einen oder mehrere Container
- Teilen sich Netzwerk (localhost) und Storage (optional)
- Haben eine gemeinsame IP-Adresse
- Laufen immer auf demselben Node

```bash
# Pod direkt starten (für Tests/Demos)
oc run demo --image=quay.io/ghilling/quarkus-demo:1.0 --port=8080

# Warten, bis der Pod bereit ist
# (Ready heißt hier nur: Der Container läuft. Ob die Anwendung schon antwortet,
#  weiß Kubernetes erst mit einer Readiness-Probe – siehe Kapitel 10.)
oc wait --for=condition=Ready pod/demo --timeout=120s

# Pod anzeigen
oc get pods
oc get pod demo -o wide

# Pod-Details
oc describe pod demo

# Logs
oc logs demo
oc logs -f demo   # folgen (Abbruch mit Strg+C)

# Befehl im Pod ausführen (--retry: falls die Anwendung noch startet)
oc exec demo -- curl -s --retry 10 --retry-all-errors --retry-delay 1 http://localhost:8080/

# In Pod einloggen
oc exec -it demo -- /bin/bash
oc exec -it demo -- sh
```

---

## Troubleshooting

```bash
# Pod-Status prüfen
oc get pods
# STATUS: Running | Pending | CrashLoopBackOff | ImagePullBackOff | Error

# Detaillierte Infos (Events sind wichtig!)
oc describe pod demo

# Logs des Containers
oc logs demo
oc logs demo --previous   # ❗ nur nach einem Neustart des Containers (z. B. nach Crash)

# Events im Namespace
oc get events --sort-by='.lastTimestamp'

# Resource-Nutzung
oc adm top pods           # ❗ Metriken erst ca. 1 Minute nach dem Pod-Start verfügbar

# Netzwerk-Test aus dem Pod
oc exec demo -- curl -s http://localhost:8080/

# Pod wieder löschen
oc delete pod demo
```

**Häufige Fehler:**

| Status | Ursache |
|---|---|
| `ImagePullBackOff` | Image nicht gefunden oder keine Pull-Berechtigung |
| `CrashLoopBackOff` | Container startet, crasht sofort (Logs prüfen!) |
| `Pending` | Kein Node verfügbar (Ressourcen?) oder fehlende PVC |
| `OOMKilled` | Container überschreitet Memory-Limit |
| `CreateContainerConfigError` | Secret oder ConfigMap fehlt |

---

## Grenzen von "Standalone Pods"

Ein direkter Pod (ohne Deployment) hat folgende Nachteile:

| Situation | Standalone Pod | Deployment |
|---|---|---|
| Container crasht | Neustart im selben Pod durch das kubelet ✅ | Neustart im selben Pod durch das kubelet ✅ |
| Node fällt aus | Pod ist weg ❌ | Neuer Pod auf einem anderen Node ✅ |
| Pod wird gelöscht oder verdrängt (Eviction, Node-Wartung) | Pod ist weg ❌ | Neuer Pod wird gestartet ✅ |
| Skalierung | Manuell (weitere Pods anlegen) ❌ | `replicas: N` ✅ |
| Rolling Update | Nicht möglich ❌ | Automatisch ✅ |
| Rollback | Nicht möglich ❌ | `oc rollout undo` ✅ |

Der Neustart eines abgestürzten **Containers** ist also kein Vorteil des Deployments – das
erledigt das kubelet für jeden Pod (`restartPolicy: Always` ist der Default). Der Unterschied
liegt darin, was passiert, wenn der **Pod selbst** verschwindet: Ein Standalone Pod ist dann
weg, ein Deployment (genauer: sein ReplicaSet) erzeugt einen neuen.

> **Fazit**: Standalone Pods nur für Debugging und Tests. Für Produktionsanwendungen immer ein
> **Deployment** (oder **StatefulSet**, für Anwendungen mit eigenem Zustand wie Datenbanken)
> verwenden!

---

## Deployment und ReplicaSet

```
Deployment
  └── ReplicaSet (v1)          ← alte Version
  └── ReplicaSet (v2, aktiv)   ← neue Version
        └── Pod
        └── Pod
        └── Pod
```

```bash
# Deployment erstellen (imperativ)
oc create deployment demo --image quay.io/ghilling/quarkus-demo:1.0 --port 8080
oc rollout status deployment/demo
oc delete deployment demo

# oder deklarativ aus einem Manifest:
oc apply -f 04-pods-on-kubernetes/deployment.yaml

# Um die Manifest-Datei selber zu erzeugen:
oc create deployment demo --image quay.io/ghilling/quarkus-demo:1.0 --port 8080 --dry-run=client -o yaml > /tmp/demo-deployment.yaml
cat /tmp/demo-deployment.yaml

# Deployment anzeigen
oc get deployments
oc describe deployment demo

# Skalieren
oc scale deployment demo --replicas=3

# Rolling Update
oc set image deployment/demo demo=quay.io/ghilling/quarkus-demo:1.1

# Rollout-Status verfolgen
oc rollout status deployment/demo

# Rollout-Historie
oc rollout history deployment/demo

# Rollback
oc rollout undo deployment/demo
oc rollout status deployment/demo
```

---

## Kubernetes-Manifeste für dieses Kapitel

Die YAML-Manifeste befinden sich in diesem Verzeichnis:

- `pod.yaml` – Standalone Pod (für Demo)
- `deployment.yaml` – Deployment mit ReplicaSet

### Hinweis: SecurityContext in OpenShift

OpenShift setzt über die **Security Context Constraints (SCC)** automatisch restriktive Sicherheitsrichtlinien für Pods und Container durch — ohne dass diese explizit im Manifest angegeben werden müssen. Die `restricted-v2` SCC (Standard in OpenShift) erzwingt beispielsweise automatisch:

- `allowPrivilegeEscalation: false`
- `runAsNonRoot: true`
- `seccompProfile.type: RuntimeDefault`
- `capabilities.drop: [ALL]`

Das bedeutet: In der Praxis müssen diese `securityContext`-Felder **nicht** explizit in Deployment-Manifesten aufgeführt werden — OpenShift kümmert sich selbstständig darum. In der `pod.yaml` dieses Kapitels sind sie zur Veranschaulichung dennoch eingetragen, um zu zeigen, was OpenShift im Hintergrund einstellt.

---

## Weiterführende Links

- [Kubernetes: Namespaces](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)
- [Kubernetes: Pods](https://kubernetes.io/docs/concepts/workloads/pods/)
- [Kubernetes: Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [OpenShift 4.22: Building Applications](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/building_applications/index)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
