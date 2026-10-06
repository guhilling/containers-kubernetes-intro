# Kapitel 12: Server-Side vs. Client-Side Apply
<!-- level: advanced -->

## Zwei Wege, "apply" zu machen

| | Client-Side Apply | Server-Side Apply |
|---|---|---|
| Wo wird der Merge berechnet? | Im `kubectl`/`oc`-Client | Im API-Server |
| Verlauf gespeichert in | Annotation `kubectl.kubernetes.io/last-applied-configuration` | `managedFields` pro Feld (Field Manager) |
| Weiß, **wer** ein Feld gesetzt hat? | Nein (nur der letzte `apply` zählt) | Ja, pro Field Manager |
| Flag | `oc apply -f deployment.yaml` (Default) | `oc apply -f deployment.yaml --server-side` |

Bisher haben wir immer **Client-Side Apply** verwendet (Standardverhalten von
`apply`). Der Client lädt das aktuelle Objekt, vergleicht es mit der
`last-applied-configuration`-Annotation und der neuen Datei (3-Way-Merge) und
schickt das Ergebnis als vollständiges Objekt zum Server.

**Server-Side Apply** (SSA) schickt stattdessen nur das YAML selbst zum
API-Server und lässt **ihn** den Merge durchführen. Der Server merkt sich
dabei pro Feld, welcher "Field Manager" (z. B. `kubectl`, ein Controller, ein
CI-Tool) es zuletzt gesetzt hat.

```bash
oc apply -f 12-apply-strategies/deployment.yaml --server-side

# managedFields anzeigen
oc get deployment demo -o yaml --show-managed-fields | grep -A5 managedFields
```

---

## Konflikte zwischen Field Managern

Wenn zwei unterschiedliche Field Manager dasselbe Feld setzen wollen, lehnt
Server-Side Apply das standardmäßig ab (anders als Client-Side Apply, wo
einfach der letzte Schreiber gewinnt):

```bash
# Erster "Field Manager"
oc apply -f 12-apply-strategies/deployment.yaml --server-side --field-manager=team-a

# Zweiter Field Manager ändert dasselbe Feld (replicas) über kubectl edit / patch
oc patch deployment demo --type=merge -p '{"spec":{"replicas":5}}' --field-manager=team-b

# Erneutes Apply von team-a auf dasselbe Feld → Conflict
oc apply -f 12-apply-strategies/deployment.yaml --server-side --field-manager=team-a   # → Konflikt (Fehler erwartet)
# error: Apply failed with 1 conflict: conflict with "team-b" ... .spec.replicas

# Auflösen: Übernahme erzwingen
oc apply -f 12-apply-strategies/deployment.yaml --server-side --field-manager=team-a --force-conflicts
```

Das macht sichtbar, was Client-Side Apply verschleiert: **wer** eigentlich für
ein Feld "zuständig" ist – wichtig, sobald mehrere Tools (GitOps-Controller,
HPA, manuelle Eingriffe) an derselben Ressource schreiben.

---

## `--validate`: Wie streng wird das YAML geprüft?

Demo mit einem Tippfehler (`replias` statt `replicas`):

```bash
cat <<'EOF' > /tmp/deployment-typo.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo
  labels:
    app: demo
spec:
  replias: 2   # Tippfehler!
  selector:
    matchLabels:
      app: demo
  template:
    metadata:
      labels:
        app: demo
    spec:
      containers:
        - name: demo
          image: quay.io/ghilling/quarkus-demo:1.0
EOF

# strict (Default): unbekannte Felder = Fehler
oc apply -f /tmp/deployment-typo.yaml --validate=strict   # → unknown field "spec.replias" (Fehler erwartet)

# warn: unbekannte Felder = Warnung, wird trotzdem angewendet (mit replicas=1, dem Default!)
oc apply -f /tmp/deployment-typo.yaml --validate=warn

# ignore: keine Schema-Prüfung
oc apply -f /tmp/deployment-typo.yaml --validate=ignore
```

`--validate=strict` verhindert genau solche stillen Fehler und ist seit
neueren `kubectl`/`oc`-Versionen der Default.

---

## `--dry-run=client` vs. `--dry-run=server`

| | `--dry-run=client` | `--dry-run=server` |
|---|---|---|
| Netzwerk-Aufruf an API-Server? | Nein | Ja |
| Durchläuft Admission-Chain (Validating/Mutating Webhooks, Quotas, SCCs)? | Nein | Ja |
| Persistiert die Änderung? | Nein | Nein |
| Zeigt realistisches Ergebnis (inkl. Defaults, die der Server setzt)? | Eingeschränkt | Ja |

```bash
# Nur lokal rendern/validieren, kein API-Call
oc apply -f 12-apply-strategies/deployment.yaml --dry-run=client -o yaml

# Serverseitig "probelaufen": inkl. Admission-Chain, aber ohne zu speichern
oc apply -f 12-apply-strategies/deployment.yaml --dry-run=server -o yaml
```

`--dry-run=server` ist der einzige Weg, um vorab zu sehen, ob z. B. eine
ResourceQuota (Kapitel 8) oder eine SCC (Kapitel 14) die Anfrage ablehnen
würde – `--dry-run=client` weiß von solchen Cluster-Richtlinien nichts.

---

## User Preferences: `kuberc`

Bisher galt: Alle Einstellungen (Cluster-Zugang, Auth, aber auch persönliche
Aliases/Defaults) landen in **einer** gemeinsamen `kubeconfig`. Das ist
unpraktisch, wenn ein Team die kubeconfig teilt, aber jede:r eigene
CLI-Vorlieben hat (z. B. `--validate=warn` als persönlichen Default oder einen
Alias `k get pods` → `kubectl get pods -o wide`).

`kuberc` trennt das: eine **persönliche** Preferences-Datei
(`~/.kube/kuberc`), die nur CLI-Verhalten (Defaults, Aliases, Overrides)
enthält, unabhängig von der (oft geteilten) `kubeconfig`.

```yaml
# ~/.kube/kuberc (Beispiel)
apiVersion: kubectl.config.k8s.io/v1alpha1
kind: Preference
overrides:
  - command: apply
    flags:
      - name: validate
        default: warn
aliases:
  - name: k
    command: get
    prependArgs: ["pods"]
```

```bash
# aktiv nur mit dem Feature-Gate/neuerer kubectl-Version:
KUBECTL_KUBERC=true kubectl get pods
```

---

## Manifeste in diesem Kapitel

- `deployment.yaml` – Übungsobjekt für die Apply-/Validate-/Dry-Run-Demos

---

## Weiterführende Links

- [Kubernetes: Server-Side Apply](https://kubernetes.io/docs/reference/using-api/server-side-apply/)
- [Kubernetes: kubectl apply (Referenz)](https://kubernetes.io/docs/reference/kubectl/generated/kubectl_apply/)
- [OpenShift 4.22: CLI Tools](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/cli_tools/index)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
