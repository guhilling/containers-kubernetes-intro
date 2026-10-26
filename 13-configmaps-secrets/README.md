# Kapitel 13: ConfigMaps und Secrets vertieft
<!-- level: advanced -->

## Das Problem aus Kapitel 7

In Kapitel 7 haben wir gesehen: Wenn eine als Volume gemountete ConfigMap
geändert wird, aktualisiert Kubernetes das Volume im laufenden Pod
(nach ca. 1 Minute) – aber **die Anwendung merkt davon meist nichts**, wenn
sie die Datei nur beim Start einliest. Ein "richtiger" Rollout (mit neuen,
frischen Pods) passiert **nicht automatisch**.

Noch unpraktischer wird es bei Umgebungsvariablen aus ConfigMaps/Secrets
(`valueFrom.configMapKeyRef` / `secretKeyRef`): Diese werden **nur beim
Containerstart** ausgelesen. Eine spätere Änderung der ConfigMap wirkt sich
erst nach einem manuellen Neustart des Pods aus.

---

## Die Lösung: `configMapGenerator`

Statt eine ConfigMap mit festem Namen zu pflegen, lässt man Kustomize sie **aus
Dateien generieren** – bei jeder inhaltlichen Änderung entsteht dabei ein
**neuer Name mit Content-Hash-Suffix**:

```yaml
# kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml

configMapGenerator:
  - name: html-content
    files:
      - html/index.html
    options:
      immutable: true

secretGenerator:
  - name: demo-secret
    literals:
      - password=changeme
```

Im Deployment wird ganz normal auf den **logischen** Namen `html-content`
verwiesen:

```yaml
volumes:
  - name: html-content
    configMap:
      name: html-content
```

Beim Rendern ersetzt Kustomize den Namen automatisch durch die
Hash-Variante – **überall**, wo er referenziert wird (Volumes, `envFrom`,
`configMapKeyRef`):

```bash
oc kustomize 13-configmaps-secrets
# → ConfigMap heißt jetzt z.B. html-content-d5k5dd82gd
# → Deployment referenziert automatisch denselben Namen
```

Ändert sich `html/index.html`, ändert sich der Hash → ein **neuer** ConfigMap-Name
→ das Pod-Template des Deployments ändert sich → Kubernetes löst einen
**echten Rollout** aus (neue Pods, alte werden terminiert). Genau das Problem
aus Kapitel 7 ist damit gelöst, ganz ohne manuellen Eingriff oder
zusätzliches Tool.

```bash
oc apply -k 13-configmaps-secrets

# Inhalt ändern und erneut anwenden
echo '<h1>Neuer Inhalt!</h1>' > 13-configmaps-secrets/html/index.html
oc apply -k 13-configmaps-secrets
oc rollout status deployment/demo
oc get configmaps -l app=demo   # zwei ConfigMaps: alte + neue Hash-Variante

# Änderung an der Datei wieder rückgängig machen
git checkout 13-configmaps-secrets/html/index.html
```

Da jede Inhaltsänderung ohnehin eine **neue** ConfigMap erzeugt, kann die generierte
ConfigMap als `immutable: true` markiert werden: Kubernetes verhindert dann versehentliche
Änderungen (`oc edit`) und muss die ConfigMap nicht mehr auf Änderungen überwachen
(entlastet den API-Server).

Alte, nicht mehr referenzierte ConfigMaps bleiben zunächst bestehen (kein
automatisches Aufräumen) – das ist bewusst so gebaut, damit ein Rollback auf
eine alte Pod-Revision weiterhin die passende ConfigMap findet.

---

## `secretGenerator` – dasselbe Prinzip für Secrets

`secretGenerator` funktioniert identisch, nur für Secrets (`literals`,
`files`, oder `envs` für ganze `.env`-Dateien). Auch hier: Namensänderung bei
Inhaltsänderung → automatischer Rollout, sobald das Secret per Volume oder
`valueFrom` referenziert wird (siehe `DEMO_SECRET_PASSWORD` im
`deployment.yaml` dieses Kapitels).

### `disableNameSuffixHash`: der Trade-off

Manchmal ist der automatische Rollout **nicht** gewünscht (z. B. wenn viele
Ressourcen dieselbe ConfigMap referenzieren und man den Rollout lieber
kontrolliert selbst auslöst). Dafür gibt es `disableNameSuffixHash: true` –
dann bleibt der Name stabil, aber man verliert den automatischen
Rollout-Trigger und ist wieder beim Verhalten aus Kapitel 7.

```yaml
configMapGenerator:
  - name: html-content
    files:
      - html/index.html
    options:
      disableNameSuffixHash: true
```

---

## Ausblick: Sealed Secrets

Ein normales Kubernetes-Secret ist nur base64-kodiert (Kapitel 7) – **nicht
verschlüsselt**. Es darf daher nicht direkt in Git landen (auch nicht in
einem privaten Repo).

**Sealed Secrets** (Bitnami) lösen das: Ein Secret wird lokal mit dem
**öffentlichen Schlüssel** eines im Cluster laufenden Controllers
verschlüsselt und als `SealedSecret`-Custom-Resource abgelegt – dieses YAML
ist git-safe, da nur der Controller (mit dem privaten Schlüssel) es wieder in
ein echtes Secret entschlüsseln kann.

> Dieses Kapitel demonstriert Sealed Secrets **nicht praktisch**, da die
> Installation des Controllers Cluster-Admin-Rechte erfordert. Für eigene
> Cluster: siehe [github.com/bitnami-labs/sealed-secrets](https://github.com/bitnami-labs/sealed-secrets).

---

## Manifeste in diesem Kapitel

- `kustomization.yaml` – `configMapGenerator` + `secretGenerator`
- `html/index.html` – Quelldatei für die generierte ConfigMap
- `deployment.yaml` – referenziert ConfigMap/Secret über den logischen Namen

---

## Weiterführende Links

- [Kubernetes: Kustomization](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/)
- [Kubernetes: ConfigMaps](https://kubernetes.io/docs/concepts/configuration/configmap/)
- [Kubernetes: Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)
- [Sealed Secrets (bitnami-labs)](https://github.com/bitnami-labs/sealed-secrets)

Siehe auch [99-anhang/DOCUMENTATION.md](../99-anhang/DOCUMENTATION.md) für die vollständige Liste.
