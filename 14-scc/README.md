# Kapitel 14: OpenShift Security Context Constraints (SCCs)
<!-- level: advanced -->

Kubernetes kennt für Pod-Sicherheit die **Pod Security Admission** (Labels
`enforce`/`audit`/`warn` mit den Stufen `privileged`/`baseline`/`restricted`).
OpenShift geht mit **SCCs (Security Context Constraints)** einen Schritt
weiter: SCCs sind feingranularer, werden pro ServiceAccount/User zugewiesen
und steuern u. a., welche User-IDs, Capabilities oder Volume-Typen ein Pod
verwenden darf.

> ❗ Das Anlegen und Zuweisen von SCCs erfordert Cluster-Admin-Rechte. Die
> Manifeste in `cluster-specific/` sind daher **Referenzbeispiele** – zum
> Nachvollziehen, nicht zum eigenständigen Ausprobieren ohne Admin-Zugriff.

## Standard-SCC `restricted-v2`

Jeder Pod, der keiner anderen SCC zugewiesen ist, läuft standardmäßig unter
`restricted-v2`. Welche SCC ein Pod bekommen hat und welche UID OpenShift vergeben hat,
lässt sich am Pod selbst ablesen:

```bash
oc apply -f 04-pods-on-kubernetes/pod.yaml
oc wait --for=condition=Ready pod/demo --timeout=120s

# Verwendete SCC (Annotation am Pod)
oc get pod demo -o jsonpath='{.metadata.annotations.openshift\.io/scc}{"\n"}'

# Vergebene UID – stammt aus dem UID-Bereich des Projects
oc exec demo -- id
oc get project $(oc project -q) -o jsonpath='{.metadata.annotations.openshift\.io/sa\.scc\.uid-range}{"\n"}'

oc delete pod demo
```

❗ Die SCC-Definitionen selbst anzusehen erfordert Leserechte auf Cluster-Ebene
(z. B. nicht in der Developer Sandbox):

```bash
oc get scc
oc describe scc restricted-v2
```

`restricted-v2` erzwingt u. a.:

- keine privilegierten Container
- keine feste, vom Nutzer gewählte User-ID (`runAsUser: MustRunAsRange`) –
  OpenShift weist stattdessen automatisch eine UID aus einem für das Project
  reservierten Bereich zu
- `allowPrivilegeEscalation: false`
- `readOnlyRootFilesystem` nicht erzwungen, aber viele weitere Einschränkungen
  bei Capabilities und Volume-Typen

Das erklärt, warum Container-Images, die intern eine feste User-ID (z. B.
`root`, UID 0, oder eine andere fest einkompilierte UID) voraussetzen, auf
OpenShift ohne Anpassung oft nicht laufen – die tatsächlich verwendete UID
steht erst zur Laufzeit fest.

## `runAsUser`-Strategien

| Strategie          | Bedeutung                                                              |
|----------------------|---------------------------------------------------------------------------|
| `MustRunAs`          | Es wird eine einzige, feste UID erzwungen (Beispiel in diesem Kapitel)   |
| `MustRunAsRange`     | UID muss aus einem Bereich stammen (Default für `restricted-v2`)         |
| `RunAsAny`           | Keine Einschränkung – Pod/Image bestimmt die UID selbst                 |

## Beispiel: feste User-ID

`cluster-specific/scc-fixed-uid.yaml` zeigt eine SCC, die genau die UID `1001` erzwingt – ein
typisches Muster für Legacy-Images, die intern mit einer bestimmten,
fest verdrahteten User-ID arbeiten:

```yaml
runAsUser:
  type: MustRunAs
  uid: 1001
```

❗ Zuweisung an eine ServiceAccount (nur mit Cluster-Admin-Rechten möglich):

```bash
oc adm policy add-scc-to-user demo-fixed-uid -z demo-reader
```

Erst nach dieser Zuweisung darf ein Pod, der über die entsprechende
ServiceAccount läuft, tatsächlich mit UID `1001` starten – ohne Zuweisung
lehnt der Admission-Controller den Pod ab.

## Ausblick: Linux User Namespaces

Ein neuerer Ansatz, der einen Teil der SCC-Komplexität rund um User-IDs
entschärfen könnte, sind Linux **User Namespaces** für Pods
(`hostUsers: false` im Pod-Spec). Dabei werden die UIDs *innerhalb* des
Containers auf einen anderen, isolierten UID-Bereich auf dem Host abgebildet
– ein Prozess, der im Container als "root" (UID 0) läuft, hat auf dem Host
eine unprivilegierte, gemappte UID. Das reduziert das Sicherheitsrisiko
fester/hoher Rechte im Container, unabhängig davon, welche UID das Image
intern erwartet.

Der Reifegrad und die Verfügbarkeit dieser Funktion unterscheiden sich je
nach Kubernetes-/OpenShift-Version und Container-Runtime – zum
Zeitpunkt dieses Seminars ist das Feature als Ausblick zu verstehen, nicht
als aktuell einsatzbereite Standardlösung.

## Manifeste in diesem Kapitel

- ❗ `cluster-specific/scc-fixed-uid.yaml` – Referenz-SCC mit fester User-ID (Cluster-Admin nötig)

---

## Weiterführende Links

- [OpenShift 4.22: Security and Compliance (SCCs)](https://docs.redhat.com/en/documentation/openshift_container_platform/4.22/html/security_and_compliance/index)
- [Kubernetes: Pod Security Standards](https://kubernetes.io/docs/concepts/security/pod-security-standards/)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
